package ture.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.courses.entity.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic,Long> {
}
