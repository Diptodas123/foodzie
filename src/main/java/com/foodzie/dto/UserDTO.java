package com.foodzie.dto;

import com.foodzie.model.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String address;
    private String city;
    private String state;
    private Role role;
}

