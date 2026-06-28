package ture.courses.services.init;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ture.courses.entity.RoleType;
import ture.courses.enums.RoleName;
import ture.courses.repository.RoleTypeRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class RoleInitService {
    private final RoleTypeRepository roleRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRoles() {
        if (roleRepository.count() == 0) {
            List<RoleType> roles = List.of(
                    new RoleType(RoleName.MODERATOR),
                    new RoleType(RoleName.USER)
            );

            roleRepository.saveAll(roles);
            System.out.println("✅ Начальные роли созданы при старте приложения!");
        }
    }

}
