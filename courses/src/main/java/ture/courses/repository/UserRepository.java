package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
