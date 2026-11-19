package ru.vlad.orm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "modules")
    java.util.Optional<Course> findWithModulesById(Long id);
}
