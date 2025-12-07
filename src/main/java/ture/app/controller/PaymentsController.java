package ture.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ture.app.dto.AccountDTO;
import ture.app.dto.TransactionDTO;
import ture.app.dto.UserDTO;
import ture.app.entity.Account;
import ture.app.service.AccountService;
import ture.app.service.TransactionService;
import ture.app.service.UserService;

import java.math.BigDecimal;

//  Swagger UI (интерактивный интерфейс) будет доступен по адресу:
//  http://localhost:8080/swagger-ui.html
//
//  OpenAPI JSON (спецификация в сыром виде) будет доступен по адресу:
//  http://localhost:8080/v3/api-docs
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Платежи и переводы", description = "API для работы с платежами и переводами")
public class PaymentsController {
    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Конвертирует строковую сумму в целое число (копейки/центы)
     * @param amount Строковое представление суммы
     * @return Сумма в минимальных единицах (копейках/центах)
     * curl -X 'POST' 'http://localhost:8080/api/payments' -H 'accept: application/json' -H 'Content-Type: application/json' -d '{"from_acc": {"id": 2},"to_acc": {"id": 1},"amount": "1.00"}'
     **/
    @Operation(summary = "Конвертация суммы",
            description = "Преобразует строковую сумму с плавающей точкой в целое число (копейки)",
            hidden = true) // Скрываем этот метод из документации API, так как он внутренний
    public Integer convertToInteger(String amount) {
        // Убираем возможные пробелы
        String cleaned = amount.trim();

        // Преобразуем в BigDecimal для точности
        BigDecimal decimal = new java.math.BigDecimal(cleaned);

        // Умножаем на 100 и преобразуем в Integer
        return decimal.multiply(new java.math.BigDecimal("100")).intValue();
    }

    @PostMapping
    @Operation(summary = "Создание платежа/перевода",
            description = "Выполняет перевод средств между счетами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Платеж успешно выполнен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверный запрос или недостаточно средств"),
            @ApiResponse(responseCode = "404",
                    description = "Один из счетов не найден"),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<TransactionDTO> payment(
            @Parameter(description = "Данные для выполнения перевода",
                    required = true,
                    schema = @Schema(implementation = TransactionDTO.class))
            @RequestBody TransactionDTO transactionDTO) {


        AccountDTO from_acc_ = transactionDTO.getFrom_acc();
        Account from_acc = accountService.getById(from_acc_.getId()).orElse(null);

        AccountDTO to_acc_ = transactionDTO.getTo_acc();
        Account to_acc = accountService.getById(to_acc_.getId()).orElse(null);

        var amount = convertToInteger(transactionDTO.getAmount());

        var tran = transactionService.create(from_acc, to_acc, amount);
        var tranDTO = new TransactionDTO(tran);
        tranDTO.getTo_acc().Clear();
        return ResponseEntity.ok(tranDTO);

    }
}
