package com.example.planAndRemind.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "friend_request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "first_user_id")
    private UserEntity firstUser;

    @ManyToOne
    @JoinColumn(name = "second_user_id")
    private UserEntity secondUser;

    //pending_one_two
    //pending_two_one
    //accepted
    //statusuri legare de request pt frontend: received, sent, sameUser, nothing, accepted
    private String status;
}