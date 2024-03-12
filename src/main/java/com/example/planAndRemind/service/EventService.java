package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.EventRepository;
import com.example.planAndRemind.Repository.InvitationRepository;
import com.example.planAndRemind.Repository.UserReminderRepository;
import com.example.planAndRemind.Repository.UserRepository;
import com.example.planAndRemind.dto.*;
import com.example.planAndRemind.exception.EventException;
import com.example.planAndRemind.exception.InvitationException;
import com.example.planAndRemind.exception.UserNotFoundException;
import com.example.planAndRemind.model.*;
import com.example.planAndRemind.util.ConversionsUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final ReminderService reminderService;

    private final InvitationRepository invitationRepository;

    private final UserReminderRepository userReminderRepository;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository, ReminderService reminderService,
                        InvitationRepository invitationRepository, UserReminderRepository userReminderRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.reminderService = reminderService;
        this.invitationRepository = invitationRepository;
        this.userReminderRepository = userReminderRepository;
    }

    public List<EventEntity> getAllEvents() {
        return eventRepository.findAll();
    }

    //returns list of events that would overlap with the current one to add
    public List<OverlapResponseModel> checkOverlap(Long userId, CheckAvailabilityRequestModel timeData){

        List<OverlapResponseModel> overlappingEvents = new ArrayList<>();

        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<EventEntity> upcomingCreatorEvents =
                this.eventRepository.findAllByCreator_IdAndCanceledAndEventDateAfter(userId, (byte) 0, yesterday);

        List<EventEntity> goingToInvitedToEvents =
                this.eventRepository.findAllByInvitations_User_IdAndInvitations_StatusAndEventDateAfter(userId, "accepted", yesterday);

        for (EventEntity upcomingCreatedEvent: upcomingCreatorEvents){
            if (!ConversionsUtil.availableInterval(timeData, upcomingCreatedEvent.getEventDate(), upcomingCreatedEvent.getStartTime(),
                    upcomingCreatedEvent.getEndTime())){
               overlappingEvents.add(new OverlapResponseModel(upcomingCreatedEvent.getId(), upcomingCreatedEvent.getName(), upcomingCreatedEvent.getCreator().getFirstName(),
                        upcomingCreatedEvent.getCreator().getLastName()));
            }
        }

        for (EventEntity goingToEvent: goingToInvitedToEvents){
            if (!ConversionsUtil.availableInterval(timeData, goingToEvent.getEventDate(), goingToEvent.getStartTime(),
                    goingToEvent.getEndTime())){
                overlappingEvents.add(new OverlapResponseModel(goingToEvent.getId(), goingToEvent.getName(), goingToEvent.getCreator().getFirstName(),
                        goingToEvent.getCreator().getLastName()));
            }
        }
        return overlappingEvents;
    }

    public List<OverlapResponseModel> checkOverlapForUpdateEvent(Long userId, CheckAvailabilityRequestModel timeData, Long eventId){
        List<OverlapResponseModel> overlappingEvents = this.checkOverlap(userId, timeData);

        overlappingEvents.removeIf(element -> Objects.equals(element.getEventId(), eventId));

        return overlappingEvents;
    }

    public void saveEvent(EventRequestModel eventModel) {

        UserEntity creator = userRepository.findById(eventModel.getCreatorId()).orElseThrow(
                ()-> new UserNotFoundException("Creator User user not found!"));


        EventEntity eventToCreate = new EventEntity(eventModel.getName(), eventModel.getEventType(),
                eventModel.getDescription(), eventModel.getEventDate(), eventModel.getStartTime(), eventModel.getEndTime(),
                eventModel.getLimitDate(), (byte) 0,creator);

        creator.getCreatedEvents().add(eventToCreate);
        this.userRepository.save(creator);

        //the actual created event
        EventEntity createdEvent =eventRepository.findTopByNameAndEventTypeAndCreatorOrderByIdDesc(eventModel.getName(),
                        eventModel.getEventType(), creator)
                .orElseThrow(()-> new EventException("EventNotFound!"));

        if (eventModel.getDefaultReminder().equals("true")){

            String timeFormat = "days";
            long amountBefore = 1L;

            if (eventModel.getEventType().equals("single")) {

                timeFormat = "minutes";
                amountBefore = 30L;
            }

            ReminderEntity defaultReminderEntity = new ReminderEntity("creator_def", (byte) 0, createdEvent,
                        eventModel.getSentTo(), timeFormat, amountBefore);


            //saved the ReminderEntity
            createdEvent.getReminders().add(defaultReminderEntity);
            this.eventRepository.save(createdEvent);

            ReminderEntity createdReminder = this.reminderService.getReminderByTypeAndEvent("creator_def", createdEvent);

            UserReminderEntity userReminderEntity = new UserReminderEntity(null, creator, createdReminder, (byte) 0);

            //saved the UserReminderEntity
            creator.getUserReminders().add(userReminderEntity);
            defaultReminderEntity.getUserReminders().add(userReminderEntity);
            this.userRepository.save(creator);
        }

        //if we got -1 from amount before from the frontend or amount before is null, it means that no additional reminder is wanted
        if (eventModel.getAmountBefore() != null && eventModel.getAmountBefore() != -1){
            ReminderEntity customReminderEntity = new ReminderEntity("creator_custom", (byte) 0, createdEvent,
                    eventModel.getSentTo(), eventModel.getTimeFormat(), eventModel.getAmountBefore());

            //saved the custom ReminderEntity
            createdEvent.getReminders().add(customReminderEntity);
            this.eventRepository.save(createdEvent);

            ReminderEntity createdCustomReminder = this.reminderService.getReminderByTypeAndEvent("creator_custom", createdEvent);

            UserReminderEntity customUserReminderEntity = new UserReminderEntity(null, creator, createdCustomReminder, (byte) 0);

            //saved the custom UserReminderEntity
            creator.getUserReminders().add(customUserReminderEntity);
            customReminderEntity.getUserReminders().add(customUserReminderEntity);
            this.userRepository.save(creator);
        }

        if (eventModel.getEventType().equals("group")){

            //saved ReminderEntity for actual invitation to an event
            long amountBeforeInvitation = ConversionsUtil.minutesBetweenSendAndEventStart(eventModel.getEventDate(), eventModel.getStartTime());
            ReminderEntity invitationReminder = new ReminderEntity("invitation", (byte) 1, createdEvent,
                    "email_notification", "minutes", amountBeforeInvitation);

            createdEvent.getReminders().add(invitationReminder);
            this.eventRepository.save(createdEvent);
            ReminderEntity createdInvitationReminder = this.reminderService.getReminderByTypeAndEvent("invitation", createdEvent);

            //saved ReminderEntity for the reminder to respond to the event
            long amountBeforeReminder = ConversionsUtil.daysBetween(eventModel.getLimitDate(), eventModel.getEventDate(),
                    eventModel.getStartTime());

            byte sent = (byte) 0;
            if (amountBeforeReminder == -1){
                //the respondTo reminder is sent if there is already at creating the event less than 1 day until the response limit
                sent = 1;
            }
            ReminderEntity respondToInvitationReminder = new ReminderEntity("to_respond", sent, createdEvent,
                    "email_notification", "days", amountBeforeReminder);

            createdEvent.getReminders().add(respondToInvitationReminder);
            this.eventRepository.save(createdEvent);
            ReminderEntity createdRespondToInvitationReminder = this.reminderService.getReminderByTypeAndEvent("to_respond", createdEvent);


            UserEntity invitedUser;
            UserReminderEntity invitationUserReminder;
            UserReminderEntity respondToInvitationUserReminder;
            InvitationEntity userInvitation;

            //need this to send bulk email to all invited users
            List<String> emailsOfInvitedUser = new ArrayList<>();

            for (Long userId: eventModel.getInviteeIDs()){
                invitedUser = this.userRepository.findById(userId).orElseThrow(
                        ()-> new UserNotFoundException("Invited User user not found!"));

                //add email to the list of emails
                emailsOfInvitedUser.add(invitedUser.getEmail());

                //saving UserReminderEntities both for invitation, as for response to invitation, for each user from the list
                invitationUserReminder = new UserReminderEntity(null, invitedUser, createdInvitationReminder, (byte) 0);
                respondToInvitationUserReminder = new UserReminderEntity(null, invitedUser, createdRespondToInvitationReminder, (byte) 0);

                invitedUser.getUserReminders().add(invitationUserReminder);

                invitedUser.getUserReminders().add(respondToInvitationUserReminder);

                this.userRepository.save(invitedUser);

                //saving the invitations to the created event
                userInvitation = new InvitationEntity(null, invitedUser, createdEvent, "pending");

                this.invitationRepository.save(userInvitation);
            }

            //sending an email with the invitation to all invited users at once
            this.reminderService.sendSameEmailToMultiple(emailsOfInvitedUser, createdInvitationReminder);

            //if there is already at creating the event less than 1 day until the response limit this reminder is sent after the
            //invitation
            if (amountBeforeReminder == -1){
                this.reminderService.sendSameEmailToMultiple(emailsOfInvitedUser, respondToInvitationReminder);
            }
        }
    }

    @Transactional
    public void updateGroupEventCreatedReminders(ReminderRequestModel reminderDetails, Long eventId, Long userId){

        UserEntity creator;

        EventEntity toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        List<String> reminderStatuses = Arrays.asList("creator_def", "creator_custom");

        //delete existing reminders
        this.reminderService.deleteForEventAndTypes(toUpdate, reminderStatuses);

        toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));


        //create new reminders if necessary
        if (reminderDetails.getDefaultReminder().equals("true")){

            ReminderEntity defaultReminderEntity = new ReminderEntity("creator_def", (byte) 0, toUpdate,
                    reminderDetails.getSentTo(), "days", 1L);


            //saved the ReminderEntity
            toUpdate.getReminders().add(defaultReminderEntity);
            this.eventRepository.save(toUpdate);

            ReminderEntity createdReminder = this.reminderService.getReminderByTypeAndEvent("creator_def", toUpdate);

            creator = userRepository.findById(userId).orElseThrow(
                    ()-> new UserNotFoundException("Creator User user not found!"));

            UserReminderEntity userReminderEntity = new UserReminderEntity(null, creator, createdReminder, (byte) 0);

            //saved the UserReminderEntity
            creator.getUserReminders().add(userReminderEntity);
            defaultReminderEntity.getUserReminders().add(userReminderEntity);
            this.userRepository.save(creator);
        }

        //if we got -1 from amount before from the frontend or amount before is null, it means that no additional reminder is wanted
        if (reminderDetails.getAmountBefore() != null && reminderDetails.getAmountBefore() != -1){

            creator = userRepository.findById(userId).orElseThrow(
                    ()-> new UserNotFoundException("Creator User user not found!"));

            toUpdate = eventRepository.findById(eventId).orElseThrow(
                    ()-> new EventException("EventNotFound!"));

            ReminderEntity customReminderEntity = new ReminderEntity("creator_custom", (byte) 0, toUpdate,
                    reminderDetails.getSentTo(), reminderDetails.getTimeFormat(), reminderDetails.getAmountBefore());

            //saved the custom ReminderEntity
            toUpdate.getReminders().add(customReminderEntity);
            this.eventRepository.save(toUpdate);

            ReminderEntity createdCustomReminder = this.reminderService.getReminderByTypeAndEvent("creator_custom", toUpdate);

            UserReminderEntity customUserReminderEntity = new UserReminderEntity(null, creator, createdCustomReminder, (byte) 0);

            //saved the custom UserReminderEntity
            creator.getUserReminders().add(customUserReminderEntity);
            customReminderEntity.getUserReminders().add(customUserReminderEntity);
            this.userRepository.save(creator);
        }


    }

    @Transactional
    public void updateGroupEventInvitedReminders(ReminderRequestModel reminderDetails, Long eventId, Long userId){

        UserEntity invitee;

        EventEntity toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        List<UserReminderEntity> userReminderListForReminder = this.userReminderRepository
                .findByUser_IdAndReminder_Event_IdAndReminder_Type(userId, eventId, "confirmed_def");

        if (userReminderListForReminder.size() > 1){
            throw new EventException("Can't be more then 1 userReminder with same reminder type for a user");
        }
        else{
            if (reminderDetails.getDefaultReminder().equals("true")){
                if (userReminderListForReminder.size() == 0){

                    ReminderEntity defaultReminderEntity = new ReminderEntity("confirmed_def", (byte) 0, toUpdate,
                            reminderDetails.getSentTo(), "days", 1L);


                    //saved the ReminderEntity
                    toUpdate.getReminders().add(defaultReminderEntity);
                    this.eventRepository.save(toUpdate);

                    ReminderEntity createdReminder = this.reminderService
                            .getLastAddedReminderByTypeAndEvent("confirmed_def", toUpdate);

                    invitee = userRepository.findById(userId).orElseThrow(
                            ()-> new UserNotFoundException("Creator User user not found!"));

                    UserReminderEntity userReminderEntity = new UserReminderEntity(null, invitee, createdReminder, (byte) 0);

                    //saved the UserReminderEntity
                    invitee.getUserReminders().add(userReminderEntity);
                    defaultReminderEntity.getUserReminders().add(userReminderEntity);
                    this.userRepository.save(invitee);
                }
            }
            else{
                //if the user doesn't want a default reminder anymore, it is deleted
                if (userReminderListForReminder.size() == 1){

                    this.reminderService.deleteById(userReminderListForReminder.get(0).getReminder().getId());

                    toUpdate = eventRepository.findById(eventId).orElseThrow(
                            ()-> new EventException("EventNotFound!"));
                }
            }

        }

        //working with the confirmed_custom reminder
        userReminderListForReminder = this.userReminderRepository
                .findByUser_IdAndReminder_Event_IdAndReminder_Type(userId, eventId, "confirmed_custom");

        if (userReminderListForReminder.size() > 1){
            throw new EventException("Can't be more then 1 userReminder with same reminder type for a user");
        }
        else{

            //deleting the old costume reminder if it exists
            if (userReminderListForReminder.size() == 1){

                this.reminderService.deleteById(userReminderListForReminder.get(0).getReminder().getId());

                toUpdate = eventRepository.findById(eventId).orElseThrow(
                        ()-> new EventException("EventNotFound!"));
            }

            if (reminderDetails.getAmountBefore() != null && reminderDetails.getAmountBefore() != -1){


                ReminderEntity defaultReminderEntity = new ReminderEntity("confirmed_custom", (byte) 0, toUpdate,
                        reminderDetails.getSentTo(), reminderDetails.getTimeFormat(), reminderDetails.getAmountBefore());


                //saved the ReminderEntity
                toUpdate.getReminders().add(defaultReminderEntity);
                this.eventRepository.save(toUpdate);

                ReminderEntity createdReminder = this.reminderService
                        .getLastAddedReminderByTypeAndEvent("confirmed_custom", toUpdate);

                invitee = userRepository.findById(userId).orElseThrow(
                        ()-> new UserNotFoundException("Creator User user not found!"));

                UserReminderEntity userReminderEntity = new UserReminderEntity(null, invitee, createdReminder, (byte) 0);

                //saved the UserReminderEntity
                invitee.getUserReminders().add(userReminderEntity);
                defaultReminderEntity.getUserReminders().add(userReminderEntity);
                this.userRepository.save(invitee);
            }
        }

    }
    @Transactional
    public void updateEvent(EventRequestModel eventModel, Long eventId){

        UserEntity creator;

        EventEntity toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        if (!toUpdate.getEventType().equals("single")){
            throw new EventException("Only simple events can be updated");
        }

        //delete existing reminders
        this.reminderService.deleteAllForEvent(toUpdate);

        toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        toUpdate.setName(eventModel.getName());
        toUpdate.setDescription(eventModel.getDescription());
        toUpdate.setEventDate(eventModel.getEventDate());
        toUpdate.setStartTime(eventModel.getStartTime());
        if (eventModel.getEndTime() != null){
            toUpdate.setEndTime(eventModel.getEndTime());
        }

        //saving the changes to the fields of the event
        this.eventRepository.save(toUpdate);

        toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        //create new reminders if necessary
        if (eventModel.getDefaultReminder().equals("true")){

            ReminderEntity defaultReminderEntity = new ReminderEntity("creator_def", (byte) 0, toUpdate,
                    eventModel.getSentTo(), "minutes", 30L);


            //saved the ReminderEntity
            toUpdate.getReminders().add(defaultReminderEntity);
            this.eventRepository.save(toUpdate);

            ReminderEntity createdReminder = this.reminderService.getReminderByTypeAndEvent("creator_def", toUpdate);

            creator = userRepository.findById(eventModel.getCreatorId()).orElseThrow(
                    ()-> new UserNotFoundException("Creator User user not found!"));

            UserReminderEntity userReminderEntity = new UserReminderEntity(null, creator, createdReminder, (byte) 0);

            //saved the UserReminderEntity
            creator.getUserReminders().add(userReminderEntity);
            defaultReminderEntity.getUserReminders().add(userReminderEntity);
            this.userRepository.save(creator);
        }

        //if we got -1 from amount before from the frontend or amount before is null, it means that no additional reminder is wanted
        if (eventModel.getAmountBefore() != null && eventModel.getAmountBefore() != -1){

            creator = userRepository.findById(eventModel.getCreatorId()).orElseThrow(
                    ()-> new UserNotFoundException("Creator User user not found!"));

            toUpdate = eventRepository.findById(eventId).orElseThrow(
                    ()-> new EventException("EventNotFound!"));

            ReminderEntity customReminderEntity = new ReminderEntity("creator_custom", (byte) 0, toUpdate,
                    eventModel.getSentTo(), eventModel.getTimeFormat(), eventModel.getAmountBefore());

            //saved the custom ReminderEntity
            toUpdate.getReminders().add(customReminderEntity);
            this.eventRepository.save(toUpdate);

            ReminderEntity createdCustomReminder = this.reminderService.getReminderByTypeAndEvent("creator_custom", toUpdate);

            UserReminderEntity customUserReminderEntity = new UserReminderEntity(null, creator, createdCustomReminder, (byte) 0);

            //saved the custom UserReminderEntity
            creator.getUserReminders().add(customUserReminderEntity);
            customReminderEntity.getUserReminders().add(customUserReminderEntity);
            this.userRepository.save(creator);
        }


    }

    @Transactional
    public void addNewInviteesToEvent(NewInviteesModel listNewInvitees, Long eventId){

        EventEntity event = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        UserEntity invitedUser;
        UserReminderEntity invitationUserReminder;
        UserReminderEntity respondToInvitationUserReminder;
        InvitationEntity userInvitation;

        //need this to send bulk email to all invited users
        List<String> emailsOfInvitedUser = new ArrayList<>();

        ReminderEntity invitationReminder = this.reminderService.getReminderByTypeAndEvent("invitation", event);
        ReminderEntity to_respondReminder = this.reminderService.getReminderByTypeAndEvent("to_respond", event);


        for (Long userId: listNewInvitees.getInviteeIDs()){

            invitedUser = this.userRepository.findById(userId).orElseThrow(
                    ()-> new UserNotFoundException("Invited User user not found!"));

            //add email to the list of emails
            emailsOfInvitedUser.add(invitedUser.getEmail());

            //saving UserReminderEntities both for invitation, as for response to invitation, for each user from the list
            //one at a time

            invitationUserReminder = new UserReminderEntity(null, invitedUser, invitationReminder, (byte) 0);
            respondToInvitationUserReminder = new UserReminderEntity(null, invitedUser, to_respondReminder, (byte) 0);

            invitedUser.getUserReminders().add(invitationUserReminder);

            invitedUser.getUserReminders().add(respondToInvitationUserReminder);


            this.userRepository.save(invitedUser);

            //saving the invitations to the created event
            userInvitation = new InvitationEntity(null, invitedUser, event, "pending");

            this.invitationRepository.save(userInvitation);
        }

        //sending an email with the invitation to all invited users at once
        this.reminderService.sendSameEmailToMultiple(emailsOfInvitedUser, invitationReminder);
    }

    public EventRequestModel getEventToUpdateForSingleEvent(Long eventId){
        EventEntity toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        boolean remindersExist = false;
        String defaultReminder = "false";
        String sendTo = "none";
        String timeFormat = "";
        Long amountBefore = -1L;


        if (this.reminderService.getReminderByTypeAndEventBool("creator_def", toUpdate)){
            defaultReminder = "true";
            ReminderEntity defaultReminderEntity = this.reminderService.getReminderByTypeAndEvent("creator_def", toUpdate);
            sendTo = defaultReminderEntity.getSendTo();
            remindersExist = true;
        }

        if (this.reminderService.getReminderByTypeAndEventBool("creator_custom", toUpdate)){
            ReminderEntity customReminderEntity = this.reminderService.getReminderByTypeAndEvent("creator_custom", toUpdate);
            sendTo = customReminderEntity.getSendTo();
            remindersExist = true;
            timeFormat = customReminderEntity.getTimeFormat();
            amountBefore = customReminderEntity.getAmountBefore();
        }

        EventRequestModel eventResponse = new EventRequestModel();

        eventResponse.setName(toUpdate.getName());
        eventResponse.setEventType(toUpdate.getEventType());
        eventResponse.setDescription(toUpdate.getDescription());
        eventResponse.setEventDate(toUpdate.getEventDate());
        eventResponse.setStartTime(toUpdate.getStartTime());

        if (toUpdate.getEndTime() != null){
            eventResponse.setEndTime(toUpdate.getEndTime());
        }
        else{
            eventResponse.setEndTime(null);
        }

        eventResponse.setCreatorId(toUpdate.getCreator().getId());
        eventResponse.setSentTo(sendTo);
        eventResponse.setDefaultReminder(defaultReminder);

        if (remindersExist){
            eventResponse.setTimeFormat(timeFormat);
            eventResponse.setAmountBefore(amountBefore);
        }

        return eventResponse;
    }

    public EventRequestModel getEventToUpdateForGroupEvent(Long eventId, Long userId, Integer relationToEvent){
        EventEntity toUpdate = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        boolean remindersExist = false;
        String defaultReminder = "false";
        String sendTo = "none";
        String timeFormat = "";
        Long amountBefore = -1L;

        String specificDefaultReminder;
        String specificCustomReminder;


        //1 means that the event was created by the given user, else the user was invited to this event
        if (relationToEvent == 1){
            specificDefaultReminder = "creator_def";
            specificCustomReminder = "creator_custom";
        }
        else{
            specificDefaultReminder = "confirmed_def";
            specificCustomReminder = "confirmed_custom";
        }

        List<UserReminderEntity> userReminderListForReminder = this.userReminderRepository
                .findByUser_IdAndReminder_Event_IdAndReminder_Type(userId, eventId, specificDefaultReminder);

        if (userReminderListForReminder.size() > 1){
            throw new EventException("Can't be more then 1 userReminder with same reminder type for a user");
        }
        else{
            if (userReminderListForReminder.size() == 1){
                defaultReminder = "true";
                sendTo = userReminderListForReminder.get(0).getReminder().getSendTo();
                remindersExist = true;
            }
        }

        userReminderListForReminder = this.userReminderRepository
                .findByUser_IdAndReminder_Event_IdAndReminder_Type(userId, eventId, specificCustomReminder);

        if (userReminderListForReminder.size() > 1){
            throw new EventException("Can't be more then 1 userReminder with same reminder type for a user");
        }
        else{
            if (userReminderListForReminder.size() == 1){

                sendTo = userReminderListForReminder.get(0).getReminder().getSendTo();
                remindersExist = true;
                timeFormat = userReminderListForReminder.get(0).getReminder().getTimeFormat();
                amountBefore = userReminderListForReminder.get(0).getReminder().getAmountBefore();
            }
        }

        EventRequestModel eventResponse = new EventRequestModel();

        eventResponse.setName(toUpdate.getName());
        eventResponse.setEventType(toUpdate.getEventType());
        eventResponse.setDescription(toUpdate.getDescription());
        eventResponse.setEventDate(toUpdate.getEventDate());
        eventResponse.setStartTime(toUpdate.getStartTime());

        if (toUpdate.getEndTime() != null){
            eventResponse.setEndTime(toUpdate.getEndTime());
        }
        else{
            eventResponse.setEndTime(null);
        }

        eventResponse.setLimitDate(toUpdate.getLimitDate());
        eventResponse.setCreatorId(toUpdate.getCreator().getId());
        eventResponse.setDefaultReminder(defaultReminder);
        eventResponse.setSentTo(sendTo);

        if (remindersExist){
            eventResponse.setTimeFormat(timeFormat);
            eventResponse.setAmountBefore(amountBefore);
        }

        return eventResponse;
    }

    public List<EventEntity> getAllEventsForUserInvitations(Long userId) {
        return eventRepository.findAllByInvitations_User_Id(userId);
    }

    public List<EventEntity> getAllEventForStatusAndUserWasInvitedTo(Long userId, String status){
        return eventRepository.findAllByInvitations_User_IdAndInvitations_Status(userId, status);
    }

    //private helper function
    private List<EventResponseModel> creatorEvents(List<EventEntity> creatorEvents) {
        return creatorEvents.stream()
                .map(event -> {
                    String specification = event.getEventType().equals("single") ? "created_single" : "created_group";
                    String isFuture =ConversionsUtil
                            .givenDateTimeIsAfterCurrentDateTime(event.getEventDate(), event.getStartTime());
                    return new EventResponseModel(event.getId(), event.getName(), event.getEventType(),
                            event.getDescription(), event.getEventDate(), event.getStartTime(), event.getEndTime(),
                            event.getLimitDate(), event.getCreator().getFirstName(), event.getCreator().getLastName(),
                            isFuture, specification);
                })
                .collect(Collectors.toList());
    }

    //sorted from earliest to latest
    public List<EventResponseModel> getUpcomingEventsSortedForUser(Long userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        //getting created events
        List<EventEntity> upcomingCreatorEvents =
                this.eventRepository.findAllByCreator_IdAndCanceledAndEventDateAfter(userId, (byte) 0, yesterday);


        for (EventEntity upcomingCreatorEvent: upcomingCreatorEvents){

            if (upcomingCreatorEvent.getEventType().equals("group") &&
                    !ConversionsUtil.givenDateTimeIsAfterRightNow(upcomingCreatorEvent.getLimitDate(), upcomingCreatorEvent.getStartTime())){
                for (InvitationEntity invitation: upcomingCreatorEvent.getInvitations()){
                    if (invitation.getStatus().equals("pending")){
                        invitation.setStatus("declined");
                        this.invitationRepository.save(invitation);
                    }
                }
            }
        }

        List<EventResponseModel> upcomingEventsCreated = this.creatorEvents(upcomingCreatorEvents);

        List<EventEntity> goingToInvitedToEvents =
                this.eventRepository.findAllByInvitations_User_IdAndInvitations_StatusAndEventDateAfter(userId, "accepted", yesterday);

        List<EventResponseModel> upcomingGoingToInvitedToEvents = goingToInvitedToEvents.stream()
                .map(event -> new EventResponseModel(event.getId(), event.getName(), event.getEventType(),
                        event.getDescription(), event.getEventDate(), event.getStartTime(), event.getEndTime(),
                        event.getLimitDate(), event.getCreator().getFirstName(), event.getCreator().getLastName(), ConversionsUtil
                        .givenDateTimeIsAfterCurrentDateTime(event.getEventDate(), event.getStartTime()),"invited_accepted"))
                .collect(Collectors.toList());

        return Stream.concat(upcomingEventsCreated.stream(), upcomingGoingToInvitedToEvents.stream())
                .sorted(Comparator.comparing(EventResponseModel::getEventDate).thenComparing(EventResponseModel::getStartTime))
                .collect(Collectors.toList());
    }


    //sorted from latest to earliest
    public List<EventResponseModel> getPastEventsSortedForUser(Long userId) {
        List<EventEntity> pastCreatorEvents =
                this.eventRepository.findAllByCreator_IdAndCanceledAndEventDateBefore(userId, (byte) 0, LocalDate.now());

        List<EventResponseModel> pastEventsCreated = creatorEvents(pastCreatorEvents);


        //declining past invitations
        for (EventEntity pastCreatorEvent: pastCreatorEvents){
            for (InvitationEntity invitation: pastCreatorEvent.getInvitations()){
                if (invitation.getStatus().equals("pending")){
                    invitation.setStatus("declined");
                    this.invitationRepository.save(invitation);
                }
            }
        }

        List<EventEntity> invitedToEvents =
                this.eventRepository
                        .findAllByInvitations_User_IdAndInvitations_StatusAndEventDateBefore(userId, "accepted",LocalDate.now());


        List<EventResponseModel> pastInvitedAnWentToEvents = invitedToEvents.stream()
                .map(event -> new EventResponseModel(event.getId(), event.getName(), event.getEventType(),
                        event.getDescription(), event.getEventDate(), event.getStartTime(), event.getEndTime(),
                        event.getLimitDate(), event.getCreator().getFirstName(), event.getCreator().getLastName(),
                        "false","invited_accepted"))
                .collect(Collectors.toList());

        return Stream.concat(pastEventsCreated.stream(), pastInvitedAnWentToEvents.stream())
                .sorted(Comparator.comparing(EventResponseModel::getEventDate,  Comparator.reverseOrder())
                .thenComparing(EventResponseModel::getStartTime, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    //sorted from earliest to latest
    public List<EventResponseModel> getToRespondEventsForUserSorted(Long userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<EventEntity> toRespondEvents =
                this.eventRepository
                        .findAllByEventTypeAndInvitations_User_IdAndInvitations_StatusAndLimitDateAfter("group", userId, "pending", yesterday);

       return toRespondEvents.stream()
               .filter(event -> ConversionsUtil.givenDateTimeIsAfterRightNow(event.getLimitDate(), event.getStartTime()))
                .map(event -> new EventResponseModel(event.getId(), event.getName(), event.getEventType(),
                        event.getDescription(), event.getEventDate(), event.getStartTime(), event.getEndTime(),
                        event.getLimitDate(), event.getCreator().getFirstName(), event.getCreator().getLastName(),
                        "true", "invited_pending"))
                .sorted(Comparator.comparing(EventResponseModel::getEventDate)
                        .thenComparing(EventResponseModel::getStartTime))
                .collect(Collectors.toList());
    }
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }


    @Transactional
    public void deleteByCreator(Long creatorId){
        UserEntity creator = userRepository.findById(creatorId).orElseThrow(
            ()-> new UserNotFoundException("First User user not found!"));

        this.eventRepository.deleteByCreator(creator);
    }

    @Transactional
    public void cancelUpcomingOrganizedEvent(Long eventId){
        EventEntity toCancel = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        LocalDateTime dateTimeOfEvent = ConversionsUtil.createDateTime(toCancel.getEventDate(), toCancel.getStartTime());

        if (dateTimeOfEvent.isBefore(LocalDateTime.now())){
            throw new EventException("The group Event started, so it can't be canceled!");
        }

        //first all reminders are deleted for this event
        this.reminderService.deleteAllForEvent(toCancel);

        EventEntity toCancelAfterRemindersDeleted = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        //second setting the cancelled flag to true for the event
        toCancelAfterRemindersDeleted.setCanceled((byte) 1);
        this.eventRepository.save(toCancelAfterRemindersDeleted);

        //when the reminder was sent in regard with the start time of the event
        long amountBeforeCancel = ConversionsUtil.minutesBetweenSendAndEventStart(toCancelAfterRemindersDeleted.getEventDate(), toCancelAfterRemindersDeleted.getStartTime());

        //added reminder for cancellation of the event
        ReminderEntity canceledReminder = new ReminderEntity("canceled", (byte) 1, toCancelAfterRemindersDeleted, "email_notification", "minutes", amountBeforeCancel);
        this.reminderService.saveReminder(canceledReminder);
        ReminderEntity canceledReminderAfterCreation = this.reminderService.getReminderByTypeAndEvent("canceled", toCancelAfterRemindersDeleted);

        List<InvitationEntity> invitations = this.invitationRepository.findByEventAndStatusNot(toCancelAfterRemindersDeleted, "declined");

        List<String> emailsToSendForCancellation = new ArrayList<>();
        UserReminderEntity userReminder;
        for(InvitationEntity invitation: invitations){
            userReminder = new UserReminderEntity(null, invitation.getUser(), canceledReminderAfterCreation, (byte) 0);

            emailsToSendForCancellation.add(invitation.getUser().getEmail());
            canceledReminderAfterCreation.getUserReminders().add(userReminder);
        }

        //send cancellation email to the invited user
        this.reminderService.sendSameEmailToMultiple(emailsToSendForCancellation, canceledReminderAfterCreation);

        //saving the UserReminder entities for each user that needs to get a cancellation reminder
        this.reminderService.saveReminder(canceledReminderAfterCreation);

        //delete all invitations for this event
        this.invitationRepository.deleteByEvent(toCancelAfterRemindersDeleted);

    }

    public void deleteSingleEvent(Long eventId){
        EventEntity toDelete = eventRepository.findById(eventId).orElseThrow(
                ()-> new EventException("EventNotFound!"));

        if (!toDelete.getEventType().equals("single")){
            throw new EventException("The to delete event is not of type single!!");
        }
        this.eventRepository.deleteById(eventId);

    }

    @Transactional
   public void deleteInvitationForPastEvents(Long eventId, Long userId){

        //delete confirmation mails of the user regardung the event
        List<UserReminderEntity> acceptedInvitationUserReminders =
                this.userReminderRepository.findByUser_IdAndReminder_Event_IdAndReminder_TypeIn(userId, eventId, Arrays
                        .asList("confirmed_def", "confirmed_custom"));

        for (UserReminderEntity userReminder: acceptedInvitationUserReminders){
            this.reminderService.deleteById(userReminder.getReminder().getId());
        }

        //first delete all UserReminders that are associated with the given event for the user
        this.userReminderRepository.deleteByUser_IdAndReminder_Event_Id(userId, eventId);
        List<InvitationEntity> invitationsByUserAndEvent = this.invitationRepository.findByUser_IdAndEvent_Id(userId, eventId);

       if (invitationsByUserAndEvent.size() != 1){
           throw new InvitationException("More or no invitations found for this user and event");
       }

       //instead of deleting an accepted invitation we just set its status to "deleted_accepted"
       InvitationEntity toDelete =  invitationsByUserAndEvent.get(0);

       toDelete.setStatus("deleted_accepted");

       this.invitationRepository.save(toDelete);
   }

   @Transactional
   public void deletePastOrganizedEvent(Long eventId, Long userId){
        EventEntity toDelete = this.eventRepository.findById(eventId)
                .orElseThrow( () -> new EventException("EventNotFound"));

       List<UserReminderEntity> creatorUserReminders =
               this.userReminderRepository.findByUser_IdAndReminder_Event_IdAndReminder_TypeIn(userId, eventId, Arrays
                       .asList("creator_def", "creator_custom"));

       for (UserReminderEntity userReminder: creatorUserReminders){
           this.reminderService.deleteById(userReminder.getReminder().getId());
       }

        //
       // cancellation flag sent to true
        toDelete.setCanceled((byte) 1);
        this.eventRepository.save(toDelete);
   }

}