package com.example.planAndRemind.controller;

import com.example.planAndRemind.dto.*;
import com.example.planAndRemind.exception.*;
import com.example.planAndRemind.model.UserEntity;
import com.example.planAndRemind.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/get-all")
    public ResponseEntity<List<UserDTO>> getAllUser() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/get-all/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> findUserByEmailAndPassword(@PathVariable String email) {
        return ResponseEntity.ok(this.userService.findUserByEmail(email));
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> addUser(@RequestBody UserEntity userEntity) {
        try{
            this.userService.addUser(userEntity);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (NotConfirmedException e){
            return new ResponseEntity<>("The account associated with this email was not confirmed yet!",
                    HttpStatus.BAD_REQUEST);
        }
        catch (UserException e){
            return new ResponseEntity<>("There is already a user with this email!",
                    HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/register-phone-nr")
    public ResponseEntity<?> registerNumber(@RequestBody AddNumberRequestModel numberRequestModel) {
        try{
            this.userService.addOrUpdatePhoneNr(numberRequestModel);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>("User not found!",
                    HttpStatus.BAD_REQUEST);
        }
        catch (SamePhoneNumberException e){
            return new ResponseEntity<>("The given number is your currently conformed phone number.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/confirm-registration")
    public ResponseEntity<?> confirmRegistration(@RequestBody ConfirmRegistrationModel confirmRegistrationModel) {
        try{
            this.userService.confirmRegistration(confirmRegistrationModel);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>("No user registered with this email. " +
                    "Please register first, then confirm your registration.",
                    HttpStatus.BAD_REQUEST);
        }
        catch (UserException e){
            return new ResponseEntity<>("Registration was already confirmed for this email.",
                    HttpStatus.BAD_REQUEST);
        }
        catch (NotConfirmedException e){
            return new ResponseEntity<>("The confirmation code did not match!",
                    HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/confirm-phone-number")
    public ResponseEntity<?> confirmPhoneNr(@RequestBody ConfirmPhoneNrRequestModel confirmPhone) {
        try{
            this.userService.confirmPhoneNumber(confirmPhone);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>("No user registered with this email!",
                    HttpStatus.BAD_REQUEST);
        }
        catch (UserException e){
            return new ResponseEntity<>("This phone number was already confirmed",
                    HttpStatus.BAD_REQUEST);
        }
        catch (NotConfirmedException e){
            return new ResponseEntity<>("The confirmation code did not match!",
                    HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/resend-confirmation-mail/{email}")
    public ResponseEntity<?> resendRegistrationMail(@PathVariable String email) {

        try{
            this.userService.resendRegistrationMail(email);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>("No user registered with this email. " +
                    "Please register first, then confirm your registration.",
                    HttpStatus.BAD_REQUEST);
        }
        catch (UserException e){
            return new ResponseEntity<>("Registration was already confirmed for this email!",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/resend-confirmation-phone-nr")
    public ResponseEntity<?> resendConfirmationSms(@RequestBody AddNumberRequestModel updateNumber) {

        try{
            this.userService.resendConfirmationSms(updateNumber);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (UserNotFoundException | UserException e){
            return new ResponseEntity<>("This phone number was already confirmed.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequestModel updateInformation, @PathVariable Long id) {

        try{
            userService.updateUser(updateInformation, id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (InvalidPasswordException | DisabledUserException e){
            return new ResponseEntity<>("The provided current password does not correspond!",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestBody SearchRequest searchRequest) {
        List<UserDTO> users = userService.searchUsersToDTO(searchRequest);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search/{id}/{first}")
    public ResponseEntity<List<UserFriendRequestResponse>> searchUsersByOneName(@PathVariable Long id, @PathVariable String first) {

        String second ="";
        SearchRequest searchRequest = new SearchRequest(first, second);
        List<UserFriendRequestResponse> users = userService.searchUsersByUser(searchRequest,id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search/{id}/{first}/{second}")
    public ResponseEntity<List<UserFriendRequestResponse>> searchUsersByTwoNames(@PathVariable Long id, @PathVariable String first, @PathVariable String second) {

        SearchRequest searchRequest = new SearchRequest(first, second);
        List<UserFriendRequestResponse> users = userService.searchUsersByUser(searchRequest,id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/get-all/friends/{id}")
    public ResponseEntity<List<UserFriendRequestResponse>> getAllFriends(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllFriendsForUser(id));
    }

    @GetMapping("/get/still-possible-to-invite/{userId}/{eventId}")
    public ResponseEntity<List<UserFriendRequestResponse>> getStillPossibleToInviteFriends(@PathVariable Long userId,
                                                                                           @PathVariable Long eventId) {
        return ResponseEntity.ok(userService.getStillPossibleToInviteFriends(userId, eventId));
    }

    @GetMapping("/get-all/sent/{id}")
    public ResponseEntity<List<UserFriendRequestResponse>> getAllSentRequests(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllSentRequestsForUser(id));
    }

    @GetMapping("/get-all/received/{id}")
    public ResponseEntity<List<UserFriendRequestResponse>> getAllReceivedRequests(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllIncomingRequestsForUser(id));
    }

    @GetMapping("/get-all-users/{id}")
    public ResponseEntity<List<UserFriendRequestResponse>> getAllForUser(@PathVariable Long id) {
        List<UserFriendRequestResponse> users = userService.getAllForUser(id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/get-invited-users/{eventId}")
    public ResponseEntity<List<InvitedUserModel>> getUsersInvitedToEvent(@PathVariable Long eventId) {
        List<InvitedUserModel> users = userService.getUsersInvitedToEvent(eventId);
        return ResponseEntity.ok(users);
    }

}

