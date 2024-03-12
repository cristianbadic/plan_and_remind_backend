package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.InvitationRepository;
import com.example.planAndRemind.Repository.UserReminderRepository;
import com.example.planAndRemind.dto.ReminderRequestModel;
import com.example.planAndRemind.exception.InvitationException;
import com.example.planAndRemind.exception.UserReminderException;
import com.example.planAndRemind.model.InvitationEntity;
import com.example.planAndRemind.model.ReminderEntity;
import com.example.planAndRemind.model.UserReminderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ReminderService reminderService;

    private final UserReminderRepository userReminderRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository, ReminderService reminderService,
                             UserReminderRepository userReminderRepository) {
        this.invitationRepository = invitationRepository;
        this.reminderService = reminderService;
        this.userReminderRepository = userReminderRepository;
    }

    public List<InvitationEntity> getAllInvitations() {
        return invitationRepository.findAll();
    }

    public void saveInvitation(InvitationEntity invitationEntity) {
        invitationRepository.save(invitationEntity);
    }

    public void deleteInvitationById(Long id) {
        invitationRepository.deleteById(id);
    }

    public InvitationEntity findInvitationByUserAndEvent(Long userId, Long eventId) {

        List<InvitationEntity> invitationByUserAndEvent = this.invitationRepository.findByUser_IdAndEvent_Id(userId, eventId);

        if (invitationByUserAndEvent.size() != 1){
            throw new InvitationException("More or no invitations found for this user and event");
        }

        return invitationByUserAndEvent.get(0);
    }

    public void declineInvitation(Long userId, Long eventId){
        InvitationEntity invitationToRespond = this.findInvitationByUserAndEvent(userId, eventId);

        //finding reminderEntity, to then delete the "respond_to" UserReminderEntity for user associated with invitation
        ReminderEntity toRespondReminder = this.reminderService.getReminderByTypeAndEvent("to_respond", invitationToRespond.getEvent());

        UserReminderEntity toDelete = userReminderRepository.findByUserAndReminder(invitationToRespond.getUser(), toRespondReminder)
                .orElseThrow(
                ()-> new UserReminderException("No to respond reminder found for this user"));
        this.userReminderRepository.deleteById(toDelete.getId());

        //actual declining
        invitationToRespond.setStatus("declined");
        this.invitationRepository.save(invitationToRespond);
    }

    public void acceptInvitation(Long userId, Long eventId, ReminderRequestModel reminderDetails){
        InvitationEntity invitationToRespond = this.findInvitationByUserAndEvent(userId, eventId);
        invitationToRespond.setStatus("accepted");

        //finding reminderEntity, to then delete the "respond_to" UserReminderEntity for user associated with invitation
        ReminderEntity toRespondReminder = this.reminderService.getReminderByTypeAndEvent("to_respond", invitationToRespond.getEvent());

        UserReminderEntity toDelete = userReminderRepository.findByUserAndReminder(invitationToRespond.getUser(), toRespondReminder)
                .orElseThrow(
                        ()-> new UserReminderException("No to respond reminder found for this user"));
        this.userReminderRepository.deleteById(toDelete.getId());

        if (reminderDetails.getDefaultReminder().equals("true")){

            String timeFormat = "days";
            long amountBefore = 1L;
            ReminderEntity defaultReminderEntity = new ReminderEntity("confirmed_def", (byte) 0, invitationToRespond.getEvent(),
                    reminderDetails.getSentTo(), timeFormat, amountBefore);

            //saved the default ReminderEntity
            invitationToRespond.getEvent().getReminders().add(defaultReminderEntity);
            this.reminderService.saveReminder(defaultReminderEntity);

            //getting the last just added reminder
            ReminderEntity createdDefaultReminder = this.reminderService.getLastAddedReminderByTypeAndEvent("confirmed_def", invitationToRespond.getEvent());

            UserReminderEntity defaultUserReminderEntity = new UserReminderEntity(null, invitationToRespond.getUser(), createdDefaultReminder, (byte) 0);

            //saved the UserReminderEntity
            this.userReminderRepository.save(defaultUserReminderEntity);
        }

        //if we got -1 from amount before from the frontend or amount before is null, it means that no additional reminder is wanted
        if (reminderDetails.getAmountBefore() != null && reminderDetails.getAmountBefore() != -1){
            ReminderEntity customReminderEntity = new ReminderEntity("confirmed_custom", (byte) 0, invitationToRespond.getEvent(),
                    reminderDetails.getSentTo(), reminderDetails.getTimeFormat(), reminderDetails.getAmountBefore());

            //saved the custom ReminderEntity
            this.reminderService.saveReminder(customReminderEntity);

            ReminderEntity createdCustomReminder = this.reminderService.getLastAddedReminderByTypeAndEvent("confirmed_custom",
                    invitationToRespond.getEvent());

            UserReminderEntity customUserReminderEntity = new UserReminderEntity(null, invitationToRespond.getUser(), createdCustomReminder, (byte) 0);

            //saved the UserReminderEntity
            this.userReminderRepository.save(customUserReminderEntity);
        }

        //updating the invitation to accept it
        this.invitationRepository.save(invitationToRespond);
    }

}