package ture.bank.controller;



import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ture.bank.service.JwtService;
import ture.bank.dto.AuthRequest;
import ture.bank.dto.AuthResponse;
import ture.bank.entity.User;
import ture.bank.repository.UserRepository;
import ture.bank.util.PasswordHasher;

import java.util.Optional;

/**
 * Контроллер для операций аутентификации и управления JWT токенами.
 * <p>Этот контроллер предоставляет REST API эндпоинты для:
 * <ul>
 *   <li>Аутентификации пользователей (логин) с получением JWT токена</li>
 *   <li>Валидации существующих JWT токенов</li>
 * </ul>
 * Все эндпоинты публичны и не требуют аутентификации для доступа.
 * </p>
 * <p><strong>Поток аутентификации:</strong>
 * <pre>
 * 1. Клиент → POST /api/auth/login с login/password
 * 2. Сервер проверяет учетные данные в БД
 * 3. Если валидно → генерирует JWT токен
 * 4. Сервер → возвращает токен клиенту
 * 5. Клиент → использует токен в заголовке Authorization для последующих запросов
 * </pre>
 * </p>
 *
 * @see RestController
 * @see RequestMapping
 * @see AuthenticationManager
 * @see JwtService
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Менеджер аутентификации Spring Security.
     * <p>Используется для стандартной аутентификации с использованием
     * настроенного {@link org.springframework.security.authentication.AuthenticationProvider}.
     * После успешной аутентификации создает объект {@link Authentication},
     * который содержит информацию о пользователе и его правах.</p>
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Сервис для работы с JWT токенами.
     * <p>Отвечает за генерацию, валидацию и извлечение информации из JWT токенов.</p>
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Репозиторий для доступа к данным пользователей в базе данных.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Сервис для загрузки данных пользователей.
     * <p>Используется для валидации токенов путем загрузки {@link UserDetails}
     * по имени пользователя из токена.</p>
     */
    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    /**
     * Аутентификация пользователя и выдача JWT токена.
     * <p>Основной эндпоинт для входа в систему. Принимает логин и пароль,
     * проверяет их валидность и возвращает JWT токен при успешной аутентификации.</p>
     *
     * <h3>Процесс аутентификации:</h3>
     * <ol>
     *   <li>Валидация входных данных (логин и пароль обязательны)</li>
     *   <li>Поиск пользователя в базе данных по логину</li>
     *   <li>Проверка совпадения пароля с помощью {@link PasswordHasher}</li>
     *   <li>Проверка, не удален ли пользователь (soft delete)</li>
     *   <li>Аутентификация через Spring Security {@link AuthenticationManager}</li>
     *   <li>Генерация JWT токена с помощью {@link JwtService}</li>
     *   <li>Возврат токена клиенту в формате {@link AuthResponse}</li>
     * </ol>
     *
     * @param authRequest объект с данными для аутентификации
     * @return ResponseEntity с JWT токеном или сообщением об ошибке
     *
     * @example Пример успешного запроса
     * <pre>{@code
     * // Запрос:
     * POST /api/auth/login
     * Content-Type: application/json
     *
     * {
     *   "login": "admin",
     *   "password": "admin123"
     * }
     *
     * // Ответ (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "login": "admin",
     *   "role": "ADMIN"
     * }
     * }</pre>
     *
     * @example Пример ошибки аутентификации
     * <pre>{@code
     * // Запрос с неверными данными:
     * {
     *   "login": "unknown",
     *   "password": "wrongpassword"
     * }
     *
     * // Ответ (400 Bad Request):
     * "Неверный логин или пароль"
     * }</pre>
     *
     * @example Пример удаленного пользователя
     * <pre>{@code
     * // Ответ (400 Bad Request):
     * "Пользователь удален"
     * }</pre>
     *
     * @throws jakarta.validation.ConstraintViolationException если нарушены ограничения валидации
     * @see AuthRequest
     * @see AuthResponse
     * @see PasswordHasher#checkPassword(String, String)
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        // 1. Поиск пользователя в базе данных по логину
        Optional<User> userOptional = userRepository.findByLogin(authRequest.getLogin());
        if (userOptional.isEmpty()) {
            // Возвращаем общее сообщение об ошибке для безопасности
            // (не говорим, существует ли пользователь, только что данные неверны)
            return ResponseEntity.badRequest().body("Неверный логин или пароль");
        }

        User user = userOptional.get();

        // 2. Проверка пароля с использованием BCrypt
        if (!PasswordHasher.checkPassword(authRequest.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Неверный логин или пароль");
        }

        // 3. Проверка soft delete (мягкого удаления)
        if (user.isDeleted()) {
            return ResponseEntity.badRequest().body("Пользователь удален");
        }

        // 4. Аутентификация через Spring Security
        // Создаем токен аутентификации с логином и паролем
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getLogin(),    // principal (имя пользователя)
                        authRequest.getPassword()  // credentials (пароль)
                )
        );

        // 5. Устанавливаем аутентификацию в контекст безопасности
        // (полезно для текущего запроса, последующие запросы будут использовать JWT)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 6. Генерация JWT токена
        String jwt = jwtService.generateToken(authRequest.getLogin());

        // 7. Получаем роль пользователя для включения в ответ
        String role = user.getRole().getName();

        // 8. Возвращаем успешный ответ с токеном
        return ResponseEntity.ok(new AuthResponse(jwt, authRequest.getLogin(), role));
    }

    /**
     * Валидация JWT токена.
     * <p>
     * Проверяет, является ли предоставленный JWT токен валидным:
     * <ul>
     *   <li>Корректный формат и структура токена</li>
     *   <li>Валидная цифровая подпись</li>
     *   <li>Срок действия не истек</li>
     *   <li>Пользователь существует в системе</li>
     * </ul>
     * Этот эндпоинт полезен для:
     * <ul>
     *   <li>Проверки токена перед использованием в клиентском приложении</li>
     *   <li>Отладки проблем с аутентификацией</li>
     *   <li>Интеграционного тестирования</li>
     * </ul>
     * </p>
     *
     * @param token JWT токен для валидации (в виде простой строки)
     * @return ResponseEntity с результатом валидации
     *
     * @example Пример успешной валидации
     * <pre>{@code
     * // Запрос:
     * POST /api/auth/validate
     * Content-Type: text/plain
     *
     * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * // Ответ (200 OK):
     * "Токен действителен"
     * }</pre>
     *
     * @example Пример невалидного токена
     * <pre>{@code
     * // Ответ (400 Bad Request):
     * "Недействительный токен"
     * }</pre>
     *
     * @example Пример ошибки валидации
     * <pre>{@code
     * // Ответ (400 Bad Request):
     * "Ошибка валидации токена"
     * }</pre>
     *
     * @see JwtService#validateToken(String, UserDetails)
     * @see JwtService#extractUsername(String)
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody String token) {
        try {
            // 1. Извлекаем имя пользователя из токена
            String username = jwtService.extractUsername(token);

            // 2. Загружаем данные пользователя для проверки
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 3. Проверяем валидность токена
            if (jwtService.validateToken(token, userDetails)) {
                return ResponseEntity.ok("Токен действителен");
            } else {
                return ResponseEntity.badRequest().body("Недействительный токен");
            }
        } catch (Exception e) {
            // Ловим все исключения (истекший токен, неверная подпись, пользователь не найден и т.д.)
            return ResponseEntity.badRequest().body("Ошибка валидации токена");
        }
    }
}

// ============================================================================
// ДОПОЛНИТЕЛЬНЫЕ ЭНДПОЙНТЫ И УЛУЧШЕНИЯ
// ============================================================================

/**
 * <h3>Пример расширенного AuthController с дополнительными эндпоинтами:</h3>
 * <pre>{@code
 * @PostMapping("/refresh")
 * public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
 *     // Реализация refresh токенов для продления сессии без перелогина
 *     String refreshToken = request.getRefreshToken();
 *
 *     if (jwtService.isRefreshTokenValid(refreshToken)) {
 *         String username = jwtService.extractUsernameFromRefreshToken(refreshToken);
 *         String newAccessToken = jwtService.generateToken(username);
 *
 *         return ResponseEntity.ok(new AuthResponse(newAccessToken, username));
 *     }
 *
 *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Невалидный refresh токен");
 * }
 *
 * @PostMapping("/logout")
 * public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
 *     // Реализация logout (добавление токена в blacklist)
 *     String token = authHeader.substring(7);
 *     tokenBlacklistService.blacklistToken(token);
 *
 *     return ResponseEntity.ok("Успешный выход из системы");
 * }
 *
 * @PostMapping("/change-password")
 * public ResponseEntity<?> changePassword(
 *         @AuthenticationPrincipal UserDetails userDetails,
 *         @Valid @RequestBody ChangePasswordRequest request) {
 *     // Смена пароля текущим пользователем
 *
 *     User user = userRepository.findByLogin(userDetails.getUsername())
 *             .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
 *
 *     // Проверяем старый пароль
 *     if (!PasswordHasher.checkPassword(request.getOldPassword(), user.getPasswordHash())) {
 *         return ResponseEntity.badRequest().body("Неверный текущий пароль");
 *     }
 *
 *     // Хешируем и сохраняем новый пароль
 *     String newPasswordHash = PasswordHasher.hashPassword(request.getNewPassword());
 *     user.setPasswordHash(newPasswordHash);
 *     userRepository.save(user);
 *
 *     return ResponseEntity.ok("Пароль успешно изменен");
 * }
 * }</pre>
 */

