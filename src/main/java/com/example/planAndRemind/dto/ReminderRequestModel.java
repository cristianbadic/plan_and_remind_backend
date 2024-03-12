package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ReminderRequestModel {

    //true sau false
    private String defaultReminder;

    //email
    //notification
    //sms
    //email_notification
    //sms_email
    //sms_notification
    //all_options
    //none
    private String sentTo;

    // hours, days, minutes
    private String timeFormat;


    private Long amountBefore;
}
