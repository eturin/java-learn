package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
}
