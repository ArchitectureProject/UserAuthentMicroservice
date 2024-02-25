package com.efrei.usermicroservice.service;

import com.efrei.usermicroservice.exceptions.custom.IncorrectPasswordException;
import com.efrei.usermicroservice.exceptions.custom.UserMicroserviceException;
import com.efrei.usermicroservice.exceptions.custom.UserNotFoundException;
import com.efrei.usermicroservice.model.AppUser;
import com.efrei.usermicroservice.model.dto.LoginResponse;
import com.efrei.usermicroservice.model.dto.UserToCreate;
import com.efrei.usermicroservice.model.dto.LoginAttempt;
import com.efrei.usermicroservice.repository.UserRepository;
import com.efrei.usermicroservice.utils.JWTUtils;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    UserRepository userRepository;

    JWTUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, JWTUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public AppUser createUser(UserToCreate userToCreate){
        AppUser appUser = mapUserToCreateIntoUser(userToCreate);
        return userRepository.save(appUser);
    }

    public AppUser getUserById(String bearerToken, String userId){
        jwtUtils.validateJwt(bearerToken.substring(7));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public List<AppUser> getAllUser(String bearerToken){
        jwtUtils.validateJwt(bearerToken.substring(7));
        return userRepository.findAll();
    }


    public LoginResponse login(LoginAttempt loginAttempt){
        AppUser appUser =  userRepository.findByEmail(loginAttempt.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + loginAttempt.email()));

        if(!isAttemptedPasswordCorrect(loginAttempt.password(), appUser.getPasswordHash())){
            throw new IncorrectPasswordException("Incorrect password for the user " + loginAttempt.email());
        }

        return new LoginResponse(jwtUtils.createJWT(appUser));
    }

    private boolean isAttemptedPasswordCorrect(String attemptedPassword, String hashedDbPassword){
        String hashedAttemptedPassword = hashStringUsingSHA256(attemptedPassword);
        return hashedAttemptedPassword.equals(hashedDbPassword);
    }

    private AppUser mapUserToCreateIntoUser(UserToCreate userToCreate){
        AppUser appUser = new AppUser();
        appUser.setUserRole(userToCreate.role());
        appUser.setEmail(userToCreate.email());
        appUser.setPasswordHash(hashStringUsingSHA256(userToCreate.password()));
        return appUser;
    }

    private String hashStringUsingSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UserMicroserviceException("Erreur lors du hash d'une chaine de caractère", e);
        }
    }

}
