package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {

    private UserDTO userEntity;
    private String jwtToken;
    private LocalDateTime expDate;

}
