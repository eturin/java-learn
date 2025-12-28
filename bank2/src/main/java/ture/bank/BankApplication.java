package ture.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной класс Spring Boot приложения "Bank".
 * <p>
 * Этот класс запускает Spring Boot приложение и инициализирует все компоненты.
 * Приложение предоставляет REST API для управления операциями между картами,
 * пользователями и ролями с использованием PostgreSQL и Flyway для миграций.
 * </p>
 *
 */
@SpringBootApplication
public class BankApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankApplication.class, args);
	}

}
