package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.NotificationRepository;
import com.example.planAndRemind.Repository.UserRepository;
import com.example.planAndRemind.dto.NotificationResponse;
import com.example.planAndRemind.exception.NotificationException;
import com.example.planAndRemind.exception.UserNotFoundException;
import com.example.planAndRemind.model.NotificationEntity;
import com.example.planAndRemind.model.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;


    @Autowired
    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public void createNotification(NotificationEntity notificationEntity){
        this.notificationRepo.save(notificationEntity);
    }

    public void deleteNotification(Long id){

        NotificationEntity notification = notificationRepo.findById(id).orElseThrow(
                ()-> new NotificationException("Notification not found to delete!"));

        UserEntity user = notification.getUser();
        user.getNotifications().remove(notification);
        this.userRepo.save(user);
        this.notificationRepo.deleteById(id);
    }

    public List<NotificationResponse> getAllNotificationsForUser(Long userId){

       UserEntity user = userRepo.findById(userId).orElseThrow(
                ()-> new UserNotFoundException("User user not found!"));

       List<NotificationEntity> allNotifications = this.notificationRepo.findAllByUserOrderByCreatedAtDesc(user);

        return allNotifications.stream()
                .map(notification -> new NotificationResponse(notification.getId(), notification.getMessage(), notification.getSeen()))
                .collect(Collectors.toList());
    }

    public List<NotificationEntity> getAll(){

        return this.notificationRepo.findAll();
    }

    @Transactional
    public void deleteAllForUser(Long userId){

        UserEntity user = userRepo.findById(userId).orElseThrow(
                ()-> new UserNotFoundException("User user not found!"));

        user.getNotifications().clear();
        this.userRepo.save(user);

        this.notificationRepo.deleteByUser(user);

    }
}
