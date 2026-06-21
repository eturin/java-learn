package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
}
