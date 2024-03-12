package com.example.planAndRemind.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class UpdateUserRequestModel {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String oldPassword;
    private String newPassword;
    private String imageUrl;
    private Byte updatePassword;

}
