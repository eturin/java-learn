package ture.bank.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ture.bank.dto.RoleFilter;
import ture.bank.dto.StringFilter;
import ture.bank.dto.UserSearchCriteria;
import ture.bank.entity.Role;
import ture.bank.entity.User;
import ture.bank.repository.specification.UserSpecifications;
import ture.bank.util.PasswordHasher;

import java.util.List;
import java.util.Optional;

@SpringBootTest          // Загружает полный Spring контекст
@ActiveProfiles("test")  // Использует test профиль
@Transactional           // Выполнять в транзакции
public class UserRepositoryTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private String login = "test";
    private String pwd = "test123";
    private String fio = "test";
    private String roleName = "USER";

    @Test
    void newUser() {
        var user = new User();

        var pwdHash = PasswordHasher.hashPassword(pwd);

        Optional<Role> foundRole = roleRepository.findByName(roleName);
        assertThat(foundRole).isPresent();
        var role = foundRole.get();

        user = new User(login, fio, role, pwdHash);
        assertThat(user.getLogin()).isEqualTo(login);
        assertThat(user.getFio()).isEqualTo(fio);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getPasswordHash()).isEqualTo(pwdHash);

        user = userRepository.save(user);
        assertThat(user.getId()).isNotZero();
        assertThat(user.getLogin()).isEqualTo(login);
        assertThat(user.getFio()).isEqualTo(fio);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getPasswordHash()).isEqualTo(pwdHash);
    }

    @Test
    void updateUser() {
        var pwdHash = PasswordHasher.hashPassword(pwd);

        Optional<Role> foundRole = roleRepository.findByName(roleName);
        assertThat(foundRole).isPresent();
        var role = foundRole.get();

        var user = new User(login, fio, role, pwdHash);
        assertThat(user.getUpdatedAt()).isNull();
        user = userRepository.save(user);

        // Фиксируем изменения в БД
        entityManager.flush();
        // удаление из кэш JPA
        //entityManager.detach(user);


        //var foundUser = userRepository.findByLogin(login);
        //assertThat(foundUser).isPresent();
        //user = foundUser.get();
        assertThat(user.getFio()).isEqualTo(fio);
        assertThat(user.getUpdatedAt()).isNull();
        pwdHash = user.getPasswordHash();
        assertThat(PasswordHasher.checkPassword(pwd, pwdHash)).isTrue();
        user.setFio("test-test");
        user = userRepository.save(user);

        // сбрасываем в базу накопленные команды
        entityManager.flush();
        // перечитываем объект из базы
        entityManager.refresh(user);

        assertThat(user.getFio()).isEqualTo("test-test");
        assertThat(user.getUpdatedAt()).isNotNull();


        foundRole = roleRepository.findByName("ADMIN");
        assertThat(foundRole).isPresent();
        role = foundRole.get();
        user.setRole(role);
        userRepository.save(user);

        // сбрасываем в базу накопленные команды
        entityManager.flush();

        var foundUser = userRepository.findByLogin(login);
        assertThat(foundUser).isPresent();
        user = foundUser.get();
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    void pageUser() {
        Optional<Role> foundRole = roleRepository.findByName(roleName);
        assertThat(foundRole).isPresent();
        var role = foundRole.get();

        var total = 100;
        var pageSize = 10;

        for (var i = 0; i < total; i++) {
            var pwdHash = PasswordHasher.hashPassword(pwd + i);

            var user = new User(login + i, fio + i, role, pwdHash);
            userRepository.save(user);
        }

        Sort sort = Sort.by(Sort.Direction.fromString("DESC"), "login");
        for (var page = 0; page < total / pageSize; page++) {
            Pageable pageable = PageRequest.of(page, pageSize, sort);
            Page<User> userPage = userRepository.findAll(pageable);
            assertThat(userPage.getContent().size()).isLessThanOrEqualTo(pageSize);

            // Проверка сортировки
            List<User> users = userPage.getContent();
            if (users.size() > 1) {
                for (int i = 0; i < users.size() - 1; i++) {
                    // DESC сортировка по login
                    assertThat(users.get(i).getLogin()).isGreaterThan(users.get(i + 1).getLogin());
                }
            }
        }

        // критерий поиска
        var loginFilter = new StringFilter();
        loginFilter.setValue(login + 2);
        loginFilter.setOperator(StringFilter.Operator.CONTAINS);

        var roleFilter = new RoleFilter();
        roleFilter.setName("USER");
        roleFilter.setOperator(RoleFilter.Operator.EQUALS);

        var criteria = new UserSearchCriteria();
        criteria.setLogin(List.of(loginFilter));
        criteria.setRole(List.of(roleFilter));

        Specification<User> spec = UserSpecifications.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("login").ascending());

        // When
        Page<User> page = userRepository.findAll(spec, pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getContent())
                .extracting(User::getLogin)
                .containsExactly(login + 2,
                                         login + 20,
                                         login + 21,
                                         login + 22,
                                         login + 23,
                                         login + 24,
                                         login + 25,
                                         login + 26,
                                         login + 27,
                                         login + 28);
    }
}
