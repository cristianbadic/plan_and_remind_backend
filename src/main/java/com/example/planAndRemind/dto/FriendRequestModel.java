package com.example.planAndRemind.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestModel {
    Long firstUserId;
    Long secondUserId;
}
