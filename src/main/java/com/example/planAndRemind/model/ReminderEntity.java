package com.example.planAndRemind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reminder")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;


    //creator_def
    //creator_custom
    //invitation
    //to_respond
    //confirmed_def
    //confirmed_custom
    //canceled
    @Column(nullable = false)
    private String type;

    // 1 or 0
    @Column(nullable = false)
    private Byte sent;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventEntity event;

    //email
    //notification
    //sms
    //email_notification
    //sms_email
    //sms_notification
    //all_options
    //none
    @Column(nullable = false)
    private String sendTo;

    //minutes
    //hours
    //days
    private String timeFormat;

    private Long amountBefore;

    @JsonIgnore
    @OneToMany(mappedBy = "reminder", cascade = CascadeType.ALL)
    private List<UserReminderEntity> userReminders;

    public ReminderEntity(String type, Byte sent, EventEntity event, String sentTo, String timeFormat, Long amountBefore) {
        this.type = type;
        this.sent = sent;
        this.event = event;
        this.sendTo = sentTo;
        this.timeFormat = timeFormat;
        this.amountBefore = amountBefore;
        this.userReminders = new ArrayList<>();
    }

}
