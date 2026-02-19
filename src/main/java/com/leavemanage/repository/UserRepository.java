package com.leavemanage.repository;

import com.leavemanage.model.Role;
import com.leavemanage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRole(Role role);
    List<User> findAllByRole(Role role);
}
