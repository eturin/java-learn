package ture.bank.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ture.bank.entity.User;
import ture.bank.repository.UserRepository;

import java.util.Collections;

/**
 * Реализация сервиса {@link UserDetailsService} для загрузки данных пользователей
 * из базы данных в контекст Spring Security.
 * <p>Этот сервис является мостом между предметной областью (сущность {@link User})
 * и Spring Security Framework. Он преобразует бизнес-сущность пользователя в объект,
 * который понимает Spring Security для целей аутентификации и авторизации.</p>
 * <p>
 * <strong>Роль в архитектуре Spring Security:</strong>
 * <pre>
 * Запрос на аутентификация → AuthenticationManager → UserDetailsService → БД
 *                                                      ↓
 *                                             UserDetails (Spring Security User)
 * </pre>
 * </p>
 *
 * <h3>Жизненный цикл вызова:</h3>
 * <ol>
 *   <li>Пользователь отправляет логин/пароль на аутентификацию</li>
 *   <li>Spring Security вызывает метод получения пользователя по логину</li>
 *   <li>Сервис загружает пользователя из БД и преобразует в {@link UserDetails}</li>
 *   <li>Spring Security сравнивает пароли и создает {@link Authentication} объект</li>
 * </ol>
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see org.springframework.security.core.userdetails.User
 * @see ture.bank.entity.User
 * @see ture.bank.repository.UserRepository
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    /**
     * Репозиторий для доступа к данным пользователей в базе данных.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Загружает данные пользователя по имени пользователя (логину).
     * <p>Этот метод является центральным в процессе аутентификации Spring Security.
     * Он вызывается каждый раз, когда система пытается аутентифицировать пользователя.</p>
     *
     * <h3>Процесс выполнения:</h3>
     * <ol>
     *   <li>Поиск пользователя в БД по логину</li>
     *   <li>Проверка, не удален ли пользователь (soft delete)</li>
     *   <li>Преобразование в объект Spring Security {@link User}</li>
     *   <li>Назначение ролей/прав (authorities)</li>
     * </ol>
     *
     * <h3>Структура возвращаемого объекта:</h3>
     * <pre>{@code
     * UserDetails {
     *   username: "admin",           // логин пользователя
     *   password: "hashed_password", // хеш пароля из БД
     *   authorities: ["ROLE_ADMIN"], // список ролей/прав
     *   accountNonExpired: true,     // аккаунт не истек
     *   accountNonLocked: true,      // аккаунт не заблокирован
     *   credentialsNonExpired: true, // пароль не истек
     *   enabled: true                // аккаунт включен
     * }
     * }</pre>
     *
     * @param username логин пользователя для поиска (не чувствителен к регистру в зависимости от БД)
     * @return объект {@link UserDetails}, содержащий информацию для аутентификации
     * @throws UsernameNotFoundException если пользователь не найден или удален
     *
     * @see UsernameNotFoundException
     * @see org.springframework.security.core.userdetails.User
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Поиск пользователя в базе данных по логину
        User user = userRepository.findByLogin(username).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // Проверка soft delete (мягкого удаления)
        if (user.isDeleted()) {
            throw new UsernameNotFoundException("Пользователь удален: " + username);
        }

        // Преобразование бизнес-сущности User в Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),                     // username для Spring Security
                user.getPasswordHash(),              // хешированный пароль для проверки
                Collections.singletonList(           // список прав/ролей
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
                )
        );
    }
}
