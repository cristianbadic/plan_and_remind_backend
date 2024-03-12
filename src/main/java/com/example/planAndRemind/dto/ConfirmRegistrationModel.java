package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfirmRegistrationModel {
    private String email;
    private String accountConfirmation;
}
