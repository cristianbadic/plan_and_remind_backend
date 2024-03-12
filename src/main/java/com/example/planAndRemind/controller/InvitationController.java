package com.example.planAndRemind.controller;

import com.example.planAndRemind.dto.ReminderRequestModel;
import com.example.planAndRemind.model.InvitationEntity;
import com.example.planAndRemind.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/invitation")
public class InvitationController {

    private final InvitationService invitationService;

    @Autowired
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<InvitationEntity>> getAllInvitations() {
        List<InvitationEntity> invitations = invitationService.getAllInvitations();
        return ResponseEntity.ok(invitations);
    }


    @PostMapping("/create")
    public ResponseEntity<?> createInvitation(@RequestBody InvitationEntity invitationEntity) {
        invitationService.saveInvitation(invitationEntity);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/decline/{userId}/{eventId}")
    public ResponseEntity<?> declineInvitation(@PathVariable Long userId, @PathVariable Long eventId) {
        invitationService.declineInvitation(userId, eventId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/accept/{userId}/{eventId}")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long userId, @PathVariable Long eventId,
                                            @RequestBody ReminderRequestModel reminderDetails) {
        invitationService.acceptInvitation(userId, eventId, reminderDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}