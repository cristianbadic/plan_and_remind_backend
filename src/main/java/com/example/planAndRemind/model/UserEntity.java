package com.example.planAndRemind.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="User")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique=true)
    private String email;
    private LocalDate birthDate;
    private String password;

    @Column(length=10000)
    private String imageUrl;

    private String accountConfirmation;

    private String phoneNumber;

    private String phoneNrConfirmation;

    @JsonIgnore
    @OneToMany(mappedBy = "firstUser", cascade = CascadeType.ALL)
    private List<FriendRequestEntity> firstFriendRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "secondUser", cascade = CascadeType.ALL)
    private List<FriendRequestEntity> secondFriendRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<NotificationEntity> notifications;

    @JsonIgnore
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<EventEntity> createdEvents;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<InvitationEntity> invitations;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserReminderEntity> userReminders;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

