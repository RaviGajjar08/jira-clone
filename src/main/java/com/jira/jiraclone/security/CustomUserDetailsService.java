package com.jira.jiraclone.security;

import com.jira.jiraclone.entity.User;
import com.jira.jiraclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // FIX: OAuth2 users have no password — use empty string to avoid NPE
        String password =
                user.getPassword() != null
                        ? user.getPassword()
                        : "";

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(password)
                .roles(user.getRole().name())
                .build();
    }
}
