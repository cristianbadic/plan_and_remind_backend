package com.example.planAndRemind.controller;

import com.example.planAndRemind.dto.FriendRequestModel;
import com.example.planAndRemind.model.FriendRequestEntity;
import com.example.planAndRemind.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/friend")
public class FriendRequestController {

    private final FriendRequestService friendService;

    @Autowired
    public FriendRequestController(FriendRequestService friendService) {
        this.friendService = friendService;
    }


    @GetMapping("/get-all")
    public ResponseEntity<List<FriendRequestEntity>> getAllRequests() {
        return ResponseEntity.ok(friendService.getAllFriendRequests());
    }

    @PostMapping("/create-request")
    public ResponseEntity<?> addRequest(@RequestBody FriendRequestModel friendRequestModel){
        this.friendService.createFriendRequest(friendRequestModel);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update-request/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Long id){
        this.friendService.updateFriendRequest(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/delete-request/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long id){
        friendService.deleteFriendRequest(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
