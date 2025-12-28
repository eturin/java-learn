package ture.bank.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ture.bank.entity.Role;
import java.util.Optional;

@SpringBootTest          // Загружает полный Spring контекст
@ActiveProfiles("test")  // Использует test профиль
@Transactional           // Откатывает транзакции после тестов
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldBe() {
        Role role = new Role();
    }

    @Test
    void shouldFindRoleByName() {

        Object[][] m = {
                {"USER", "Пользователь"},
                {"ADMIN", "Администратор"}
        };
        for(Object[] x : m) {
            var roleName = (String) x[0];
            var description = (String) x[1];

            Optional<Role> foundRole = roleRepository.findByName(roleName);

            assertThat(foundRole).isPresent();
            assertThat(foundRole.get().getName()).isEqualTo(roleName);
            assertThat(foundRole.get().getDescription()).isEqualTo(description);
        }
    }
}
