package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.FriendRequestRepository;
import com.example.planAndRemind.Repository.NotificationRepository;
import com.example.planAndRemind.Repository.UserRepository;
import com.example.planAndRemind.dto.FriendRequestModel;
import com.example.planAndRemind.exception.FriendRequestNotFoundException;
import com.example.planAndRemind.exception.UserNotFoundException;
import com.example.planAndRemind.model.FriendRequestEntity;
import com.example.planAndRemind.model.NotificationEntity;
import com.example.planAndRemind.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRepository;
    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

    @Autowired
    public FriendRequestService(FriendRequestRepository friendRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }


    public void createFriendRequest(FriendRequestModel friendRequestModel){

        //first sender, second is receiver

        Long firstId = friendRequestModel.getFirstUserId();
        Long secondId = friendRequestModel.getSecondUserId();
        String status = "pending_one_two";

        Long temp;
        if (firstId > secondId){
            temp = firstId;
            firstId = secondId;
            secondId = temp;
            status = "pending_two_one";
        }

        UserEntity firstUser = userRepository.findById(firstId).orElseThrow(
                ()-> new UserNotFoundException("First User user not found!"));

        UserEntity secondUser = userRepository.findById(secondId).orElseThrow(
                ()-> new UserNotFoundException("Second user user not found!"));

        FriendRequestEntity createdRequest = new FriendRequestEntity(null,firstUser, secondUser, status);

        firstUser.getFirstFriendRequests().add(createdRequest);

        this.userRepository.save(firstUser);


        NotificationEntity notification = new NotificationEntity();
        String message;
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSeen((byte) 0);

        if( status.equals("pending_one_two")){
            message = "You got a friend request from " + firstUser.getFirstName() + " " +firstUser.getLastName()
                    + " please check your Requests page";
            notification.setUser(secondUser);
            notification.setMessage(message);
            secondUser.getNotifications().add(notification);
            this.userRepository.save(secondUser);

        }
        else{
            message = "You got a friend request from " + secondUser.getFirstName() + " " +secondUser.getLastName()
                    + " please check your Requests page ";

            notification.setUser(firstUser);
            notification.setMessage(message);
            firstUser.getNotifications().add(notification);
            this.userRepository.save(firstUser);

        }

    }

    public List<FriendRequestEntity> getAllFriendRequests(){
        return this.friendRepository.findAll();
    }

    public void deleteFriendRequest(Long id){
        FriendRequestEntity friendRequestEntity = this.getFriendRequestById(id);

        UserEntity firstUser = friendRequestEntity.getFirstUser();

        UserEntity secondUser = friendRequestEntity.getSecondUser();

        System.out.println("!!!!!! cate notificari are user cu id1 inainte?" + firstUser.getFirstFriendRequests().size());

        firstUser.getFirstFriendRequests().remove(friendRequestEntity);
        secondUser.getSecondFriendRequests().remove(friendRequestEntity);

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        friendRepository.deleteById(id);

    }

    private FriendRequestEntity getFriendRequestById(Long id){
        return friendRepository.findById(id).orElseThrow(
                ()-> new FriendRequestNotFoundException("Something wrong at finding friend request!"));
    }
    public void updateFriendRequest(Long id) {

        String status = "accepted";

        FriendRequestEntity toUpdate= this.getFriendRequestById(id);

        UserEntity firstUser = toUpdate.getFirstUser();

        UserEntity secondUser = toUpdate.getSecondUser();

        UserEntity sender;
        UserEntity receiver;

        NotificationEntity notificationToSender = new NotificationEntity();
        NotificationEntity notificationToReceiver = new NotificationEntity();
        String messageSender;
        String messageReceiver;

        sender =  firstUser;
        receiver = secondUser;
        if (toUpdate.getStatus().equals("pending_two_one")){
            sender = secondUser;
            receiver = firstUser;
        }

        notificationToSender.setUser(sender);
        messageSender = "Your friend request to " + receiver.getFirstName() + " " +receiver.getLastName()
                    + " was excepted.";

        notificationToSender.setMessage(messageSender);
        notificationToSender.setCreatedAt(LocalDateTime.now());
        notificationToSender.setSeen((byte) 0);

        sender.getNotifications().add(notificationToSender);
        this.userRepository.save(sender);
        //this.notificationRepository.save(notificationToSender);

        notificationToReceiver.setUser(receiver);
        messageReceiver = "You are now friends with " + sender.getFirstName() + " " + sender.getLastName()
                    + ".";

        notificationToReceiver.setMessage(messageReceiver);
        notificationToReceiver.setCreatedAt(LocalDateTime.now());
        notificationToReceiver.setSeen((byte) 0);

        receiver.getNotifications().add(notificationToReceiver);
        this.userRepository.save(receiver);

        toUpdate.setStatus(status);

        this.friendRepository.save(toUpdate);
    }
}
