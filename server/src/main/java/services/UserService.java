package services;

import com.sun.jdi.InvalidTypeException;
import dataaccess.AuthTokenDao;
import dataaccess.UserDao;
import models.UserModel;
import models.AuthTokenModel;
import resultClasses.RegisterResult;

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
        }
        else if(userDao.getUser(user.getUsername()) == null){
            userDao.addUser(user);
            AuthTokenModel authTokenModel = new AuthTokenModel(user.getUsername(), UUID.randomUUID());
            authTokenDao.addAuthToken(authTokenModel);
            return authTokenModel;
        }else{
            throw new UserAlreadyExistsException("Error: User with that Username already exists");
        }
    }
}
