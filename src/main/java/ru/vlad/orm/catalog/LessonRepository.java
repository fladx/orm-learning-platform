package ru.vlad.orm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    java.util.List<Lesson> findByModuleIdOrderById(Long moduleId);
    java.util.List<Lesson> findByModuleIdInOrderById(java.util.Collection<Long> moduleIds);
}
