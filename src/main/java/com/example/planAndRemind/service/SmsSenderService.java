package com.example.planAndRemind.service;

import ClickSend.Api.SmsApi;
import ClickSend.ApiClient;
import ClickSend.ApiException;
import ClickSend.Model.SmsMessage;
import ClickSend.Model.SmsMessageCollection;
import com.example.planAndRemind.dto.SmsDetailsModel;
import com.example.planAndRemind.exception.FailedSendSmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsSenderService {


    private final ApiClient clickSendConfig;

    @Autowired
    public SmsSenderService(ApiClient clickSendConfig) {
        this.clickSendConfig = clickSendConfig;
    }


    public String sendSmsToUser(SmsDetailsModel smsDetails){

        SmsApi smsApi = new SmsApi(clickSendConfig);
        SmsMessage smsMessage=new SmsMessage();
        smsMessage.body(smsDetails.getMessageBody());
        smsMessage.to(smsDetails.getSendTo());
        smsMessage.source(smsDetails.getSendingSource());
        return getString(smsApi, smsMessage);

    }

    public String sendConfirmationSms(String phoneNr, String registrationCode){
        SmsApi smsApi = new SmsApi(clickSendConfig);

        SmsMessage smsMessage=new SmsMessage();
        smsMessage.body("Plan & Remind: Hello! Te confirmation code for your phone number is the following: " + registrationCode);
        smsMessage.to(phoneNr);
        smsMessage.source("Spring boot confirmation");
        return getString(smsApi, smsMessage);
    }

    private String getString(SmsApi smsApi, SmsMessage smsMessage) {
        List<SmsMessage> smsMessageList= List.of(smsMessage);

        SmsMessageCollection smsMessages = new SmsMessageCollection();
        smsMessages.messages(smsMessageList);
        try {
            return smsApi.smsSendPost(smsMessages);

        } catch (ApiException e) {
            throw new FailedSendSmsException("Couldn't send Sms");
        }
    }
}
