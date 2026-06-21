package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module,Long> {
}
