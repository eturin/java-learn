package ture.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Класс конфигурации свойств JWT (JSON Web Token).
 * <p>
 * Этот класс автоматически связывает свойства, определенные в файлах конфигурации
 * (например, {@code application.yaml} или {@code application.properties}),
 * с полями Java объекта, используя префикс {@code jwt}.
 * </p>
 * <p>
 * JWT (JSON Web Token) - это стандартный способ безопасной передачи информации
 * между сторонами в виде JSON объекта. В контексте Spring Security токены JWT
 * используются для аутентификации и авторизации пользователей.
 * </p>
 *
 * <h3>Пример конфигурации в {@code application.yaml}:</h3>
 * <pre>{@code
 * jwt:
 *   secret: "mySuperSecretKeyThatIsAtLeast32CharactersLong12345"
 *   expiration: 86400000  # 24 часа в миллисекундах
 *   issuer: "bank-app"
 * }</pre>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.stereotype.Component
 * @see <a href="https://jwt.io/introduction">JWT Introduction</a>
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * Секретный ключ для подписи и верификации JWT токенов.
     * <p>Этот ключ используется алгоритмом HMAC для создания цифровой подписи токена.</p>
     */
    private String secret;
    /**
     * Время жизни JWT токена в миллисекундах (до повторной аутентификации).
     */
    private long expiration;
    /**
     * Издатель (issuer) JWT токена.
     * <p>
     * Это строка, идентифицирующая приложение или сервис, который создал токен.
     * Используется в claim {@code iss} (issuer) внутри JWT токена.
     * К примеру, "my-app".
     * </p>
     */
    private String issuer;

    /**
     * Получает секретный ключ для подписи JWT токенов.
     *
     * @return String
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Устанавливает секретный ключ для подписи JWT токенов.
     * <p>
     * <strong>Важно:</strong> Ключ должен быть установлен через конфигурацию приложения.
     * Не рекомендуется устанавливать его программно, за исключением тестов.
     * </p>
     *
     * @param secret новый секретный ключ
     * @throws IllegalArgumentException если ключ {@code null} или слишком короткий
     */
    public void setSecret(String secret) {
        // Добавлена базовая проверка безопасности
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Секретный ключ JWT должен быть не менее 32 символов");
        }
        this.secret = secret;
    }

    /**
     * Получает время жизни JWT токена в миллисекундах
     *
     * @return long
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Устанавливает время жизни JWT токена.
     *
     * @param expiration новое время жизни в миллисекундах
     * @throws IllegalArgumentException если значение отрицательное
     */
    public void setExpiration(long expiration) {
        if (expiration <= 0) {
            throw new IllegalArgumentException("Время жизни токена должно быть положительным числом");
        }
        this.expiration = expiration;
    }

    /**
     * Получает издателя JWT токенов.
     *
     * @return String
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Устанавливает издателя JWT токенов.
     *
     * @param issuer новый идентификатор издателя
     * @throws IllegalArgumentException если значение {@code null} или пустое
     */
    public void setIssuer(String issuer) {
        if (issuer == null || issuer.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Издатель токена не может быть пустым"
            );
        }
        this.issuer = issuer;
    }


}
