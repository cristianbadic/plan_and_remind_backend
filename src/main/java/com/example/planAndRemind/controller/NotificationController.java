package com.example.planAndRemind.controller;

import com.example.planAndRemind.dto.NotificationResponse;
import com.example.planAndRemind.model.NotificationEntity;
import com.example.planAndRemind.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        this.notificationService.deleteNotification(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<NotificationEntity>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/get-all/{id}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForUser(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getAllNotificationsForUser(id));
    }

    @DeleteMapping("/delete-all/{id}")
    public ResponseEntity<?> deleteAllForUser(@PathVariable Long id) {
        this.notificationService.deleteAllForUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
