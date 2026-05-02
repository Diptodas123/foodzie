package com.foodzie.repository;

import com.foodzie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID> {
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    Optional<User> findByEmail(String email);
}
