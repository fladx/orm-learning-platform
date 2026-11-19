package ru.vlad.orm.quiz;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByModuleId(Long moduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> findForWrite(@Param("id") Long id);
}
