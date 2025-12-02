package ture.app.controller;

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

    public Integer convertToInteger(String amount) {
        // Убираем возможные пробелы
        String cleaned = amount.trim();

        // Преобразуем в BigDecimal для точности
        BigDecimal decimal = new java.math.BigDecimal(cleaned);

        // Умножаем на 100 и преобразуем в Integer
        return decimal.multiply(new java.math.BigDecimal("100")).intValue();
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> paymant(@RequestBody TransactionDTO transactionDTO) {
        AccountDTO from_acc_ = transactionDTO.getFrom_acc();
        Account from_acc = accountService.getById(from_acc_.getId()).orElse(null);

        AccountDTO to_acc_   = transactionDTO.getTo_acc();
        Account to_acc = accountService.getById(from_acc_.getId()).orElse(null);

        var amount = convertToInteger(transactionDTO.getAmount());

        var tran = transactionService.create(from_acc, to_acc, amount);
        return ResponseEntity.ok(new TransactionDTO(tran));
    }
}
