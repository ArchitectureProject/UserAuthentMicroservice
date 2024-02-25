package com.efrei.usermicroservice.model.dto;

import com.efrei.usermicroservice.model.UserRole;

public record UserToCreate(UserRole role, String email, String password){}
