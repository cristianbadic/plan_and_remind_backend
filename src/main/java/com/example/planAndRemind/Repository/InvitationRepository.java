package com.example.planAndRemind.Repository;

import com.example.planAndRemind.model.EventEntity;
import com.example.planAndRemind.model.InvitationEntity;

import com.example.planAndRemind.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    List<InvitationEntity> findAll();
    InvitationEntity save(InvitationEntity invitationEntity);
    void deleteById(Long id);
    Optional<InvitationEntity> findById(Long id);

    List<InvitationEntity> findByUser(UserEntity user);
    List<InvitationEntity> findByEvent_Id(Long eventId);
    List<InvitationEntity> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    List<InvitationEntity> findByEventAndStatusNot(EventEntity event, String status);

    void deleteByEvent(EventEntity event);
}