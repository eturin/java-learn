package ture.bank.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ture.bank.service.JwtService;

import java.io.IOException;

/**
 * Фильтр для аутентификации на основе JWT (JSON Web Token).
 * <p>Этот фильтр перехватывает каждый HTTP запрос и проверяет наличие валидного JWT токена
 * в заголовке Authorization. Если токен найден и валиден, пользователь считается
 * аутентифицированным и информация о нем помещается в SecurityContext.</p>
 * <p><strong>Принцип работы:</strong>
 * <pre>
 * HTTP Запрос → JwtAuthenticationFilter → Проверка токена → Установка аутентификации → Контроллер
 *      ↓                   ↓                       ↓                  ↓                    ↓
 *  Содержит          Извлекает токен         Валидирует       Создает объект        Получает
 *  заголовок         из Authorization      с помощью JwtService  Authentication      аутентифицированного
 *  Authorization                              и UserDetailsService                   пользователя
 * </pre>
 * </p>
 * <p><strong>Расположение в цепочке фильтров:</strong>
 * Этот фильтр должен выполняться ДО стандартных фильтров Spring Security,
 * чтобы установить аутентификацию до проверки прав доступа.</p>
 *
 * @see OncePerRequestFilter
 * @see JwtService
 * @see UserDetailsService
 * @see SecurityContextHolder
 * @see UsernamePasswordAuthenticationToken
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Сервис для работы с JWT токенами.
     * <p>Используется для:
     * <ul>
     *   <li>Извлечения имени пользователя из токена</li>
     *   <li>Валидации токена (подпись, срок действия)</li>
     * </ul></p>
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Сервис для загрузки данных пользователей.
     * <p>Используется для получения {@link UserDetails} по имени пользователя,
     * извлеченному из JWT токена.</p>
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Имя cookie для JWT токена.
     */
    public static final String JWT_COOKIE_NAME = "jwt_token";

    /**
     * Основной метод фильтра, который обрабатывает каждый HTTP запрос.
     * <p><strong>Алгоритм работы:</strong>
     * <ol>
     *   <li>Проверяет наличие заголовка Authorization с Bearer токеном</li>
     *   <li>Извлекает JWT токен из заголовка</li>
     *   <li>Извлекает имя пользователя из токена</li>
     *   <li>Если пользователь еще не аутентифицирован в текущем контексте:</li>
     *   <ol type="a">
     *     <li>Загружает UserDetails из базы данных</li>
     *     <li>Валидирует токен с помощью JwtService</li>
     *     <li>Создает объект аутентификации</li>
     *     <li>Устанавливает аутентификацию в SecurityContext</li>
     *   </ol>
     *   <li>Пропускает запрос дальше по цепочке фильтров</li>
     * </ol>
     * </p>
     *
     * <h3>Пример заголовка Authorization:</h3>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxNzAwMDAwMCwiZXhwIjoxNzE3MDg2NDAwfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
     * </pre>
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров для продолжения обработки
     * @throws ServletException если происходит ошибка сервлета
     * @throws IOException если происходит ошибка ввода/вывода
     *
     * @example Пример запроса с JWT токеном
     * <pre>{@code
     * GET /api/users/me HTTP/1.1
     * Host: localhost:8080
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * Content-Type: application/json
     * }</pre>
     *
     * @example Пример ответа без токена
     * <pre>{@code
     * // Без заголовка Authorization или с некорректным форматом:
     * // Фильтр просто пропускает запрос дальше
     * // Spring Security вернет 401 Unauthorized
     * }</pre>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractToken(request);

        if (jwt == null) {
            // Если токена нет ни в заголовке, ни в куках, пропускаем запрос дальше
            filterChain.doFilter(request, response);
            return;
        }

        final String username;
        try {
            // Извлекаем имя пользователя из токена
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Если не удалось извлечь username, пропускаем запрос
            filterChain.doFilter(request, response);
            return;
        }

        // Если имя пользователя извлечено и пользователь еще не аутентифицирован в текущем контексте
        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Загружаем данные пользователя из базы данных
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Валидируем токен
            if (jwtService.validateToken(jwt, userDetails)) {
                // Создаем объект аутентификации
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,        // principal - данные пользователя
                        null,               // credentials - не нужны, так как токен уже проверен
                        userDetails.getAuthorities()  // authorities - роли и права пользователя
                );

                // Добавляем дополнительные детали запроса (IP адрес, сессия и т.д.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в контекст безопасности
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Позволяет настроить исключения для определенных путей запросов.
     * <p>Запросы к этим путям будут пропускаться без проверки JWT токена.</p>
     *
     * @param request HTTP запрос
     * @return true если фильтр должен быть пропущен для этого запроса
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Пример: не проверять JWT для публичных эндпоинтов
        String path = request.getRequestURI();
        return path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }

    /**
     * Извлекает JWT токен из различных источников.
     * <p>Поддерживает несколько способов передачи токена:
     * <ul>
     *   <li>Заголовок Authorization: Bearer {token}</li>
     *   <li>Query параметр: ?token={token}</li>
     *   <li>Cookie: jwt_token={token}</li>
     * </ul>
     * </p>
     *
     * @param request HTTP запрос
     * @return JWT токен или null, если токен не найден
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Проверяем заголовок Authorization (основной способ)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Проверяем query параметр (для WebSocket или специфичных случаев)
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }

        // 3. Проверяем cookie (для браузерных приложений)
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Обрабатывает ошибки аутентификации.
     * <p>Если токен невалиден (истек, подделан и т.д.),
     * устанавливает соответствующий статус ответа.</p>
     *
     * @param response HTTP ответ
     * @param errorMessage сообщение об ошибке
     * @param httpStatus HTTP статус
     */
    private void handleAuthenticationError(HttpServletResponse response,
                                           String errorMessage,
                                           int httpStatus)
            throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                        getErrorCode(httpStatus), errorMessage)
        );
    }

    /**
     * Возвращает код ошибки на основе HTTP статуса.
     */
    private String getErrorCode(int httpStatus) {
        switch (httpStatus) {
            case 401: return "unauthorized";
            case 403: return "forbidden";
            case 400: return "bad_request";
            default: return "authentication_error";
        }
    }

    /**
     * Логирует информацию об аутентификации.
     *
     * @param request HTTP запрос
     * @param username имя пользователя
     * @param success успешна ли аутентификация
     */
    private void logAuthentication(HttpServletRequest request,
                                   String username,
                                   boolean success) {
        String logMessage = String.format(
                "JWT Authentication - Path: %s, User: %s, Success: %s, IP: %s",
                request.getRequestURI(),
                username != null ? username : "anonymous",
                success,
                request.getRemoteAddr()
        );

        if (success) {
            logger.debug(logMessage);
        } else {
            logger.warn(logMessage);
        }
    }
}


