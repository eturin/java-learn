package ture.bank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger для документации REST API.
 * <p>Этот класс настраивает автоматическую генерацию документации API
 * на основе аннотаций в контроллерах и DTO.</p>
 *
 * <h3>Доступные URL после запуска:</h3>
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></li>
 *   <li>OpenAPI YAML: <a href="http://localhost:8080/v3/api-docs.yaml">http://localhost:8080/v3/api-docs.yaml</a></li>
 * </ul>
 *
 * <h3>Интеграция с JWT аутентификацией:</h3>
 * <p>Добавлена кнопка "Authorize" в Swagger UI для ввода JWT токена.
 * Формат: Bearer {ваш_токен}</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Application API",
                version = "1.0.0",
                description = """
                        REST API для банковского приложения.
                        
                        ## Основные возможности:
                        - Аутентификация и авторизация пользователей (JWT)
                        - Управление пользователями
                        - Ролевая модель доступа (USER, ADMIN)
                        
                        ## Аутентификация:
                        Для доступа к защищенным эндпоинтам необходимо:
                        1. Выполнить запрос на `/api/auth/login` для получения JWT токена
                        2. Использовать полученный токен в заголовке Authorization:
                           `Bearer {ваш_токен}`
                        
                        ## Тестовые пользователи:
                        - **admin** / admin123 (роль: ADMIN)
                        """,
                contact = @Contact(
                        name = "Bank Support",
                        email = "support@bank.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Локальный сервер разработки"
                ),
                @Server(
                        url = "https://api.bank.com",
                        description = "Production сервер"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT аутентификация. Получите токен через /api/auth/login"
)
public class SwaggerConfig {
}