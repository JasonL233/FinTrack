package com.fintrack.transactionservice.dto;

public class UserContext {
    private Long userId;
    private String email;

    // Constructors
    public UserContext() {
    }

    public UserContext(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String email;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public UserContext build() {
            return new UserContext(userId, email);
        }
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}