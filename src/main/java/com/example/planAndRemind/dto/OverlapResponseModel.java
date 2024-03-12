package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OverlapResponseModel {
    private Long eventId;
    private String eventName;
    private String firstName;
    private String lastName;
}
