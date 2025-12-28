package ture.app.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ture.app.service.UserService;

// Создадим простой компонент, который автоматически добавит тестовых пользователей при запуске приложения.
// Для чего это нужно:
// - Чтобы убедиться, что всё работает правильно - от создания таблицы до сохранения и чтения данных.
@Component
public class TestDataRunner implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        /*System.out.println("Начало тестирования");
        var usr1 = userService.createUser("ture", "eturin@gmail.com");
        var usr2 = userService.createUser("aaa", "aaa@gmail.com");
        System.out.printf("Созданы пользователи:\n%s\n%s\n", usr1, usr2);

        var l = userService.getAllUsers();
        System.out.printf("Всего пользователей в базе: %s\n", l.size());
        for (var user : l) {
            System.out.println(user);
        }

        var usr3 = userService.getUserByName("ture");
        if (usr3.isPresent()) {
            System.out.printf("Найден пользователь по имени: %s\n", usr3.get());
        }
        System.out.println("Тестирование завершено");*/
    }
}
