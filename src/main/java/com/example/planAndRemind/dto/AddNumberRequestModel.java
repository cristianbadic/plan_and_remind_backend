package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AddNumberRequestModel {
    private Long id;
    private String phoneNumber;
}
