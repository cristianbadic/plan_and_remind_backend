package com.example.planAndRemind.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class NotificationsRemindersResponseModel {

    private Long id;
    private String message;
    private Byte seen;
    private LocalDateTime createdAt;

    //notification or reminder
    private String type;
}
