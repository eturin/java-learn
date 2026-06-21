package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ture.courses.entity.RoleType;

public interface RoleTypeRepository extends JpaRepository<RoleType,Long> {
}
