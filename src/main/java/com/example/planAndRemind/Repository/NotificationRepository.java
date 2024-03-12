package com.example.planAndRemind.Repository;

import com.example.planAndRemind.model.NotificationEntity;
import com.example.planAndRemind.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAll();
    NotificationEntity save(NotificationEntity notificationEntity);
    void deleteById(Long id);
    Optional<NotificationEntity> findById(Long id);

    List<NotificationEntity> findAllByUser(UserEntity user);

    void deleteByUser(UserEntity user);

    void deleteByUser_Id(Long userId);

    List<NotificationEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user);

    List<NotificationEntity> findAllByUser_Id(Long userId);

}