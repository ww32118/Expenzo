package com.Expenzo_Backend.Expenzo.repository;

import com.Expenzo_Backend.Expenzo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);


    User findUserById(Long id);

    Optional<User> findById(Long id);

    Long id(Long id);

    void deleteUserById(Long id);

    boolean existsByUsername(String username);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);

    // If findByEmail doesn't return Optional, change it to:
    Optional<User> findByEmail(String email);

}
