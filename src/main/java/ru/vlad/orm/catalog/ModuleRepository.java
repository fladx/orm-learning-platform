package ru.vlad.orm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    java.util.List<Module> findByCourseIdOrderById(Long courseId);
}
