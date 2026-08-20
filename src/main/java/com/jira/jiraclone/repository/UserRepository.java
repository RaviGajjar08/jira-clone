package com.jira.jiraclone.repository;

import com.jira.jiraclone.entity.Role;
import com.jira.jiraclone.entity.User;
import com.jira.jiraclone.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // ==========================
    // Authentication
    // ==========================

    Optional<User> findByEmailAndDeletedFalse(
            String email
    );

    boolean existsByEmailAndDeletedFalse(
            String email
    );

    // ==========================
    // Search
    // ==========================

    Page<User>
    findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedFalse(
            String name,
            String email,
            Pageable pageable
    );

    // ==========================
    // Admin
    // ==========================

    Page<User> findByDeletedFalse(
            Pageable pageable
    );

    List<User> findByRoleAndDeletedFalse(
            Role role
    );

    List<User> findByStatusAndDeletedFalse(
            UserStatus status
    );

    List<User> findByEnabledTrueAndDeletedFalse();

    List<User> findByEnabledFalseAndDeletedFalse();
}