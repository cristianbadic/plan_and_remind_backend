package com.example.planAndRemind.util;

import com.example.planAndRemind.dto.CheckAvailabilityRequestModel;
import com.example.planAndRemind.exception.ReminderException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class ConversionsUtil {
    private ConversionsUtil() {

    }


    public static long daysBetween(LocalDate limitDate, LocalDate eventDate, LocalTime startTime) {
        //used for the "to_respond" reminder, it is also the reason why 1 is subtracted
        LocalDate reminderDate = limitDate.minusDays(1);

        LocalDateTime reminderDateTime = reminderDate.atTime(startTime);

        LocalDateTime currentDateTime = LocalDateTime.now();

        System.out.println("The reminder date time is: " + reminderDateTime);
        System.out.println("The current date time is " + currentDateTime);
        if (reminderDateTime.isBefore(currentDateTime)){
            System.out.println("O dat ca ii inainte" + currentDateTime);

            return -1;
        }
        return ChronoUnit.DAYS.between(reminderDate, eventDate);
    }
    public static LocalDateTime createDateTime(LocalDate date, LocalTime time){

        return date.atTime(time);
    }

    // if true the event will take place in the future
    public static boolean givenDateTimeIsAfterRightNow(LocalDate givenDate, LocalTime givenTime){
        LocalDateTime localDateTime = createDateTime(givenDate, givenTime);
        return localDateTime.isAfter(LocalDateTime.now());
    }

    public static String givenDateTimeIsAfterCurrentDateTime(LocalDate givenDate, LocalTime givenTime){
        LocalDateTime localDateTime = createDateTime(givenDate, givenTime);
        if (localDateTime.isAfter(LocalDateTime.now())){
            return "true";
        }
        return "false";
    }


    public static boolean availableInterval(CheckAvailabilityRequestModel newEventRequest,
                                            LocalDate existingStartDate, LocalTime existingStartTime, LocalTime existingEndTime){

        //if the dates of the events are different, there can be no overlap
        if (!newEventRequest.getEventDate().equals(existingStartDate)){
            return true;
        }

        if (newEventRequest.getEndTime() != null){
            if (existingEndTime != null){
                boolean newStartsBeforeExistingEnds = newEventRequest.getStartTime().isBefore(existingEndTime);

                boolean newEndsAfterExistingStarts = newEventRequest.getEndTime().isAfter(existingStartTime);

                //both conditions need to be true for an overlap to be present
                return !(newStartsBeforeExistingEnds && newEndsAfterExistingStarts);
            }

            if (newEventRequest.getStartTime().equals(existingStartTime)){
                return false;
            }

            boolean existingStartTimeInside = existingStartTime.isAfter(newEventRequest.getStartTime()) &&
                    existingStartTime.isBefore(newEventRequest.getEndTime());
            return !(existingStartTimeInside);

        }
        else{
            if (existingEndTime != null){
                if (newEventRequest.getStartTime().equals(existingStartTime)){
                    return false;
                }
                boolean existingStartTimeInside = newEventRequest.getStartTime().isAfter(existingStartTime) &&
                        newEventRequest.getStartTime().isBefore(existingEndTime);
                return !(existingStartTimeInside);
            }
            else{
                return !(newEventRequest.getStartTime().equals(existingStartTime));
            }
        }
    }
    public static long minutesBetweenSendAndEventStart(LocalDate eventDate, LocalTime startTime){

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = createDateTime(eventDate, startTime);

        long minutes = ChronoUnit.MINUTES.between(now, eventStart);


        return minutes;
    }

    public static LocalDateTime sentTimestamp(LocalDate eventDate, LocalTime startTime, String timeType, Long amountBefore) {

        //if amountBefore is negative just returning an old date
        if (amountBefore < 0) {
            return LocalDateTime.of(2020, 7, 5, 15, 0);
        }

        LocalDateTime eventStartDateTime = createDateTime(eventDate, startTime);

        switch (timeType) {
            case "minutes":
                eventStartDateTime = eventStartDateTime.minusMinutes(amountBefore);
                break;
            case "hours":
                eventStartDateTime = eventStartDateTime.minusHours(amountBefore);
                break;
            case "days":
                eventStartDateTime = eventStartDateTime.minusDays(amountBefore);
                break;

            default:
                throw new ReminderException("Unknown time format!");
        }

        return eventStartDateTime;
    }

}
