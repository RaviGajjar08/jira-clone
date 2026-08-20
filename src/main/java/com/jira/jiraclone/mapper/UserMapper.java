package com.jira.jiraclone.mapper;

import com.jira.jiraclone.dto.UserResponse;
import com.jira.jiraclone.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())

                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .enabled(user.isEnabled())

                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())

                .lastLoginAt(user.getLastLoginAt())
                .lastActivityAt(user.getLastActivityAt())

                .build();
    }
}