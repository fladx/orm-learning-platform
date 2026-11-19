package ru.vlad.orm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    java.util.Optional<Profile> findByUserId(Long userId);
}
