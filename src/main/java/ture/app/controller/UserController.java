package ture.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ture.app.dto.AccountDTO;
import ture.app.dto.UserDTO;
import ture.app.entity.User;
import ture.app.service.AccountService;
import ture.app.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Создаем класс UserController с методами для обработки HTTP запросов.
// Для чего это нужно:
// - Предоставляем API для внешних клиентов (веб, мобильные приложения)
// - Обрабатываем HTTP запросы (GET, POST, PUT, DELETE)
// - Преобразуем JSON в Java объекты и обратно
//
//  Swagger UI (интерактивный интерфейс) будет доступен по адресу:
//  http://localhost:8080/swagger-ui.html
//
//  OpenAPI JSON (спецификация в сыром виде) будет доступен по адресу:
//  http://localhost:8080/v3/api-docs
@RestController
@RequestMapping("/api/users")
@Tag(name = "Управление пользователями", description = "API для работы с пользователями и их счетами")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    // GET /api/users - получить всех пользователей
    // curl http://127.0.0.1:8080/api/users
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей системы.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный запрос")
    })
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    // GET /api/users/{id} - получить пользователя по ID
    // curl http://127.0.0.1:8080/api/users/1
    @Operation(summary = "Получить пользователя по ID", description = "Ищет и возвращает данные пользователя по его уникальному идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(UserDTO::new)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users/by-name/{name} - получить пользователя по имени
    // curl http://127.0.0.1:8080/api/users/by-name/ture
    @Operation(summary = "Получить пользователя по имени", description = "Ищет и возвращает данные пользователя по его имени.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным имененм не найден")
    })
    @GetMapping("/by-name/{name}")
    public ResponseEntity<UserDTO> getUserByName(@PathVariable String name) {
        Optional<User> user = userService.getUserByName(name);
        return user.map(UserDTO::new)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users/by-email/{email} - получить пользователя по email
    // curl http://127.0.0.1:8080/api/users/by-email/eturin@gmail.com
    @Operation(summary = "Получить пользователя по email", description = "Ищет и возвращает данные пользователя по его электронной почте.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанной электронной почтой не найден")
    })
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(UserDTO::new)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users - создать нового пользователя
    // curl -X POST http://localhost:8080/api/users   -H "Content-Type: application/json"  -d '{"name":"ture","email":"eturin@gmail.com"}'
    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя с указанными именем и email"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные пользователя"
            )
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для создания пользователя",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            description = "Объект пользователя",
                            requiredProperties = {"name", "email"},
                            example = """
                    {
                      "name": "ture",
                      "email": "eturin@gmail.com"
                    }
                    """
                    )
            )
    ) @RequestBody User user) {
        var usr = userService.createUser(user.getName(), user.getEmail());
        return  ResponseEntity.ok(new UserDTO(usr));
    }

    // PUT /api/users/{id}/name - обновить имя пользователя
    // curl -X PUT http://localhost:8080/api/users/2/name   -H "Content-Type: application/json"  -d 'Бла-бла'
    @PutMapping("/{id}/name")
    public ResponseEntity<UserDTO> updateUserName(@PathVariable Long id, @RequestBody User user) {
        if ( !user.getName().isEmpty()) {
            var u = userService.updateUser(id, user.getName());
            if(u != null) {
                return ResponseEntity.ok(new UserDTO(u));
            }
        }
        return ResponseEntity.notFound().build();
    }

    // PUT /api/users/{id}/email - обновить email пользователя
    // curl -X POST http://localhost:8080/api/users/2/email   -H "Content-Type: application/json"  -d 'Бла@бла.БЛА'
    @PutMapping("/{id}/email")
    public ResponseEntity<UserDTO> updateUserEmail(@PathVariable Long id, @RequestBody User user) {
        if ( !user.getName().isEmpty()) {
            var u = userService.updateUser(id, user.getEmail());
            if(u != null) {
                return ResponseEntity.ok(new UserDTO(u));
            }
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE /api/users/{id} - удалить пользователя
    // // curl -X DELETE http://localhost:8080/api/users/2
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    // GET /api/users/{id}/accounts
    // curl http://127.0.0.1:8080/api/users/1/accounts
    @GetMapping("/{id}/accounts")
    public ResponseEntity<List<AccountDTO>> getUserAccounts(@PathVariable Long id) {
        var accounts = userService.getAccounts(id);
        var list = accounts.values()
                           .stream()
                           .map(AccountDTO::new)
                           .toList();
        return ResponseEntity.ok(list);
    }

    // PUT /api/users/{id}/accounts/{id_acc}/name
    // curl -X PUT http://127.0.0.1:8080/api/users/1/accounts/1/name -H "Content-Type: application/json"  -d "Основной расчётный счёт"
    @PutMapping("/{id}/accounts/{id_acc}/name")
    public ResponseEntity<Boolean> setAccountName(@PathVariable("id") Long id,
                                                  @PathVariable("id_acc")  Long id_acc,
                                                  @RequestBody String name) {
        var res = accountService.setAccountName(id,id_acc,name);
        return ResponseEntity.ok(res);
    }

    // POST /api/users/{id}/accounts
    // curl -X POST http://localhost:8080/api/users/1/accounts   -H "Content-Type: application/json"  -d 'Брокерский счёт 1'
    @Operation(summary = "Создать новый счет для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет успешно создан"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/{id}/accounts")
    public ResponseEntity<AccountDTO> createUserAccount(@PathVariable Long id, @RequestBody String name) {
        var account =  accountService.create(id, name);
        return ResponseEntity.ok(new AccountDTO(account));
    }
}
