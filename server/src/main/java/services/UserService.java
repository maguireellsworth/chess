package services;

import dataaccess.AuthTokenDao;
import dataaccess.UserDao;
import models.UserModel;
import models.AuthTokenModel;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private UserDao userDao;
    private AuthTokenDao authTokenDao;

    public UserService(UserDao userDao, AuthTokenDao authTokenDao){
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    public AuthTokenModel registerUser(UserModel user) throws Exception{
        if(user.getUsername() == null || user.getPassword() == null || user.getEmail() == null){
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        } else if(userDao.getUser(user.getUsername()) == null){
            userDao.addUser(user);
            AuthTokenModel authTokenModel = new AuthTokenModel(user.getUsername(), UUID.randomUUID());
            authTokenDao.addAuthToken(authTokenModel);
            return authTokenModel;
        }else{
            throw new UserAlreadyExistsException("Error: User with that Username already exists");
        }
    }

    public AuthTokenModel loginUser(UserModel user) throws Exception{
        if(user.getUsername() == null || user.getPassword() == null){
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        }
        UserModel userData = userDao.getUser(user.getUsername());
        if((userData == null) || (!Objects.equals(userData.getPassword(), user.getPassword()))){
            throw new InvalidCredentialsException("Error: Username or Password is incorrect");
        }else{
            AuthTokenModel authData = new AuthTokenModel(user.getUsername(), UUID.randomUUID());
            authTokenDao.addAuthToken(authData);
            return authData;
        }
    }

    public void logoutUser(UUID authToken){
        if(!validateUser(authToken)){
            throw new InvalidCredentialsException("Error: Unauthorized");
        }else{
            authTokenDao.deleteAuthToken(authToken);
        }
    }

    public boolean validateUser(UUID authToken){
        return authTokenDao.authTokenexists(authToken);
    }
}
