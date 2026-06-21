package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course,Long> {
}
