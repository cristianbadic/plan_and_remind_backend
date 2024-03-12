package com.example.planAndRemind.controller;


import com.example.planAndRemind.dto.NotificationsRemindersResponseModel;
import com.example.planAndRemind.model.ReminderEntity;
import com.example.planAndRemind.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/reminder")
public class ReminderController {

    private final ReminderService reminderService;

    @Autowired
    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<ReminderEntity>> getAllReminders() {
        List<ReminderEntity> reminders = this.reminderService.getAllReminders();
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/get-all/for-event/{eventId}")
    public ResponseEntity<List<ReminderEntity>> getAllRemindersForEvent(@PathVariable Long eventId) {
        List<ReminderEntity> reminders = this.reminderService.getRemindersByEventId(eventId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/get-all/reminders-and-notifications/{userId}")
    public ResponseEntity<List<NotificationsRemindersResponseModel>> getAllNotificationsAndRemindersForUser(@PathVariable Long userId) {
        List<NotificationsRemindersResponseModel> notificationsAndRemindersForUser = this.reminderService
                .getAllSentNotificationsAndRemindersForUser(userId);
        return ResponseEntity.ok(notificationsAndRemindersForUser);
    }

    @DeleteMapping("/delete/reminder-or-notification/{notificationReminderId}/{userId}/{type}")
    public ResponseEntity<?> deleteNotificationOrReminder(@PathVariable Long notificationReminderId, @PathVariable Long userId, @PathVariable String type) {
        this.reminderService.deleteNotificationOrReminder(notificationReminderId, userId, type);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/seen/reminder-or-notification/{notificationReminderId}/{userId}/{type}")
    public ResponseEntity<?> seenNotificationOrReminder(@PathVariable Long notificationReminderId, @PathVariable Long userId, @PathVariable String type) {
        this.reminderService.seenNotificationOrReminder(notificationReminderId, userId, type);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-all/reminders-and-notifications/{userId}")
    public ResponseEntity<?> deleteAllNotificationsAndRemindersForUser(@PathVariable Long userId) {
        this.reminderService.deleteAllNotificationsAndRemindersForUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
