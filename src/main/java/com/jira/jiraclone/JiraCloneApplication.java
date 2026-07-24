package com.jira.jiraclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JiraCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiraCloneApplication.class, args);
    }

}
