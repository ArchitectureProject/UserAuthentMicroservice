package com.efrei.usermicroservice.controller;

import com.efrei.usermicroservice.model.AppUser;
import com.efrei.usermicroservice.model.dto.LoginAttempt;
import com.efrei.usermicroservice.model.dto.LoginResponse;
import com.efrei.usermicroservice.model.dto.UserToCreate;
import com.efrei.usermicroservice.service.UserServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public AppUser createUser(@RequestBody UserToCreate userToCreate){
        return userService.createUser(userToCreate);
    }

    @PostMapping("/login")
    public LoginResponse getJwt(@RequestBody LoginAttempt loginAttempt) {
        return userService.login(loginAttempt);
    }

    @GetMapping("/user/{userId}")
    public AppUser getUser(@RequestHeader(name = "Authorization") String bearerToken,
                           @PathVariable String userId) {
        return userService.getUserById(bearerToken, userId);
    }

    @GetMapping("/user")
    public List<AppUser> getAllUsers(@RequestHeader(name = "Authorization") String bearerToken) {
        return userService.getAllUser(bearerToken);
    }

}
