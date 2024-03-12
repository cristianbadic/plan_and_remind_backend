package com.example.planAndRemind.Repository;

import com.example.planAndRemind.model.EventEntity;
import com.example.planAndRemind.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findAll();
    EventEntity save(EventEntity eventEntity);
    void deleteById(Long id);
    Optional<EventEntity> findById(Long id);

    List<EventEntity> findByCreator(UserEntity creator);

    List<EventEntity> findByNameAndEventTypeAndCreator(String name, String eventType, UserEntity creator);

    Optional<EventEntity> findTopByNameAndEventTypeAndCreatorOrderByIdDesc(String name, String eventType, UserEntity creator);

    void deleteByCreator(UserEntity creator);

    List<EventEntity> findAllByInvitations_User_Id(Long userId);

    List<EventEntity> findAllByInvitations_User_IdAndInvitations_Status(Long userId, String status);

    List<EventEntity> findAllByInvitations_User_IdAndInvitations_StatusAndEventDateAfter(Long userId, String status, LocalDate eventDate);
    List<EventEntity> findAllByInvitations_User_IdAndInvitations_StatusAndEventDateBefore(Long userId, String status, LocalDate eventDate);

    List<EventEntity> findAllByEventTypeAndInvitations_User_IdAndInvitations_StatusAndLimitDateAfter(String eventType, Long userId, String status, LocalDate limitDate);

    List<EventEntity> findAllByCreator_IdAndCanceledAndEventDateAfter(Long userId, Byte canceled, LocalDate eventDate);
    List<EventEntity> findAllByCreator_IdAndCanceledAndEventDateBefore(Long userId, Byte canceled, LocalDate eventDate);
}