/**
 * <h3>Пример DTO для дополнительных операций:</h3>
 * <pre>{@code
 * // Для refresh токенов
 * public class RefreshTokenRequest {
 *     @NotBlank
 *     private String refreshToken;
 *
 *     // геттеры и сеттеры
 * }
 *
 * // Для смены пароля
 * public class ChangePasswordRequest {
 *     @NotBlank
 *     private String oldPassword;
 *
 *     @NotBlank
 *     @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
 *     private String newPassword;
 *
 *     @NotBlank
 *     private String confirmPassword;
 *
 *     // геттеры и сеттеры + валидация совпадения newPassword и confirmPassword
 * }
 * }</pre>
 */

/**
 * <h3>Рекомендации по безопасности для эндпоинтов аутентификации:</h3>
 * <ol>
 *   <li><strong>Rate Limiting:</strong> Ограничьте количество попыток входа (например, 5 попыток в минуту)
 *     <pre>{@code
 *     // Используйте Spring Security или отдельную библиотеку
 *     .antMatchers("/api/auth/login").access("@rateLimiter.canAccess(#request)")
 *     }</pre>
 *   </li>
 *
 *   <li><strong>Задержка при неудачных попытках:</strong> Добавьте небольшую задержку при неверных попытках входа</li>
 *
 *   <li><strong>Логирование:</strong> Логируйте все попытки входа (успешные и неуспешные) с IP адресом
 *     <pre>{@code
 *     logger.info("Успешный вход: {}, IP: {}", authRequest.getLogin(), request.getRemoteAddr());
 *     logger.warn("Неудачная попытка входа: {}, IP: {}", authRequest.getLogin(), request.getRemoteAddr());
 *     }</pre>
 *   </li>
 *
 *   <li><strong>Блокировка аккаунтов:</strong> Реализуйте временную блокировку после N неудачных попыток
 *     <pre>{@code
 *     if (user.getFailedLoginAttempts() >= 5) {
 *         user.setLockedUntil(Instant.now().plusMinutes(15));
 *         userRepository.save(user);
 *         return ResponseEntity.badRequest().body("Аккаунт временно заблокирован");
 *     }
 *     }</pre>
 *   </li>
 *
 *   <li><strong>HTTPS обязателен:</strong> Всегда используйте HTTPS в production</li>
 * </ol>
 */

