package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.FriendRequestRepository;
import com.example.planAndRemind.Repository.InvitationRepository;
import com.example.planAndRemind.Repository.UserRepository;
import com.example.planAndRemind.dto.*;
import com.example.planAndRemind.exception.*;
import com.example.planAndRemind.mapper.UserMapper;
import com.example.planAndRemind.model.FriendRequestEntity;
import com.example.planAndRemind.model.InvitationEntity;
import com.example.planAndRemind.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final FriendRequestRepository friendRepository;

    private final InvitationRepository invitationRepository;

    private final EmailSenderService emailSenderService;

    private final SmsSenderService smsSenderService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
                       FriendRequestRepository friendRepository, InvitationRepository invitationRepository,
                       EmailSenderService emailSenderService, SmsSenderService smsSenderService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.friendRepository = friendRepository;
        this.invitationRepository = invitationRepository;
        this.emailSenderService = emailSenderService;
        this.smsSenderService = smsSenderService;
        this.authenticationManager = authenticationManager;
    }

    public void addUser(UserEntity userEntity) {

        String confirmationCode = this.emailSenderService.generateRegistrationCode();


        Optional<UserEntity> foundUser = this.userRepository.findByEmail(userEntity.getEmail());

        if (foundUser.isPresent()){
            if (foundUser.get().getAccountConfirmation().equals("1")){
                throw new UserException("There is already a user with this email!");
            }
            else{
                throw new NotConfirmedException("The account associated with this email was not confirmed yet!");
            }
        }

        userEntity.setAccountConfirmation(confirmationCode);
        userEntity.setPassword(this.getEncodedPassword(userEntity.getPassword()));
        userEntity.setPhoneNrConfirmation("0");
        userEntity.setPhoneNumber("0");
        userRepository.save(userEntity);

        this.emailSenderService.sendConfirmRegistrationEmail(userEntity.getEmail(), confirmationCode);
    }

    public void updateUser(UpdateUserRequestModel updateInformation, Long userId) {

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                ()-> new UserNotFoundException("User not found!"));

        userEntity.setFirstName(updateInformation.getFirstName());
        userEntity.setLastName(updateInformation.getLastName());
        userEntity.setBirthDate(updateInformation.getBirthDate());
        userEntity.setImageUrl(updateInformation.getImageUrl());

        if (updateInformation.getUpdatePassword() == 1){
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEntity.getEmail(), updateInformation.getOldPassword()));
            } catch (DisabledException ex) {
                throw new DisabledUserException("User is disabled");
            } catch (BadCredentialsException ex) {
                throw new InvalidPasswordException("The provided password is not the current one!");
            }

            userEntity.setPassword(this.getEncodedPassword(updateInformation.getNewPassword()));
        }

        userRepository.save(userEntity);
    }

    public void addOrUpdatePhoneNr(AddNumberRequestModel numberRequestModel){
        UserEntity userEntity = userRepository.findById(numberRequestModel.getId()).orElseThrow(
                ()-> new UserNotFoundException("User not found!"));

        if (userEntity.getPhoneNumber().equals(numberRequestModel.getPhoneNumber()) &&
            userEntity.getPhoneNrConfirmation().equals("1")){
            throw new SamePhoneNumberException("The given number is your currently conformed phone number.");
        }

        String confirmationCode = this.emailSenderService.generateRegistrationCode();

        userEntity.setPhoneNrConfirmation(confirmationCode);
        userEntity.setPhoneNumber(numberRequestModel.getPhoneNumber());
        this.userRepository.save(userEntity);

        this.smsSenderService.sendConfirmationSms(numberRequestModel.getPhoneNumber(), confirmationCode);

    }

    public void confirmRegistration(ConfirmRegistrationModel confirmRegistration){

        UserEntity userEntity = userRepository.findByEmail(confirmRegistration.getEmail()).orElseThrow(
                ()-> new UserNotFoundException("No user registered with this email!"));

        if (userEntity.getAccountConfirmation().equals("1")){
            throw new UserException("Registration was already confirmed for this email.");
        }

        if (userEntity.getAccountConfirmation().equals(confirmRegistration.getAccountConfirmation())){
            userEntity.setAccountConfirmation("1");
            this.userRepository.save(userEntity);
        }
        else{
            throw new NotConfirmedException("The confirmation code did not match!");
        }

    }

    public void confirmPhoneNumber(ConfirmPhoneNrRequestModel confirmPhone){

        UserEntity userEntity = userRepository.findById(confirmPhone.getId()).orElseThrow(
                ()-> new UserNotFoundException("No user registered with this email!"));

        if (userEntity.getPhoneNrConfirmation().equals("1")){
            throw new UserException("This phone number was already confirmed.");
        }

        if (userEntity.getPhoneNrConfirmation().equals(confirmPhone.getPhoneNrConfirmation())){
            userEntity.setPhoneNrConfirmation("1");
            this.userRepository.save(userEntity);
        }
        else{
            throw new NotConfirmedException("The confirmation code did not match!");
        }

    }

    public void resendRegistrationMail(String email){

        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                ()-> new UserNotFoundException("No user registered with this email!"));

        if (userEntity.getAccountConfirmation().equals("1")){
            throw new UserException("Registration was already confirmed for this email!");
        }

        String newConfirmationCode = this.emailSenderService.generateRegistrationCode();
        userEntity.setAccountConfirmation(newConfirmationCode);
        this.userRepository.save(userEntity);

        this.emailSenderService.sendConfirmRegistrationEmail(userEntity.getEmail(), newConfirmationCode);
    }

    public void resendConfirmationSms(AddNumberRequestModel updateNumber){

        UserEntity userEntity = userRepository.findById(updateNumber.getId()).orElseThrow(
                ()-> new UserNotFoundException("No user registered with this id!"));

        if (userEntity.getPhoneNrConfirmation().equals("1")){
            throw new UserException("This phone number was already confirmed.");
        }

        String newConfirmationCode = this.emailSenderService.generateRegistrationCode();
        userEntity.setPhoneNrConfirmation(newConfirmationCode);
        this.userRepository.save(userEntity);

        this.smsSenderService.sendConfirmationSms(userEntity.getPhoneNumber(), newConfirmationCode);

    }

    public List<UserDTO> findAllUsers() {
        return userMapper.usersListTODtos(userRepository.findAll());
    }


    public UserDTO findUserById(Long id){
        return userMapper.userToDto(userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found")));
    }

    public void deleteUser(Long id){

        userRepository.deleteById(id);

    }

    public UserDTO findUserByEmail(String email) {
        return userMapper.userToDto(userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("user with this Email not found")));
    }

    public List<UserEntity> searchUsers(SearchRequest searchRequest) {
        String firstName = searchRequest.getFirstName();
        String secondName = searchRequest.getSecondName();

        List<UserEntity> users1 = userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndAccountConfirmation(firstName, firstName, "1");

        Set<UserEntity> mergedFoundUsers = new HashSet<>(users1);

        if (secondName != null && !secondName.isEmpty()) {
            List<UserEntity> users2 = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndAccountConfirmation(secondName, secondName, "1");
            mergedFoundUsers.addAll(users2);
        }
        return new ArrayList<>(mergedFoundUsers);
    }

    public List<UserDTO> searchUsersToDTO(SearchRequest searchRequest){
        return userMapper.usersListTODtos(this.searchUsers(searchRequest));
    }

    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * first finding user that made the search, then finding all users for the given input string/strings
     * then going through all found users to get the relationship between found user and user that made the request
     */
    public List<UserFriendRequestResponse> searchUsersByUser(SearchRequest searchRequest, Long id) {
        UserEntity currentUser = userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found"));

        List<UserEntity> foundUsers = this.searchUsers(searchRequest);

        StatusRequestIdModel statusAndId;
        String status;
        Long requestId;

        List<UserFriendRequestResponse> foundUsersWithStatus = new ArrayList<>();

        for (UserEntity foundUser: foundUsers){
            if (currentUser.getId() < foundUser.getId()){
                statusAndId = getStatusAndRequestId(currentUser, foundUser, "sent", "received");
                status = statusAndId.getStatus();
                requestId = statusAndId.getRequestId();
            }
            else if (currentUser.getId() > foundUser.getId()){
                statusAndId = getStatusAndRequestId(foundUser, currentUser, "received", "sent");
                status = statusAndId.getStatus();
                requestId = statusAndId.getRequestId();
            }
            else{
                status = "sameUser";
                requestId = -1L;
            }
            foundUsersWithStatus.add(new UserFriendRequestResponse(userMapper.userToDto(foundUser), status, requestId));
        }

        return foundUsersWithStatus;
    }

    public List<UserFriendRequestResponse> getAllForUser(Long id) {
        UserEntity currentUser = userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found"));

        List<UserEntity> foundUsers = this.userRepository.findAll();

        StatusRequestIdModel statusAndId;
        String status;
        Long requestId;

        List<UserFriendRequestResponse> foundUsersWithStatus = new ArrayList<>();

        for (UserEntity foundUser: foundUsers){
            if (currentUser.getId() < foundUser.getId()){
                statusAndId = getStatusAndRequestId(currentUser, foundUser, "sent", "received");
                status = statusAndId.getStatus();
                requestId = statusAndId.getRequestId();
            }
            else if (currentUser.getId() > foundUser.getId()){
                statusAndId = getStatusAndRequestId(foundUser, currentUser, "received", "sent");
                status = statusAndId.getStatus();
                requestId = statusAndId.getRequestId();
            }
            else{
                status = "sameUser";
                requestId = -1L;
            }
            foundUsersWithStatus.add(new UserFriendRequestResponse(userMapper.userToDto(foundUser), status, requestId));
        }

        return foundUsersWithStatus;
    }

    private StatusRequestIdModel getStatusAndRequestId(UserEntity firstUser, UserEntity secondUser, String firstStatus, String secondStatus) {

        //if nothing is found there was no request made
        String status = "nothing";
        Long requestId = -1L;

        Optional<FriendRequestEntity> relationship = this.friendRepository
                .findByFirstUserAndSecondUser(firstUser, secondUser);

        if (relationship.isPresent()){
            FriendRequestEntity relationshipEntity = relationship.get();
            requestId = relationshipEntity.getId();

            if (relationshipEntity.getStatus().equals("pending_one_two")){

                status = firstStatus;
            }
            else if (relationshipEntity.getStatus().equals("pending_two_one")) {

                status = secondStatus;
            }
            else{
                status = "accepted";
            }
        }
        return new StatusRequestIdModel(status, requestId);
    }

    public List<UserFriendRequestResponse> getAllFriendsForUser(Long id){
        UserEntity currentUser = userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found"));

        List<FriendRequestEntity> requests = this.friendRepository.findByFirstUserOrSecondUserAndStatus(currentUser, "accepted");

        List<UserFriendRequestResponse> result = new ArrayList<>();

        UserEntity userToAdd;
        for (FriendRequestEntity request: requests){
            if (Objects.equals(request.getFirstUser().getId(), currentUser.getId())){
                userToAdd = request.getSecondUser();
            }
            else{
                userToAdd = request.getFirstUser();
            }
            result.add(new UserFriendRequestResponse(userMapper.userToDto(userToAdd), "accepted",
                    request.getId()));
        }
        return result;
    }

    public List<UserFriendRequestResponse> getAllIncomingRequestsForUser(Long id){
        UserEntity currentUser = userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found"));

        List<FriendRequestEntity> requests = this.friendRepository
                .findByFirstUserOrSecondUserAndStatusContaining(currentUser, "pending");

        List<UserFriendRequestResponse> result = new ArrayList<>();

        for (FriendRequestEntity request: requests){
            if (request.getFirstUser().getId().equals(currentUser.getId()) && request.getStatus().equals("pending_two_one")){
                result.add(new UserFriendRequestResponse(userMapper.userToDto(request.getSecondUser()), "received",
                        request.getId()));
            }
            else if (request.getSecondUser().getId().equals(currentUser.getId()) && request.getStatus().equals("pending_one_two")){
                result.add(new UserFriendRequestResponse(userMapper.userToDto(request.getFirstUser()), "received",
                        request.getId()));
            }

        }
        return result;
    }

    public List<UserFriendRequestResponse> getAllSentRequestsForUser(Long id){
        UserEntity currentUser = userRepository.findById(id).orElseThrow(
                ()-> new UserNotFoundException("user with this Id not found"));

        List<FriendRequestEntity> requests = this.friendRepository
                .findByFirstUserOrSecondUserAndStatusContaining(currentUser, "pending");

        List<UserFriendRequestResponse> result = new ArrayList<>();

        for (FriendRequestEntity request: requests){
            if (request.getFirstUser().getId().equals(currentUser.getId()) && request.getStatus().equals("pending_one_two")){
                result.add(new UserFriendRequestResponse(userMapper.userToDto(request.getSecondUser()), "sent",
                        request.getId()));
            }
            else if (request.getSecondUser().getId().equals(currentUser.getId()) && request.getStatus().equals("pending_two_one")){
                result.add(new UserFriendRequestResponse(userMapper.userToDto(request.getFirstUser()), "sent",
                        request.getId()));
            }

        }
        return result;
    }


    public List<InvitedUserModel> getUsersInvitedToEvent(Long eventId){
        List <InvitationEntity> invitations = this.invitationRepository.findByEvent_Id(eventId);


        return invitations.stream()
                .map(invitation->{
                    UserEntity user = invitation.getUser();
                    String statusRegardingEvent;
                    if (invitation.getStatus().equals("deleted_accepted")){
                        statusRegardingEvent = "accepted";
                    }
                    else{
                        statusRegardingEvent = invitation.getStatus();
                    }
                    return new InvitedUserModel(user.getId(), user.getFirstName(), user.getLastName(), statusRegardingEvent);
                })
                .collect(Collectors.toList());
    }

    public List<UserFriendRequestResponse> getStillPossibleToInviteFriends(Long userId, Long eventId){

        List<UserFriendRequestResponse> friendsList= this.getAllFriendsForUser(userId);
        List<InvitedUserModel> invitedFriendsList = this.getUsersInvitedToEvent(eventId);

        List<Long> invitedFriendsIds = invitedFriendsList.stream()
                .map(InvitedUserModel::getId)
                .collect(Collectors.toList());

        return friendsList.stream()
                .filter(friend -> !invitedFriendsIds.contains(friend.getUserEntity().getId()))
                .collect(Collectors.toList());
    }

}
