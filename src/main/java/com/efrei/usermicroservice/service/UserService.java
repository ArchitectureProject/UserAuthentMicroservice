package com.efrei.usermicroservice.service;

import com.efrei.usermicroservice.model.AppUser;
import com.efrei.usermicroservice.model.dto.UserToCreate;

public interface UserService {
    AppUser createUser(UserToCreate userToCreate);
}