/**
 * <h3>Пример обработки исключений для улучшения UX:</h3>
 * <pre>{@code
 * @ExceptionHandler(BadCredentialsException.class)
 * public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
 *     // Можно возвращать разные сообщения для разных ситуаций
 *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
 *             .body(Map.of(
 *                 "error": "invalid_credentials",
 *                 "message": "Неверный логин или пароль"
 *             ));
 * }
 *
 * @ExceptionHandler(DisabledException.class)
 * public ResponseEntity<?> handleDisabledUser(DisabledException ex) {
 *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
 *             .body(Map.of(
 *                 "error": "account_disabled",
 *                 "message": "Аккаунт отключен. Обратитесь к администратору"
 *             ));
 * }
 *
 * @ExceptionHandler(LockedException.class)
 * public ResponseEntity<?> handleLockedAccount(LockedException ex) {
 *     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
 *             .body(Map.of(
 *                 "error": "account_locked",
 *                 "message": "Аккаунт заблокирован. Попробуйте позже"
 *             ));
 * }
 * }</pre>
 */

/**
 * <h3>Поток данных при аутентификации:</h3>
 * <pre>
 * ┌───────────┐     1. Login Request      ┌──────────────┐
 * │           │ ────────────────────────> │              │
 * │   Client  │                           │ AuthController│
 * │           │ <──────────────────────── │              │
 * └───────────┘     6. JWT Response      └──────────────┘
 *                            │
 *                            ▼
 *                    ┌──────────────┐
 *                    │  Validation  │
 *                    │   Process    │
 *                    └──────────────┘
 *                     │     │     │
 *                     ▼     ▼     ▼
 *            ┌─────────┐ ┌─────┐ ┌─────┐
 *            │Check DB │ │Check│ │Spring│
 *            │for User │ │Password│ │Auth │
 *            └─────────┘ └─────┘ └─────┘
 *                     │     │     │
 *                     └─────┴─────┘
 *                            │
 *                            ▼
 *                    ┌──────────────┐
 *                    │ Generate JWT │
 *                    │   & Return   │
 *                    └──────────────┘
 * </pre>
 */

