package com.example.planAndRemind.controller;


import com.example.planAndRemind.model.UserReminderEntity;
import com.example.planAndRemind.service.UserReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/user-reminder")
public class UserReminderController {

    private final UserReminderService userReminderService;

    @Autowired
    public UserReminderController(UserReminderService userReminderService) {
        this.userReminderService = userReminderService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<UserReminderEntity>> getUserReminders() {
        List<UserReminderEntity> userReminders = this.userReminderService.getAllUserReminders();
        return ResponseEntity.ok(userReminders);
    }

    @GetMapping("get-by-user/{id}")
    public ResponseEntity<List<UserReminderEntity>> getUserRemindersByUserId(@PathVariable Long id) {
        List<UserReminderEntity> userReminders = this.userReminderService.getAllUserRemindersForUser(id);
        return ResponseEntity.ok(userReminders);
    }
}
