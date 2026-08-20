package com.jira.jiraclone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_user_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_user_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_user_deleted",
                        columnList = "deleted"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
            unique = true,
            nullable = false,
            length = 150
    )
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime lastLoginAt;

    private LocalDateTime lastActivityAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;
}