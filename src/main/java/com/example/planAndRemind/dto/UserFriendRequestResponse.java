package com.example.planAndRemind.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public class UserFriendRequestResponse {
    private UserDTO userEntity;

    // received, sent, sameUser, nothing, accepted
    private String status;
    private Long requestId;
}
