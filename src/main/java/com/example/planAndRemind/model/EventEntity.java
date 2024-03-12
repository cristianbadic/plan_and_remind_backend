package com.example.planAndRemind.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// 1. pe front pentru limit date sa cer sa se dea numarul de zile
// inainte de a a face callul pe back in metoda aia fac cateva validari, ora de inceput sa fie inaite de ora de sfarsit
// si ca limit date sa fie inainte de start date, daca nu, dau pop-up la ceva snackbar-uri de ai grija cum ai pus datele

//2. pt adaugare un endpoint care se asigura ca in acel interval de nu exista alte evenimente,daca exista sa dea o eroare
// pe front avem dialog de a adaugare, cand dai submit se apeleaza ebdpointul de daca nu sunt evenimente
// daca sunt se deschide un nou dialog de confirmare, doar daca aicise confirma o sa facem request de adaugare de evinement
//dupa confirmarea din al doilea dialog inchidem ambele dialoguri, daca nu e confirmat inchidem doar al doilea dialog


//4. event cu nume diferite daca tipul e la fel ca sa evitam confuzii

//5. pt reminder cand trec prin toate alea netrimise, poate chiar inainte sa trimit sa mai dau un check daca nu o fost sters
    //eventul

@Entity
@Table(name = "event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    //single sau group
    @Column(nullable = false)
    private String eventType;

    @Column(length=10000)
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate;
    @Column(nullable = false)
    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate limitDate;

    private Byte canceled;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private UserEntity creator;

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<InvitationEntity> invitations;

    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<ReminderEntity> reminders;

    public EventEntity(String name, String eventType, String description, LocalDate eventDate, LocalTime startTime, LocalTime endTime, LocalDate limitDate, Byte canceled, UserEntity creator) {
        this.name = name;
        this.eventType = eventType;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.limitDate = limitDate;
        this.canceled = canceled;
        this.creator = creator;
        this.invitations = new ArrayList<>();
        this.reminders = new ArrayList<>();
    }
}