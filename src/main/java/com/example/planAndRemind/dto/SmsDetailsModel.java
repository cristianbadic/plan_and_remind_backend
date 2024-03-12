package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SmsDetailsModel {
    private String messageBody;
    private String sendingSource;
    private String sendTo;

    public SmsDetailsModel() {
    }
}
