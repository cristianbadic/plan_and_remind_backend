package com.example.planAndRemind.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class EventRequestModel {

    private String name;

    private String eventType;

    private String description;

    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

    // doar daca group
    private LocalDate limitDate;

    private Long creatorId;

    private String defaultReminder;

    private String sentTo;

    //minutes, hours, days
    private String timeFormat;

    private Long amountBefore;

    //doar daca group
    private List<Long> inviteeIDs;

    public EventRequestModel(){

    }
}
