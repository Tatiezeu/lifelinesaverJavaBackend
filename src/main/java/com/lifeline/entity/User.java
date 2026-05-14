package com.lifeline.entity;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USERS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    @JsonbProperty("numeric_id")
    private Long numericId;

    @Column(name = "EMAIL", length = 255, unique = true)
    private String email;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "ROLE", length = 32)
    private String role;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "IS_ACTIVE")
    private boolean isActive = true;

    @Column(name = "IS_SUSPENDED")
    private boolean isSuspended = false;

    @Column(name = "DATE_JOINED")
    private LocalDateTime dateJoined;

    @Lob
    @Column(name = "PROFILE_PICTURE", columnDefinition = "CLOB")
    private String profilePicture;

    // Default constructor for JPA
    public User() {}

    // ── Getters & Setters ──────────────────────────────────────────────

    /**
     * NetBeans/Frontend expects 'id' to be the email for path params.
     */
    @JsonbProperty("id")
    public String getId() { return email; }

    public Long getNumericId() { return numericId; }
    public void setNumericId(Long numericId) { this.numericId = numericId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @JsonbProperty("is_active")
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @JsonbProperty("is_suspended")
    public boolean isSuspended() { return isSuspended; }
    public void setSuspended(boolean suspended) { isSuspended = suspended; }

    @JsonbProperty("date_joined")
    public LocalDateTime getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDateTime dateJoined) { this.dateJoined = dateJoined; }

    @JsonbProperty("profile_picture")
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    @PrePersist
    protected void onCreate() {
        if (dateJoined == null) {
            dateJoined = LocalDateTime.now();
        }
    }
}
