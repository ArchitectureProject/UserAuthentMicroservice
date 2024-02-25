package com.efrei.usermicroservice.service;

import com.efrei.usermicroservice.model.AppUser;
import com.efrei.usermicroservice.model.dto.LoginAttempt;
import com.efrei.usermicroservice.model.dto.LoginResponse;
import com.efrei.usermicroservice.model.dto.UserToCreate;

import java.util.List;

public interface UserService {
    AppUser createUser(UserToCreate userToCreate);
    AppUser getUserById(String bearerToken, String userId);
    List<AppUser> getAllUser(String bearerToken);
    LoginResponse login(LoginAttempt loginAttempt);
    AppUser modifyUser(String bearerToken, String userId, UserToCreate userToCreate);
    void deleteUser(String bearerToken, String userId);
}
