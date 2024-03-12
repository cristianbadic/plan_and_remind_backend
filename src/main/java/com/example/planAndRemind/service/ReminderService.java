package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.NotificationRepository;
import com.example.planAndRemind.Repository.ReminderRepository;
import com.example.planAndRemind.Repository.UserReminderRepository;
import com.example.planAndRemind.dto.NotificationsRemindersResponseModel;
import com.example.planAndRemind.dto.SmsDetailsModel;
import com.example.planAndRemind.exception.NotificationException;
import com.example.planAndRemind.exception.ReminderException;
import com.example.planAndRemind.model.EventEntity;
import com.example.planAndRemind.model.NotificationEntity;
import com.example.planAndRemind.model.ReminderEntity;
import com.example.planAndRemind.model.UserReminderEntity;
import com.example.planAndRemind.util.ConversionsUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserReminderRepository userReminderRepository;

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;

    private final SmsSenderService smsSenderService;

    @Autowired
    public ReminderService(ReminderRepository reminderRepository, UserReminderRepository userReminderRepository,
                           NotificationRepository notificationRepository, EmailSenderService emailSenderService,
                           SmsSenderService smsSenderService) {
        this.reminderRepository = reminderRepository;
        this.userReminderRepository = userReminderRepository;
        this.notificationRepository = notificationRepository;
        this.emailSenderService = emailSenderService;
        this.smsSenderService = smsSenderService;
    }

    public List<ReminderEntity> getAllReminders(){
        return this.reminderRepository.findAll();
    }

    public ReminderEntity getReminderByTypeAndEvent(String type, EventEntity event){
        List<ReminderEntity> createdReminderList = this.reminderRepository.findByTypeAndEvent(type, event);

        if (createdReminderList.size() != 1){
            throw new ReminderException("Some problem at creating the reminder, the size was" + createdReminderList.size());
        }

        return createdReminderList.get(0);
    }

    public boolean getReminderByTypeAndEventBool(String type, EventEntity event){
        List<ReminderEntity> createdReminderList = this.reminderRepository.findByTypeAndEvent(type, event);

        return createdReminderList.size() == 1;
    }

    //used for confirmation reminder
    public ReminderEntity getLastAddedReminderByTypeAndEvent(String type, EventEntity event){
        Optional<ReminderEntity> createdReminder = this.reminderRepository.findTopByTypeAndEventOrderByIdDesc(type, event);

        if (!createdReminder.isPresent()){
            throw new ReminderException("Couldn't find recently created reminder");
        }

        return createdReminder.get();
    }
    public List<ReminderEntity> getRemindersByEventId(Long eventId){
        return this.reminderRepository.findByEvent_Id(eventId);
    }

    public void deleteAllForEvent(EventEntity event){
        this.reminderRepository.deleteByEvent(event);
    }

    public void deleteForEventAndTypes(EventEntity event, List<String> types){
        this.reminderRepository.deleteByEventAndTypeIn(event, types);
    }


    public void saveReminder(ReminderEntity reminder){
        this.reminderRepository.save(reminder);
    }

    public void deleteById(Long reminderId){
        this.reminderRepository.deleteById(reminderId);
    }


    public String createMessage(ReminderEntity reminderEntity){

        String resultedMessage;
        String timeFormatForReminder = reminderEntity.getTimeFormat();

        if (reminderEntity.getAmountBefore() == 1){
            switch (reminderEntity.getTimeFormat()){
                case "minutes":
                    timeFormatForReminder = "minute";
                    break;

                case "hours":
                    timeFormatForReminder = "hour";
                    break;

                case "days":
                    timeFormatForReminder = "day";
                    break;

                default:
                    throw new ReminderException("time format was different than the 3 allowed formats");
            }
        }

        switch (reminderEntity.getType()) {

            case "creator_def":

                resultedMessage = "Your event \"" + reminderEntity.getEvent().getName() + "\" will take place in less than "
                        + reminderEntity.getAmountBefore() + " " + timeFormatForReminder + "!";
                break;

            case "creator_custom":

                resultedMessage = "Your event \"" + reminderEntity.getEvent().getName() + "\" will take place in less than "
                        + reminderEntity.getAmountBefore() + " " + timeFormatForReminder + " !";
                break;

            case "invitation":

                resultedMessage = "You were invited to the event \"" + reminderEntity.getEvent().getName()
                        + "\" by " + reminderEntity.getEvent().getCreator().getFirstName() + " "
                        + reminderEntity.getEvent().getCreator().getLastName()
                        + ", it will take place on " + reminderEntity.getEvent().getEventDate() + " starting at "
                        + reminderEntity.getEvent().getStartTime() +
                        ". Please check the app for additional information and to respond to the invitation!"
                        + " You will get a reminder to respond to the event, one day before the limit response time.";
                break;

            case "to_respond":
                resultedMessage = "There is less then one day left to respond to the invitation to \"" + reminderEntity.getEvent().getName()
                        + "\" by " + reminderEntity.getEvent().getCreator().getFirstName() + " "
                        + reminderEntity.getEvent().getCreator().getLastName()
                        +". If no response is given by the limit response date, your invitation will be automatically declined!";
                break;

            case "confirmed_def":

                resultedMessage = "The event \"" + reminderEntity.getEvent().getName() + "\" organized by "
                        + reminderEntity.getEvent().getCreator().getFirstName() + " "
                        + reminderEntity.getEvent().getCreator().getLastName()
                        + " will start tomorrow at this time !";
                break;

            case "confirmed_custom":

                resultedMessage = "The event \"" + reminderEntity.getEvent().getName() + "\" organized by "
                        + reminderEntity.getEvent().getCreator().getFirstName() + " "
                        + reminderEntity.getEvent().getCreator().getLastName()
                        + " will take place in less than!" + reminderEntity.getAmountBefore() + " " + timeFormatForReminder + "!";
                break;

            case "canceled":

                resultedMessage = "The event \"" + reminderEntity.getEvent().getName() + "\" for which you where invited to by "
                        + reminderEntity.getEvent().getCreator().getFirstName() + " "
                        + reminderEntity.getEvent().getCreator().getLastName()
                        + " was canceled. As a result it was also removed from your schedule.";
                break;

            default:
                throw new ReminderException("Unknown reminder type!");
        }

        return resultedMessage;
    }

    public String createSubject(ReminderEntity reminderEntity){

        String resultedSubject;

        switch (reminderEntity.getType()) {

            case "creator_def":

                resultedSubject = "Plan & Remind: Reminder for your event: \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            case "creator_custom":

                resultedSubject = "Plan & Remind: Reminder for your event \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            case "invitation":

                resultedSubject = "Plan & Remind: Invitation to event \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            case "to_respond":
                resultedSubject = "Plan & Remind: Respond to the event: \"" + reminderEntity.getEvent().getName() + "\" reminder";
                break;

            case "confirmed_def":

                resultedSubject = "Plan & Remind: Default Reminder for the event: \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            case "confirmed_custom":

                resultedSubject = "Plan & Remind: Reminder for the event: \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            case "canceled":

                resultedSubject = "Plan & Remind: Cancellation of the event \"" + reminderEntity.getEvent().getName() + "\"";
                break;

            default:
                throw new ReminderException("Unknown reminder type!");
        }

        return resultedSubject;
    }

   public List<NotificationsRemindersResponseModel> getAllSentNotificationsAndRemindersForUser(Long userId){

        List<UserReminderEntity> sentUserReminders = userReminderRepository
                .findByUser_IdAndReminder_SentAndReminder_SendToIn(userId, (byte) 1, Arrays
                        .asList("email_notification", "sms_notification", "all_options", "notification"));

        //adding the reminders with their message and send time to the response
        List<NotificationsRemindersResponseModel> remindersAndNotifications = sentUserReminders.stream()
                .map(userReminder -> {
                    LocalDate eventDate = userReminder.getReminder().getEvent().getEventDate();
                    LocalTime eventStatTime = userReminder.getReminder().getEvent().getStartTime();
                    String timeFormat = userReminder.getReminder().getTimeFormat();
                    Long amountBefore =  userReminder.getReminder().getAmountBefore();

                    return new NotificationsRemindersResponseModel(userReminder.getReminder().getId(),
                            this.createMessage(userReminder.getReminder()),
                            userReminder.getSeen(),
                            ConversionsUtil.sentTimestamp(eventDate, eventStatTime, timeFormat, amountBefore),
                            "reminder");
                })
                .collect(Collectors.toList());

        List<NotificationEntity> notificationsForUser = this.notificationRepository.findAllByUser_Id(userId);

        for (NotificationEntity notification: notificationsForUser){
            remindersAndNotifications.add(new NotificationsRemindersResponseModel(notification.getId(),
                    notification.getMessage(), notification.getSeen(), notification.getCreatedAt(), "notification"));
        }

        remindersAndNotifications.sort(Comparator.comparing(NotificationsRemindersResponseModel::getCreatedAt,
                Comparator.reverseOrder()));
        return remindersAndNotifications;
    }

    public void sendSms(String phone_nr, ReminderEntity reminderEntity){
        String messageToSend = this.createMessage(reminderEntity);
        String messageToSendWithTitle = "Plan and Remind: " + messageToSend;
        SmsDetailsModel smsDetails = new SmsDetailsModel(messageToSendWithTitle,"spring boot app", phone_nr);
        this.smsSenderService.sendSmsToUser(smsDetails);
    }
    public void sendEmail(String userMail, ReminderEntity reminderEntity){
        String messageToSend = this.createMessage(reminderEntity);
        String subjectOfEmail = this.createSubject(reminderEntity);
        this.emailSenderService.sendSimpleEmail(userMail, subjectOfEmail, messageToSend);

    }

    public void sendSameEmailToMultiple(List<String> userEmails, ReminderEntity reminderEntity){

        String messageToSend = this.createMessage(reminderEntity);
        String subjectOfEmail = this.createSubject(reminderEntity);
        this.emailSenderService.sendEmailToMultipleUsers(userEmails, subjectOfEmail, messageToSend);
    }

    @Transactional
    public void deleteNotificationOrReminder(Long notificationOrReminderId, Long userId, String type){
        if (type.equals("notification")){
            this.notificationRepository.deleteById(notificationOrReminderId);
        }
        else if (type.equals("reminder")){
            long nrUserRemindersForReminder = this.userReminderRepository.countByReminder_Id(notificationOrReminderId);

            //if there is only one UserReminderEntity for the given reminderId, we can delete directly delete the "ReminderEntity"
            //else only the UserReminderEntity
            if (nrUserRemindersForReminder == 1){
                this.reminderRepository.deleteById(notificationOrReminderId);
            }
            else{
                this.userReminderRepository.deleteByUser_IdAndReminder_Id(userId, notificationOrReminderId);
            }
        }
        else{
            throw new ReminderException("The to delete didn't have nether the notification nor the reminder type!");
        }
    }

    @Transactional
    public void seenNotificationOrReminder(Long notificationOrReminderId, Long userId, String type){
        if (type.equals("notification")){

            Optional<NotificationEntity> seenNotification = notificationRepository.findById(notificationOrReminderId);

            if (!seenNotification.isPresent()){
                throw new NotificationException("Couldn't find recently created notification");
            }
            NotificationEntity actualSeenNotification = seenNotification.get();
            actualSeenNotification.setSeen((byte) 1);
            notificationRepository.save(actualSeenNotification);
        }
        else if (type.equals("reminder")){

            Optional<UserReminderEntity> seenUserReminder = userReminderRepository
                    .findByUser_IdAndReminder_Id(userId, notificationOrReminderId);

            if (!seenUserReminder.isPresent()){
                throw new NotificationException("Couldn't find recently created reminder");
            }
            UserReminderEntity actualSeenUserReminder = seenUserReminder.get();
            actualSeenUserReminder.setSeen((byte) 1);
            userReminderRepository.save(actualSeenUserReminder);
        }
        else{
            throw new ReminderException("The to delete didn't have nether the notification nor the reminder type!");
        }
    }

    @Transactional
    public void deleteAllNotificationsAndRemindersForUser(Long userId){
        this.notificationRepository.deleteByUser_Id(userId);
        List<UserReminderEntity> sentUserRemindersForUser = this.userReminderRepository.findByUser_IdAndReminder_Sent(userId, (byte) 1);

        for (UserReminderEntity userReminder: sentUserRemindersForUser){

            long nrUserRemindersForReminder = this.userReminderRepository.countByReminder_Id(userReminder.getReminder().getId());

            //if there is only one UserReminderEntity for the given reminderId, we can delete directly delete the "ReminderEntity"
            //otherwise only the UserReminderEntity
            if (nrUserRemindersForReminder == 1){
                this.reminderRepository.deleteById(userReminder.getReminder().getId());
            }
            else{
                this.userReminderRepository.deleteByUser_IdAndReminder_Id(userId, userReminder.getReminder().getId());
            }

        }
    }

    //scheduling method that starts running after the last instance stopped
    //used to send the planed unsent reminders

    @Scheduled(fixedDelay = 10000)
    public void sendReminders(){

        long startTime = System.currentTimeMillis();

       // not yet send reminders
        List<ReminderEntity> notSentReminders= this.reminderRepository.findBySent((byte) 0);

        LocalDate eventDate;
        LocalTime eventTime;
        String timeFormat;
        Long amountBefore;
        LocalDateTime timestampForSending;

        //10 minutes before current time stamp
        LocalDateTime currentLocalDateTimeWithErrorMargin = LocalDateTime.now().minusMinutes(10);

        LocalDateTime currentLocalDateTime = LocalDateTime.now();

        for (ReminderEntity notSentReminder: notSentReminders){
            eventDate = notSentReminder.getEvent().getEventDate();
            eventTime = notSentReminder.getEvent().getStartTime();
            timeFormat = notSentReminder.getTimeFormat();
            amountBefore = notSentReminder.getAmountBefore();
            timestampForSending = ConversionsUtil.sentTimestamp(eventDate,eventTime,timeFormat,amountBefore);

            if (timestampForSending.isBefore(currentLocalDateTimeWithErrorMargin)){
                this.reminderRepository.deleteById(notSentReminder.getId());
            }
            else{
                //if the filtered reminder was supposed to be sent before the current timestamp it is sent
                if (timestampForSending.isBefore(currentLocalDateTime)){

                    //checking if the reminder wasn't deleted since the reminders where fetched from the db
                    Optional<ReminderEntity> optionalReminder = this.reminderRepository.findById(notSentReminder.getId());

                    if (optionalReminder.isPresent()) {
                        ReminderEntity checkedNotSendReminder = optionalReminder.get();

                        checkedNotSendReminder.setSent((byte) 1);
                        this.reminderRepository.save(checkedNotSendReminder);

                        String reminderSendTo = checkedNotSendReminder.getSendTo();
                        if (!reminderSendTo.equals("notification") && !reminderSendTo.equals("none")){

                            List<UserReminderEntity> userReminderEntities = this.userReminderRepository
                                    .findByReminder(checkedNotSendReminder);

                            List<String> emailsToSendTo = new ArrayList<>();
                            List<String> numbersToSendTo = new ArrayList<>();

                            for (UserReminderEntity userReminder: userReminderEntities){
                                emailsToSendTo.add(userReminder.getUser().getEmail());
                                if (userReminder.getUser().getPhoneNumber() != null){
                                    numbersToSendTo.add(userReminder.getUser().getPhoneNumber());
                                }
                            }

                            //sending the reminder through email for all users associated to this reminder
                            if (reminderSendTo.equals("email") || reminderSendTo.equals("email_notification")){

                                this.sendSameEmailToMultiple(emailsToSendTo, checkedNotSendReminder);
                            }

                            if (reminderSendTo.equals("sms") || reminderSendTo.equals("sms_notification")){

                                if (numbersToSendTo.size() == 1){
                                    this.sendSms(numbersToSendTo.get(0), checkedNotSendReminder);
                                }
                            }


                            if (reminderSendTo.equals("sms_email") || reminderSendTo.equals("all_options")){


                                if (numbersToSendTo.size() == 1){
                                    this.sendSms(numbersToSendTo.get(0), checkedNotSendReminder);
                                    this.sendEmail(emailsToSendTo.get(0), checkedNotSendReminder);
                                }
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTimeInMillis = endTime - startTime;
        long elapsedTimeInSeconds = elapsedTimeInMillis / 1000;

        System.out.println("Execution time: " + elapsedTimeInSeconds + " seconds");
    }
}
