package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatusRequestIdModel {
    private String status;
    private Long requestId;
}
