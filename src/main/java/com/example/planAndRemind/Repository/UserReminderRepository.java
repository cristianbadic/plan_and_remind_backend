package com.example.planAndRemind.Repository;

import com.example.planAndRemind.model.ReminderEntity;
import com.example.planAndRemind.model.UserEntity;
import com.example.planAndRemind.model.UserReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReminderRepository extends JpaRepository<UserReminderEntity, Long> {

    List<UserReminderEntity> findAll();
    UserReminderEntity save(UserReminderEntity userReminderEntity);
    void deleteById(Long id);
    Optional<UserReminderEntity> findById(Long id);

    Optional<UserReminderEntity> findByUserAndReminder(UserEntity user, ReminderEntity reminder);

    Optional<UserReminderEntity> findByUser_IdAndReminder_Id(Long userId, Long reminderId);

    List<UserReminderEntity> findByReminder(ReminderEntity reminder);

    List<UserReminderEntity> findByUser_Id(Long userId);

    void deleteByUser_IdAndReminder_Event_Id(Long userId, Long eventId);

    List<UserReminderEntity> findByUser_IdAndReminder_Event_IdAndReminder_TypeIn(Long userId, Long eventId, List<String> types);

    List<UserReminderEntity> findByUser_IdAndReminder_SentAndReminder_SendToIn(Long userId, Byte wasSent, List<String> sendTo);

    List<UserReminderEntity> findByUser_IdAndReminder_Sent(Long userId, Byte wasSent);

    List<UserReminderEntity> findByUser_IdAndReminder_Event_IdAndReminder_Type(Long userId, Long eventId, String reminderType);

    long countByReminder_Id(Long reminderId);

    void deleteByUser_IdAndReminder_Id(Long userId, Long reminderId);
}

