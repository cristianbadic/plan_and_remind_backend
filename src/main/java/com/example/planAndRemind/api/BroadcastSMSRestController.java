package com.example.planAndRemind.api;

import com.example.planAndRemind.dto.SmsDetailsModel;
import com.example.planAndRemind.exception.FailedSendSmsException;
import com.example.planAndRemind.service.SmsSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BroadcastSMSRestController {

    private final SmsSenderService smsSenderService;

    @Autowired
    public BroadcastSMSRestController(SmsSenderService smsSenderService) {
        this.smsSenderService = smsSenderService;
    }



    @PostMapping("/api/send-to-user")
    public ResponseEntity<String> sendSMSToUser(@RequestBody SmsDetailsModel smsDetailsModel){
        try {
            String result = this.smsSenderService.sendSmsToUser(smsDetailsModel);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (FailedSendSmsException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Exception when calling SmsApi#smsSendPost", HttpStatus.BAD_REQUEST);

    }

    }