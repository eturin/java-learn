package ture.bank.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Вспоиогательный класс для работы с паролями
 */
public class PasswordHasher {

    /**
     * Генерация хеша пароля
     * @param plainPassword исходный пароль
     * @return хеш пароля для хранения в БД
     */
    public static String hashPassword(String plainPassword) {
        // Генерируем соль и хеш автоматически
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Проверка пароля
     * @param plainPassword пароль для проверки
     * @param hashedPassword хеш из БД
     * @return true если пароль верный
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}