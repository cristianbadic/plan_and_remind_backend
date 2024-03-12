package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvitedUserModel {
    private Long id;
    private String firstName;
    private String lastName;

    //accepted, declined sau pending
    private String statusToInvitation;
}
