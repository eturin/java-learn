package ture.bank.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ture.bank.config.JwtProperties;

import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис для работы с JWT (JSON Web Token) токенами.
 * <p>Этот сервис предоставляет функциональность для создания, верификации и извлечения информации
 * из JWT токенов. Используется для аутентификации и авторизации пользователей в системе.</p>
 * <p><strong>Основные возможности:</strong>
 * <ul>
 *   <li>Генерация JWT токенов для аутентифицированных пользователей</li>
 *   <li>Верификация валидности токенов (подпись, срок действия)</li>
 *   <li>Извлечение информации (claims) из токенов</li>
 *   <li>Проверка соответствия токена пользователю</li>
 * </ul></p>
 *
 * @see JwtProperties
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see <a href="https://github.com/jwtk/jjwt">JJWT Library</a>
 * @see <a href="https://jwt.io/introduction">JWT Specification</a>
 */
@Service
public class JwtService {
    /**
     * Конфигурационные свойства JWT.
     */
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * Создает секретный ключ для подписи и верификации JWT токенов.
     * <p><strong>Алгоритм:</strong> HMAC-SHA256 (HS256)
     * <br>
     * <strong>Важно:</strong> Ключ генерируется на основе секретной строки из {@link JwtProperties}.
     * Один и тот же секрет всегда порождает одинаковый ключ</p>
     *
     * @return секретный ключ типа {@link SecretKey}
     * @throws IllegalArgumentException если секретная строка некорректна
     * @see JwtProperties#getSecret()
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Извлекает имя пользователя (subject) из JWT токена.
     * <p>Имя пользователя хранится в claim {@code sub} (subject) токена.</p>
     *
     * @param token JWT токен в виде строки
     * @return String
     * @throws io.jsonwebtoken.JwtException если токен невалиден, истек или не может быть разобран
     * @throws IllegalArgumentException если токен {@code null} или пустой
     * @see Claims#getSubject()
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия из JWT токена.
     * <p>Дата истечения хранится в claim {@code exp} (expiration) токена.</p>
     *
     * @param token JWT токен в виде строки
     * @return Date
     * @throws io.jsonwebtoken.JwtException если токен невалиден или не может быть разобран
     * @see Claims#getExpiration()
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает произвольное claim (утверждение) из JWT токена.
     * <p>Метод использует функциональный интерфейс для гибкого извлечения данных.
     * Полезен для получения кастомных claims, добавленных при генерации токена.</p>
     *
     * @param <T> тип возвращаемого значения
     * @param token JWT токен в виде строки
     * @param claimsResolver функция-извлекатель для конкретного claim
     * @return значение claim указанного типа
     * @throws io.jsonwebtoken.JwtException если токен невалиден или не может быть разобран
     * @throws IllegalArgumentException если token или claimsResolver {@code null}
     *
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все claims (утверждения) из JWT токена.
     * <p>
     * <strong>Процесс верификации:</strong>
     * <ol>
     *   <li>Проверяется цифровая подпись токена с использованием секретного ключа</li>
     *   <li>Проверяется структура токена (header.payload.signature)</li>
     *   <li>Токен парсится и возвращаются все claims</li>
     * </ol>
     * </p>
     * <p>
     *
     * @param token JWT токен в виде строки
     * @return объект {@link Claims}, содержащий все claims токена
     * @throws io.jsonwebtoken.security.SecurityException если подпись невалидна
     * @throws io.jsonwebtoken.MalformedJwtException если токен имеет некорректный формат
     * @throws io.jsonwebtoken.ExpiredJwtException если токен истек (только при наличии валидации)
     * @throws IllegalArgumentException если токен {@code null} или пустой
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Проверяет, истек ли срок действия JWT токена.
     * <p>Сравнивает дату истечения ({@code exp}) токена с текущей датой и временем.</p>
     *
     * @param token JWT токен в виде строки
     * @return {@code true} если токен истек, {@code false} если еще действителен
     * @throws io.jsonwebtoken.JwtException если токен невалиден или не может быть разобран
     * @see #extractExpiration(String)
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Генерирует новый JWT токен для указанного имени пользователя.
     * <p><strong>Содержимое токена (claims):</strong>
     * <ul>
     *   <li>{@code sub} (subject) - имя пользователя</li>
     *   <li>{@code iss} (issuer) - издатель (из {@link JwtProperties})</li>
     *   <li>{@code iat} (issued at) - время создания (текущее время)</li>
     *   <li>{@code exp} (expiration) - время истечения (текущее время + expiration из настроек)</li>
     * </ul></p>
     *
     * @param username имя пользователя, для которого генерируется токен
     * @return сгенерированный JWT токен в виде строки
     * @throws IllegalArgumentException если username {@code null} или пустой
     * @throws IllegalStateException если конфигурация JWT некорректна
     *
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Создает JWT токен с указанными claims и subject.
     * <p><strong>Процесс создания:</strong>
     * <ol>
     *   <li>Добавляет переданные claims</li>
     *   <li>Устанавливает subject (обычно username)</li>
     *   <li>Устанавливает издателя (issuer) из конфигурации</li>
     *   <li>Устанавливает время создания (issued at) - текущее время</li>
     *   <li>Устанавливает время истечения (expiration) на основе конфигурации</li>
     *   <li>Подписывает токен с использованием алгоритма HS256</li>
     *   <li>Собирает итоговый токен в формате Base64Url</li>
     * </ol></p>
     *
     * @param claims дополнительные claims (утверждения) для включения в токен
     * @param subject subject токена (обычно имя пользователя)
     * @return сгенерированный JWT токен
     * @throws io.jsonwebtoken.JwtException если не удается создать токен
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Проверяет валидность JWT токена для конкретного пользователя.
     * <p><strong>Проверки выполняются:</strong>
     * <ol>
     *   <li>Извлекается имя пользователя из токена</li>
     *   <li>Сравнивается с именем пользователя из {@link UserDetails}</li>
     *   <li>Проверяется, не истек ли срок действия токена</li>
     *   <li>Подпись токена проверяется в {@link #extractAllClaims(String)}</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Примечание:</strong> Метод не проверяет, отозван ли токен.
     * Для реализации blacklist токенов потребуется дополнительная логика.
     * </p>
     *
     * @param token JWT токен для проверки
     * @param userDetails данные пользователя для сравнения
     * @return {@code true} если токен валиден для данного пользователя, {@code false} в противном случае
     * @throws io.jsonwebtoken.JwtException если токен невалиден или не может быть разобран
     * @throws IllegalArgumentException если token или userDetails {@code null}
     *
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Генерирует JWT токен с дополнительными claims.
     * <p>Полезно для добавления дополнительной информации в токен,
     * такой как роли, разрешения, идентификаторы и т.д.</p>
     *
     * @param claims дополнительные claims для включения в токен
     * @param username имя пользователя (будет установлено как subject)
     * @return сгенерированный JWT токен
     * @throws IllegalArgumentException если username {@code null} или пустой
     *
     * <h3>Пример:</h3>
     * <pre>{@code
     * Map<String, Object> claims = new HashMap<>();
     * claims.put("role", "ADMIN");
     * claims.put("userId", 12345);
     * claims.put("email", "user@example.com");
     *
     * String token = jwtService.generateTokenWithClaims(claims, "username");
     *
     * var roleName = extractClaim(token, claims -> claims.get("role", String.class));
     * }</pre>
     */
    public String generateTokenWithClaims(Map<String, Object> claims, String username) {
        return createToken(claims, username);
    }

    /**
     * Проверяет, действителен ли JWT токен (без привязки к пользователю).
     * <p>Выполняет базовые проверки:
     * <ul>
     *   <li>Формат токена (header.payload.signature)</li>
     *   <li>Валидность подписи</li>
     *   <li>Срок действия (expiration)</li>
     * </ul></p>
     *
     * @param token JWT токен для проверки
     * @return {@code true} если токен технически валиден, {@code false} в противном случае
     */
    public Boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получает оставшееся время жизни токена в миллисекундах.
     * <p>Полезно для клиентов, чтобы знать, когда нужно обновить токен.</p>
     *
     * @param token JWT токен
     * @return оставшееся время жизни в миллисекундах, или 0 если токен истек
     * @throws io.jsonwebtoken.JwtException если токен невалиден
     */
    public long getRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        long remaining = expiration.getTime() - now.getTime();
        return Math.max(remaining, 0);
    }
}
