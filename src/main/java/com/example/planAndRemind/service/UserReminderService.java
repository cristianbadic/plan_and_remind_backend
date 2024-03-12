package com.example.planAndRemind.service;


import com.example.planAndRemind.Repository.UserReminderRepository;
import com.example.planAndRemind.model.UserReminderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserReminderService {

    private final UserReminderRepository userReminderRepository;

    @Autowired
    public UserReminderService(UserReminderRepository userReminderRepository) {
        this.userReminderRepository = userReminderRepository;
    }

    public List<UserReminderEntity> getAllUserReminders(){
        return this.userReminderRepository.findAll();
    }

    public List<UserReminderEntity> getAllUserRemindersForUser(Long userId){
        return this.userReminderRepository.findByUser_Id(userId);
    }
}
