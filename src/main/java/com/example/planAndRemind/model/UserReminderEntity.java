package com.example.planAndRemind.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "user_reminder")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "reminder_id")
    private ReminderEntity reminder;

    //1 sau 0
    private Byte seen;
}
