package ture.app.service.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ture.app.dto.AccountDTO;
import ture.app.entity.Account;
import ture.app.service.AccountService;
import ture.app.service.TransactionService;
import ture.app.transactions.PaymentRequest;
import ture.app.transactions.PaymentResponse;
import ture.app.transactions.PaymentStatus;
import ture.app.transactions.TransactionsServiceGrpc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@GrpcService
public class TransactionsGrpcService extends TransactionsServiceGrpc.TransactionsServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(TransactionsGrpcService.class);

    public Integer convertToInteger(String amount) {
        // Убираем возможные пробелы
        String cleaned = amount.trim();

        // Преобразуем в BigDecimal для точности
        BigDecimal decimal = new java.math.BigDecimal(cleaned);

        // Умножаем на 100 и преобразуем в Integer
        return decimal.multiply(new java.math.BigDecimal("100")).intValue();
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Override
    public void processPayment(PaymentRequest request,
                               StreamObserver<PaymentResponse> responseObserver) {
        logger.info("gRPC Pyment request received: {} -> {} = {}",
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount());

        try {
            var fromAccID = request.getFromAccountId();
            var toAccID =request.getToAccountId();
            var amount = convertToInteger(request.getAmount());

            var tran = transactionService.create(fromAccID, toAccID, amount);

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setId(tran.getId())
                    .setFromAccountId(fromAccID)
                    .setToAccountId(toAccID)
                    .setAmount(String.format("%.2f", tran.getAmount()/100.0))
                    .setStatus(PaymentStatus.COMPLETED)
                    .setCreatedAt(tran.getCreatedAt().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("gRPC Payment response sent");
        } catch (Exception e) {

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setId(0)
                    .setFromAccountId(request.getFromAccountId())
                    .setToAccountId(request.getToAccountId())
                    .setAmount(request.getAmount())
                    .setStatus(PaymentStatus.FAILED)
                    .setErrorMessage(e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info("gRPC Payment err: {}", e.getMessage());
        }
    }
}








