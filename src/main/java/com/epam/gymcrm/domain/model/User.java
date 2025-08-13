package com.epam.gymcrm.domain.model;

import java.util.Objects;

public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String rawPassword;
    private Boolean isActive;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        if (Boolean.TRUE.equals(this.isActive) && active)
            throw new IllegalStateException("User is already active.");
        if (Boolean.FALSE.equals(this.isActive) && !active)
            throw new IllegalStateException("User is already inactive.");
        this.isActive = active;
    }

    public boolean checkPassword(String inputPassword) {
        return Objects.equals(this.password, inputPassword);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public void changePassword(String newPassword) {
        if (Objects.isNull(newPassword) || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
        this.password = newPassword;
    }

    public void updateProfile(String firstName, String lastName, boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
