package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class EventResponseModel {
    private Long id;
    private String name;
    private String eventType;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate limitDate;
    private String creatorFirstName;
    private String creatorLastName;

    //can be: true or false
    private String isFuture;

    //created_single, created_group, invited_pending, invited_accepted
    private String specification;
}