package ture.bank.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Основной класс конфигурации безопасности Spring Security.
 * <p>Этот класс настраивает всю систему безопасности приложения, включая:
 * <ul>
 *   <li>Аутентификацию пользователей (JWT + база данных)</li>
 *   <li>Авторизацию доступа к эндпоинтам</li>
 *   <li>Настройку цепочки фильтров безопасности</li>
 *   <li>Конфигурацию кодировщика паролей</li>
 *   <li>Управление сессиями (stateless режим для REST API)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Архитектура безопасности:</strong>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    SecurityFilterChain                      │
 * ├─────────────────────────────────────────────────────────────┤
 * │ 1. JwtAuthenticationFilter (наш кастомный фильтр)           │
 * │ 2. UsernamePasswordAuthenticationFilter (Spring Security)   │
 * │ 3. AuthorizationFilter (проверка прав доступа)              │
 * │ 4. ExceptionTranslationFilter (обработка ошибок)            │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 * </p>
 *
 * @see EnableWebSecurity
 * @see EnableMethodSecurity
 * @see SecurityFilterChain
 * @see JwtAuthenticationFilter
 * @see UserDetailsService
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Кастомный фильтр для JWT аутентификации.
     * <p>Извлекает JWT токен из заголовка Authorization, проверяет его валидность
     * и устанавливает аутентификацию в SecurityContext.</p>
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Сервис для загрузки данных пользователей.
     * <p>Реализует интерфейс {@link UserDetailsService}, используется для
     * получения информации о пользователе из базы данных по имени пользователя.</p>
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Создает и настраивает кодировщик паролей.
     * <p>Используется для:
     * <ul>
     *   <li>Хеширования паролей при регистрации/смене пароля</li>
     *   <li>Проверки паролей при аутентификации</li>
     * </ul>
     * BCrypt - рекомендуемый алгоритм для хеширования паролей в Spring Security.
     * </p>
     *
     * @return экземпляр {@link BCryptPasswordEncoder}
     *
     * @example Пример использования
     * <pre>{@code
     * // Хеширование пароля:
     * String rawPassword = "myPassword123";
     * String encodedPassword = passwordEncoder().encode(rawPassword);
     * // Результат: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     *
     * // Проверка пароля:
     * boolean matches = passwordEncoder().matches(rawPassword, encodedPassword);
     * // Результат: true если пароли совпадают
     * }</pre>
     *
     * @see BCryptPasswordEncoder
     * @see PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает и настраивает провайдер аутентификации.
     * <p>{@link DaoAuthenticationProvider} - это реализация AuthenticationProvider,
     * которая использует UserDetailsService и PasswordEncoder для аутентификации
     * пользователей на основе данных из базы данных.</p>
     *
     * @return настроенный {@link DaoAuthenticationProvider}
     *
     * @see DaoAuthenticationProvider
     * @see UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // Устанавливаем кодировщик паролей
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Создает и возвращает менеджер аутентификации.
     * <p>{@link AuthenticationManager} - это центральный компонент Spring Security,
     * который управляет процессом аутентификации. Он использует настроенные
     * AuthenticationProvider'ы для проверки учетных данных.</p>
     *
     * @param config конфигурация аутентификации Spring Security
     * @return менеджер аутентификации
     * @throws Exception если возникает ошибка при создании менеджера
     *
     * @see AuthenticationManager
     * @see AuthenticationConfiguration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Создает и настраивает цепочку фильтров безопасности.
     * <p>Это основной метод конфигурации Spring Security, который определяет:
     * <ul>
     *   <li>Какие эндпоинты защищены, а какие публичны</li>
     *   <li>Как обрабатываются сессии (stateless для JWT)</li>
     *   <li>Какие фильтры используются и в каком порядке</li>
     *   <li>Настройки CSRF защиты</li>
     * </ul></p>
     *
     * <h3>Порядок фильтров в цепочке:</h3>
     * <ol>
     *   <li>{@link JwtAuthenticationFilter} - проверка JWT токенов</li>
     *   <li>{@link UsernamePasswordAuthenticationFilter} - стандартная аутентификация Spring</li>
     *   <li>Authorization filters - проверка прав доступа</li>
     *   <li>Exception handling filters - обработка исключений</li>
     * </ol>
     *
     * <h3>Настройки доступа к эндпоинтам:</h3>
     * <table border="1">
     *   <tr>
     *     <th>Путь/Метод</th>
     *     <th>Доступ</th>
     *     <th>Описание</th>
     *   </tr>
     *   <tr>
     *     <td>{@code /api/auth/**}</td>
     *     <td>PUBLIC</td>
     *     <td>Эндпоинты аутентификации (логин, регистрация)</td>
     *   </tr>
     *   <tr>
     *     <td>{@code /v3/api-docs/**}, {@code /swagger-ui/**}</td>
     *     <td>PUBLIC</td>
     *     <td>Документация OpenAPI/Swagger</td>
     *   </tr>
     *   <tr>
     *     <td>{@code GET /actuator/health}</td>
     *     <td>PUBLIC</td>
     *     <td>Health check для мониторинга</td>
     *   </tr>
     *   <tr>
     *     <td>{@code /api/admin/**}</td>
     *     <td>ROLE_ADMIN</td>
     *     <td>Административные функции</td>
     *   </tr>
     *   <tr>
     *     <td>{@code /**} (все остальные)</td>
     *     <td>AUTHENTICATED</td>
     *     <td>Требуется валидный JWT токен</td>
     *   </tr>
     * </table>
     *
     * @param http объект для настройки HTTP безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если возникает ошибка при настройке
     *
     * @example Пример HTTP запросов
     * <pre>{@code
     * // Публичный доступ (работает без токена):
     * POST /api/auth/login
     * GET /swagger-ui/index.html
     * GET /actuator/health
     *
     * // Требуется аутентификация:
     * GET /api/users/me          → Нужен валидный JWT токен
     * POST /api/transactions     → Нужен валидный JWT токен
     *
     * // Требуется роль ADMIN:
     * GET /api/admin/users       → Нужен JWT токен с ролью ADMIN
     * DELETE /api/admin/users/1  → Нужен JWT токен с ролью ADMIN
     * }</pre>
     *
     * @see HttpSecurity
     * @see SecurityFilterChain
     * @see AbstractHttpConfigurer
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF защиту (не нужна для REST API с JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Настраиваем управление сессиями
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Настраиваем авторизацию запросов
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (не требуют аутентификации)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Документация API
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Health check для мониторинга
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Административные эндпоинты (требуют роль ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Регистрируем наш провайдер аутентификации
                .authenticationProvider(authenticationProvider())

                // Добавляем JWT фильтр перед стандартным фильтром аутентификации
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

// ============================================================================
// ДОПОЛНИТЕЛЬНАЯ КОНФИГУРАЦИЯ И ПРИМЕРЫ
// ============================================================================

/**
 * <h3>Расширенная конфигурация CORS (если нужна):</h3>
 * <pre>{@code
 * @Bean
 * public CorsConfigurationSource corsConfigurationSource() {
 *     CorsConfiguration configuration = new CorsConfiguration();
 *     configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
 *     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
 *     configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
 *     configuration.setExposedHeaders(Arrays.asList("Authorization"));
 *     configuration.setAllowCredentials(true);
 *     configuration.setMaxAge(3600L);
 *
 *     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
 *     source.registerCorsConfiguration("/**", configuration);
 *     return source;
 * }
 *
 * // Затем в securityFilterChain добавить:
 * .cors(cors -> cors.configurationSource(corsConfigurationSource()))
 * }</pre>
 */

/**
 * <h3>Пример конфигурации для разных профилей:</h3>
 * <pre>{@code
 * @Profile("dev")
 * @Bean
 * public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
 *     // В разработке разрешаем больше публичных эндпоинтов
 *     return http
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/**").permitAll()  // В разработке всё публично
 *         )
 *         .build();
 * }
 *
 * @Profile("prod")
 * @Bean
 * public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
 *     // В production строгая безопасность
 *     return http
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/api/auth/**").permitAll()
 *             .anyRequest().authenticated()
 *         )
 *         .build();
 * }
 * }</pre>
 */

/**
 * <h3>Пример настройки исключений и обработчиков ошибок:</h3>
 * <pre>{@code
 * @Bean
 * public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *     return http
 *         .exceptionHandling(exception -> exception
 *             .authenticationEntryPoint((request, response, authException) -> {
 *                 // Обработка ошибок аутентификации (401)
 *                 response.setStatus(HttpStatus.UNAUTHORIZED.value());
 *                 response.setContentType(MediaType.APPLICATION_JSON_VALUE);
 *                 response.getWriter().write("{\"error\": \"Требуется аутентификация\"}");
 *             })
 *             .accessDeniedHandler((request, response, accessDeniedException) -> {
 *                 // Обработка ошибок авторизации (403)
 *                 response.setStatus(HttpStatus.FORBIDDEN.value());
 *                 response.setContentType(MediaType.APPLICATION_JSON_VALUE);
 *                 response.getWriter().write("{\"error\": \"Недостаточно прав\"}");
 *             })
 *         )
 *         .build();
 * }
 * }</pre>
 */

/**
 * <h3>Пример интеграции с Actuator для мониторинга:</h3>
 * <pre>{@code
 * .authorizeHttpRequests(auth -> auth
 *     .requestMatchers("/actuator/health").permitAll()
 *     .requestMatchers("/actuator/info").permitAll()
 *     .requestMatchers("/actuator/metrics").hasRole("ADMIN")
 *     .requestMatchers("/actuator/**").hasRole("ADMIN")
 * )
 * }</pre>
 */

/**
 * <h3>Рекомендации по безопасности для production:</h3>
 * <ol>
 *   <li><strong>HTTPS обязателен:</strong> Всегда используйте HTTPS в production</li>
 *   <li><strong>Заголовки безопасности:</strong> Добавьте Security Headers:
 *     <pre>{@code
 *     .headers(headers -> headers
 *         .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
 *         .frameOptions(frame -> frame.sameOrigin())
 *         .httpStrictTransportSecurity(hsts -> hsts
 *             .includeSubDomains(true)
 *             .maxAgeInSeconds(31536000)
 *         )
 *     )
 *     }</pre>
 *   </li>
 *   <li><strong>Время жизни токенов:</strong> Установите короткое время жизни access токенов (15-60 мин)</li>
 *   <li><strong>Rate limiting:</strong> Добавьте ограничение запросов для /api/auth/login</li>
 *   <li><strong>Логирование:</strong> Настройте логирование попыток доступа и ошибок аутентификации</li>
 *   <li><strong>Аудит:</strong> Реализуйте аудит важных операций (логин, изменение прав, etc.)</li>
 * </ol>
 */

/**
 * <h3>Пример аннотаций для методов контроллера:</h3>
 * <pre>{@code
 * // В контроллерах можно использовать аннотации для проверки прав:
 * @RestController
 * public class UserController {
 *
 *     @GetMapping("/users/me")
 *     @PreAuthorize("isAuthenticated()")  // Любой аутентифицированный пользователь
 *     public User getCurrentUser() { ... }
 *
 *     @GetMapping("/admin/users")
 *     @PreAuthorize("hasRole('ADMIN')")  // Только ADMIN
 *     public List<User> getAllUsers() { ... }
 *
 *     @GetMapping("/users/{id}")
 *     @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
 *     public User getUserById(@PathVariable Long id) { ... }
 * }
 * }</pre>
 */

/**
 * <h3>Поток работы безопасности:</h3>
 * <pre>
 * 1. Клиент → HTTP запрос → Spring Security Filter Chain
 * 2. JwtAuthenticationFilter проверяет заголовок Authorization
 * 3. Если токен валиден → устанавливает Authentication в SecurityContext
 * 4. AuthorizationFilter проверяет права доступа к запрашиваемому эндпоинту
 * 5. Если доступ разрешен → запрос передается в контроллер
 * 6. Если доступ запрещен → выбрасывается AccessDeniedException
 * 7. ExceptionTranslationFilter обрабатывает исключения и возвращает соответствующий HTTP статус
 * </pre>
 */