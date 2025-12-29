package ture.bank.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ture.bank.config.JwtProperties;
import ture.bank.dto.ChangePasswordRequest;
import ture.bank.security.JwtAuthenticationFilter;
import ture.bank.security.SecurityConfig;
import ture.bank.service.JwtService;
import ture.bank.dto.AuthRequest;
import ture.bank.dto.AuthResponse;
import ture.bank.entity.User;
import ture.bank.repository.UserRepository;
import ture.bank.util.PasswordHasher;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер для операций аутентификации и управления JWT токенами.
 * <p>Этот контроллер предоставляет REST API эндпоинты для:
 * <ul>
 *   <li>Аутентификации пользователей (логин) с получением JWT токена</li>
 *   <li>Валидации существующих JWT токенов</li>
 * </ul>
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
@Tag(name = "Аутентификация", description = "Операции аутентификации и управления JWT токенами")
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
     * Конфигурационные свойства JWT.
     */
    @Autowired
    private JwtProperties jwtProperties;

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
    @Operation(
            summary = "Аутентификация пользователя",
            description = """
                    Аутентификация пользователя по логину и паролю.
                    При успешной аутентификации возвращает JWT токен.
                    
                    ### Тестовые учетные данные:
                    - **admin** / admin123 (роль: ADMIN)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный логин или пароль",
                    content = @Content(mediaType = "text/plain")
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Parameter(
                    description = "Данные для аутентификации",
                    required = true,
                    schema = @Schema(implementation = AuthRequest.class)
            )
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletResponse response) {
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

        // 8. Устанавливаем токен в HTTP-only cookie
        setJwtCookie(response, jwt, Duration.ofMillis(jwtProperties.getExpiration()));

        // 9. Возвращаем успешный ответ с токеном
        return ResponseEntity.ok(new AuthResponse(jwt, authRequest.getLogin(), role));
    }

    /**
     * Обновление JWT токена.
     * <p>Проверяет текущий токен из cookie и выдает новый токен
     * без необходимости повторной аутентификации.</p>
     *
     * @param jwtToken текущий JWT токен из cookie
     * @param response HTTP ответ для установки нового cookie
     * @return ResponseEntity с результатом обновления
     */
    @Operation(
            summary = "Обновление JWT токена",
            description = "Обновляет JWT токен из cookie без повторной аутентификации"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токен успешно обновлен",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Не удалось обновить токен",
                    content = @Content(mediaType = "text/plain")
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = JwtAuthenticationFilter.JWT_COOKIE_NAME, required = false) String jwtToken,
            HttpServletResponse response) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Токен отсутствует в cookie");
        }

        try {
            // 1. Извлекаем имя пользователя из токена
            String username = jwtService.extractUsername(jwtToken);

            // 2. Загружаем данные пользователя для проверки
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 3. Проверяем валидность текущего токена
            if (jwtService.validateToken(jwtToken, userDetails)) {
                // 4. Генерируем новый токен
                String newJwt = jwtService.generateToken(username);

                // 5. Устанавливаем новый токен в cookie
                setJwtCookie(response, newJwt, Duration.ofMillis(jwtProperties.getExpiration()));

                // 6. Возвращаем информацию об обновлении
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "username", username,
                        "message", "Токен успешно обновлен"
                ));
            } else {
                return ResponseEntity.badRequest().body("Текущий токен недействителен");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка обновления токена: " + e.getMessage());
        }
    }

    /**
     * Выход из системы (logout).
     * <p>Удаляет JWT токен из cookie, делая его недействительным.</p>
     *
     * @param response HTTP ответ для очистки cookie
     * @return ResponseEntity с сообщением об успешном выходе
     */
    @Operation(
            summary = "Выход из системы",
            description = "Удаляет JWT токен из cookie, завершая сессию пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный выход из системы",
                    content = @Content(mediaType = "text/plain")
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Удаляем cookie, устанавливая его с нулевым временем жизни
        setJwtCookie(response, "", Duration.ofMillis(0));
        return ResponseEntity.ok("Успешный выход из системы");
    }

    /**
     * Устанавливает JWT токен в HTTP-only cookie.
     *
     * @param response HTTP ответ
     * @param jwtToken JWT токен
     */
    private void setJwtCookie(HttpServletResponse response, String jwtToken, Duration age) {
        // Создаем cookie с настройками безопасности
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.JWT_COOKIE_NAME, jwtToken)
                .httpOnly(true)          // Защита от XSS атак
                .secure(true)            // Только по HTTPS (в production)
                .path("/")               // Доступно для всех путей
                .maxAge(age)
                .sameSite("Strict")      // Защита от CSRF атак
                .build();

        // Добавляем cookie в заголовки ответа
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
    @Operation(
            summary = "Проверка валидности JWT токена",
            description = "Проверяет, является ли предоставленный JWT токен валидным"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токен действителен",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Токен недействителен или произошла ошибка валидации",
                    content = @Content(mediaType = "text/plain")
            )
    })
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(
            @Parameter(
                    description = "JWT токен для проверки",
                    required = true,
                    schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            )
            @RequestBody String token) {
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

    /**
     * Смена пароля текущего пользователя.
     * <p>Позволяет аутентифицированному пользователю изменить свой пароль.
     * Для успешной смены пароля необходимо:
     * <ul>
     *   <li>Указать текущий (старый) пароль</li>
     *   <li>Указать новый пароль (не менее 6 символов)</li>
     *   <li>Подтвердить новый пароль</li>
     *   <li>Новый пароль не должен совпадать со старым</li>
     * </ul>
     * </p>
     *
     * @param changePasswordRequest объект с данными для смены пароля
     * @return ResponseEntity с результатом операции
     *
     * @example Пример успешной смены пароля
     * <pre>{@code
     * // Запрос:
     * POST /api/auth/change-password
     * Authorization: Bearer {jwt_token}
     * Content-Type: application/json
     *
     * {
     *   "oldPassword": "admin123",
     *   "newPassword": "newAdmin123",
     *   "confirmPassword": "newAdmin123"
     * }
     *
     * // Ответ (200 OK):
     * "Пароль успешно изменен"
     * }</pre>
     *
     * @example Пример ошибок
     * <pre>{@code
     * // Неверный текущий пароль (400 Bad Request):
     * "Неверный текущий пароль"
     *
     * // Новый пароль совпадает со старым (400 Bad Request):
     * "Новый пароль не должен совпадать с текущим"
     *
     * // Пароли не совпадают (400 Bad Request):
     * "Новый пароль и подтверждение не совпадают"
     * }</pre>
     *
     * @see ChangePasswordRequest
     * @see org.springframework.security.core.annotation.AuthenticationPrincipal
     */
    @Operation(
            summary = "Смена пароля текущего пользователя",
            description = """
                    Позволяет аутентифицированному пользователю изменить свой пароль.
                    
                    ### Требования:
                    - Должен быть аутентифицирован (JWT токен)
                    - Текущий пароль должен быть верным
                    - Новый пароль: не менее 6 символов
                    - Новый пароль и подтверждение должны совпадать
                    - Новый пароль не должен совпадать со старым
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменен",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или неверные данные",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = "text/plain")
            )
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Parameter(
                    description = "Данные для смены пароля",
                    required = true,
                    schema = @Schema(implementation = ChangePasswordRequest.class)
            )
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            Authentication authentication) {

        /// Проверяем, аутентифицирован ли пользователь
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Пользователь не аутентифицирован");
        }

        // Получаем имя пользователя из Authentication
        String username = authentication.getName();

        // Ищем пользователя в базе данных
        Optional<User> userOptional = userRepository.findByLogin(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Пользователь не найден");
        }

        User user = userOptional.get();

        // 1. Проверяем текущий пароль
        if (!PasswordHasher.checkPassword(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Неверный текущий пароль");
        }

        // 2. Проверяем, что новый пароль не совпадает со старым
        if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            return ResponseEntity.badRequest().body("Новый пароль не должен совпадать с текущим");
        }

        // 3. Проверяем, что новый пароль и подтверждение совпадают
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Новый пароль и подтверждение не совпадают");
        }

        // 4. Хешируем новый пароль
        String newPasswordHash = PasswordHasher.hashPassword(changePasswordRequest.getNewPassword());

        // 5. Обновляем пароль пользователя
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        // 6. Возвращаем успешный ответ
        return ResponseEntity.ok("Пароль успешно изменен");
    }
}

// ============================================================================
// ДОПОЛНИТЕЛЬНЫЕ ЭНДПОЙНТЫ И УЛУЧШЕНИЯ
// ============================================================================




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

