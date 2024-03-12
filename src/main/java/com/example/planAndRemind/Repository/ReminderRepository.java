package com.example.planAndRemind.Repository;


import com.example.planAndRemind.model.EventEntity;
import com.example.planAndRemind.model.ReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<ReminderEntity, Long> {

    List<ReminderEntity> findAll();
    ReminderEntity save(ReminderEntity reminderEntity);
    void deleteById(Long id);
    Optional<ReminderEntity> findById(Long id);

    List<ReminderEntity> findByTypeAndEvent(String type, EventEntity event);

    Optional<ReminderEntity> findTopByTypeAndEventOrderByIdDesc(String type, EventEntity event);

    List<ReminderEntity> findByEvent_Id(Long eventId);

    List<ReminderEntity> findBySent(Byte sent);

    void deleteByEvent(EventEntity event);

    void deleteByEventAndTypeIn(EventEntity event, List<String> types);

}