/**
 * <h3>Пример использования токена после аутентификации:</h3>
 * <pre>{@code
 * // 1. Клиент получает токен:
 * POST /api/auth/login → {"token": "eyJ...", "type": "Bearer"}
 *
 * // 2. Клиент использует токен для доступа к защищенным ресурсам:
 * GET /api/users/me
 * Authorization: Bearer eyJ...
 *
 * // 3. JwtAuthenticationFilter проверяет токен и устанавливает аутентификацию
 * // 4. Контроллер получает доступ к данным пользователя через @AuthenticationPrincipal
 * }</pre>
 */

/**
 * <h3>Тестирование эндпоинтов аутентификации:</h3>
 * <pre>{@code
 * @SpringBootTest
 * @AutoConfigureMockMvc
 * class AuthControllerTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @Test
 *     void testLogin_Success() throws Exception {
 *         mockMvc.perform(post("/api/auth/login")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content("{\"login\":\"admin\",\"password\":\"admin123\"}"))
 *                 .andExpect(status().isOk())
 *                 .andExpect(jsonPath("$.token").exists())
 *                 .andExpect(jsonPath("$.type").value("Bearer"));
 *     }
 *
 *     @Test
 *     void testLogin_InvalidCredentials() throws Exception {
 *         mockMvc.perform(post("/api/auth/login")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content("{\"login\":\"admin\",\"password\":\"wrong\"}"))
 *                 .andExpect(status().isBadRequest());
 *     }
 *
 *     @Test
 *     void testValidateToken_Valid() throws Exception {
 *         // Получаем токен
 *         String token = ...;
 *
 *         mockMvc.perform(post("/api/auth/validate")
 *                 .contentType(MediaType.TEXT_PLAIN)
 *                 .content(token))
 *                 .andExpect(status().isOk())
 *                 .andExpect(content().string("Токен действителен"));
 *     }
 * }
 * }</pre>
 */
