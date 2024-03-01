package com.efrei.usermicroservice.controller;

import com.efrei.usermicroservice.model.AppUser;
import com.efrei.usermicroservice.model.dto.LoginAttempt;
import com.efrei.usermicroservice.model.dto.LoginResponse;
import com.efrei.usermicroservice.model.dto.UserToCreate;
import com.efrei.usermicroservice.service.UserServiceImpl;
import com.efrei.usermicroservice.utils.JWTUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//simple comment
@RestController
public class UserController {
    UserServiceImpl userService;

    JWTUtils jwtUtils;

    public UserController(UserServiceImpl userService, JWTUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/user")
    public AppUser createUser(@RequestBody UserToCreate userToCreate){
        return userService.createUser(userToCreate);
    }

    @PostMapping("/login")
    public LoginResponse getJwt(@RequestBody LoginAttempt loginAttempt) {
        return userService.login(loginAttempt);
    }

    @GetMapping("/public_key")
    public String getJwk() {
        return jwtUtils.createJwks();
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

    @PutMapping("/user/{userId}")
    public AppUser modifyUser(@RequestHeader(name = "Authorization") String bearerToken,
                              @PathVariable String userId,
                              @RequestBody UserToCreate userToCreate) {
        return userService.modifyUser(bearerToken, userId, userToCreate);
    }

    @DeleteMapping("/user/{userId}")
    public void deleteUser(@RequestHeader(name = "Authorization") String bearerToken,
                           @PathVariable String userId) {
        userService.deleteUser(bearerToken, userId);
    }


}
