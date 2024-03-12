package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CheckAvailabilityRequestModel {

    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

}
