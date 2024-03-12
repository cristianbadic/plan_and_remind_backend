package com.example.planAndRemind.controller;

import com.example.planAndRemind.dto.*;
import com.example.planAndRemind.model.EventEntity;
import com.example.planAndRemind.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<EventEntity>> getAllEvents() {
        List<EventEntity> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    //all events for which a user was invited to
    @GetMapping("/get-all/for-user-invited-to/{id}")
    public ResponseEntity<List<EventEntity>> getAllForUserInvitedTo(@PathVariable Long id) {
        List<EventEntity> events = eventService.getAllEventsForUserInvitations(id);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/get-all/for-user-invited-to/event-status/{id}/{status}")
    public ResponseEntity<List<EventEntity>> getAllWithStatusAndUserInvitedTo(@PathVariable Long id, @PathVariable String status) {
        List<EventEntity> events = eventService.getAllEventForStatusAndUserWasInvitedTo(id,status);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/check-overlap/{userId}")
    public ResponseEntity<List<OverlapResponseModel>> checkOverlap(@PathVariable Long userId,
                                                                   @RequestBody CheckAvailabilityRequestModel timeData) {
        List<OverlapResponseModel> overlappingEvents = eventService.checkOverlap(userId, timeData);
        return ResponseEntity.ok(overlappingEvents);
    }

    @PostMapping("/check-overlap-for-update/{userId}/{eventId}")
    public ResponseEntity<List<OverlapResponseModel>> checkOverlapForUpdateEvent(@PathVariable Long userId,
                                                                   @PathVariable Long eventId,
                                                                   @RequestBody CheckAvailabilityRequestModel timeData) {
        List<OverlapResponseModel> overlappingEvents = eventService.checkOverlapForUpdateEvent(userId, timeData, eventId);
        return ResponseEntity.ok(overlappingEvents);
    }

    //all upcoming events for which the user confirmed his invitation
    @GetMapping("/get-all/upcoming-sorted/{userId}")
    public ResponseEntity<List<EventResponseModel>> getAllUpcomingEventsForUser(@PathVariable Long userId) {
        List<EventResponseModel> events = eventService.getUpcomingEventsSortedForUser(userId);
        return ResponseEntity.ok(events);
    }

    //all past events for which the user took part of
    @GetMapping("/get-all/past-sorted/{userId}")
    public ResponseEntity<List<EventResponseModel>> getAllTookPartInPastEventsForUser(@PathVariable Long userId) {
        List<EventResponseModel> events = eventService.getPastEventsSortedForUser(userId);
        return ResponseEntity.ok(events);
    }

    //all events for which the user can still respond to
    @GetMapping("/get-all/upcoming-to-respond/{userId}")
    public ResponseEntity<List<EventResponseModel>> getAllNeedToRespondEventsForUser(@PathVariable Long userId) {
        List<EventResponseModel> events = eventService.getToRespondEventsForUserSorted(userId);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody EventRequestModel eventEntity) {
        eventService.saveEvent(eventEntity);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEvent(@RequestBody EventRequestModel eventEntity, @PathVariable Long id) {
        eventService.updateEvent(eventEntity, id);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PutMapping("/update-group-creator-reminder/{eventId}/{userId}")
    public ResponseEntity<?> updateGroupEventCreatedReminders(@RequestBody ReminderRequestModel reminderDetails, @PathVariable Long eventId,
                                                              @PathVariable Long userId) {
        eventService.updateGroupEventCreatedReminders(reminderDetails, eventId, userId);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PutMapping("/update-group-invitee-reminder/{eventId}/{userId}")
    public ResponseEntity<?> updateGroupEventInvitedReminders(@RequestBody ReminderRequestModel reminderDetails, @PathVariable Long eventId,
                                                              @PathVariable Long userId) {
        eventService.updateGroupEventInvitedReminders(reminderDetails, eventId, userId);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/get/single-event-to-update/{id}")
    public ResponseEntity<EventRequestModel> getEventToUpdateForSingleEvent(@PathVariable Long id) {
        EventRequestModel event = eventService.getEventToUpdateForSingleEvent(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/get/group-event-to-update/{eventId}/{userId}/{relationToEvent}")
    public ResponseEntity<EventRequestModel> getEventToUpdateForGroupEvent(@PathVariable Long eventId, @PathVariable Long userId,
                                                                            @PathVariable Integer relationToEvent) {
        EventRequestModel event = eventService.getEventToUpdateForGroupEvent(eventId, userId, relationToEvent);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/add-new-invitees/{id}")
    public ResponseEntity<?> addNewInvitees(@RequestBody NewInviteesModel listNewInvitees, @PathVariable Long id) {
        eventService.addNewInviteesToEvent(listNewInvitees, id);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEventById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-all-by-creator/{id}")
    public ResponseEntity<?> deleteAllByCreator(@PathVariable Long id) {
        eventService.deleteByCreator(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/cancel-upcoming/{id}")
    public ResponseEntity<?> cancelUpcomingEvent(@PathVariable Long id) {
        eventService.cancelUpcomingOrganizedEvent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-single/{id}")
    public ResponseEntity<?> deleteSingleEvent(@PathVariable Long id) {
        eventService.deleteSingleEvent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-invitation/{id}/{userId}")
    public ResponseEntity<?> deleteInvitationForPastEvent(@PathVariable Long id, @PathVariable Long userId) {
        eventService.deleteInvitationForPastEvents(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-past-group/{id}/{userId}")
    public ResponseEntity<?> deletePastGroupEvent(@PathVariable Long id, @PathVariable Long userId) {
        eventService.deletePastOrganizedEvent(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
