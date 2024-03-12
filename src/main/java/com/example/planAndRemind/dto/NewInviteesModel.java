package com.example.planAndRemind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NewInviteesModel {

    public NewInviteesModel() {
    }

    private List<Long> inviteeIDs;
}
