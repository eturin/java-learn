package ture.app.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ture.app.entity.Account;
import ture.app.entity.User;
import ture.app.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// Создаем класс UserService, который будет содержать основную логику работы с пользователями.
// Для чего это нужно:
// - Service слой содержит бизнес-логику приложения
// - Отделяет контроллеры (которые принимают запросы) от репозиториев (которые работают с БД)
// - Здесь мы можем добавлять проверки, преобразования данных и другую логику
@Service
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    // Создание нового пользователя
    public User createUser(String username, String email) {
        logger.info(String.format("Creating new user '%s' with email '%s'", username, email));
        User user = new User(username, email);
        return userRepository.save(user);
    }
    // Получение всех пользователей
    public List<User> getAllUsers() {
        logger.info(String.format("Retrieving all users."));
        return userRepository.findAll();
    }
    // Получение пользователя по ID
    public Optional<User> getUserById(Long id) {
        logger.info(String.format("Retrieving user with id '%s'", id));
        return userRepository.findById(id);
    }
    // Получение пользователя по имени
    public Optional<User> getUserByName(String name) {
        logger.info(String.format("Retrieving user with name '%s'", name));
        return userRepository.findByName(name);
    }
    // Обновление имени пользователя
    public User updateUser(Long id, String name) {
        logger.info(String.format("Updating user with id '%s', new name is '%s'", id, name));
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User usr = user.get();
            usr.setName(name);
            return userRepository.save(usr);
        }
        return null;
    }
    // Получение пользователя по email
    public Optional<User> getUserByEmail(String email) {
        logger.info(String.format("Retrieving user with email '%s'", email));
        return userRepository.findByEmail(email);
    }
    // Обновление email пользователя
    public User updateUserEmail(Long id, String email) {
        logger.info(String.format("Updating user with id '%s', new email is '%s'", id, email));
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User usr = user.get();
            usr.setEmail(email);
            return userRepository.save(usr);
        }
        return null;
    }
    // Удаление пользователя
    public void deleteUserById(Long id) {
        logger.warn(String.format("Deleting user with id '%s'", id));
        userRepository.deleteById(id);
    }
    // Получение списка счетов
    public Map<String,Account> getAccounts(Long id) {
        logger.info(String.format("Retrieving user`a accounts with id '%s'", id));
        var user = userRepository.findById(id);
        return user.map(User::getAccounts).orElse(null);
    }

}
