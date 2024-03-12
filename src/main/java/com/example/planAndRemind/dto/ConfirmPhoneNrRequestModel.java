package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmPhoneNrRequestModel {
    private Long id;
    private String phoneNrConfirmation;
}
