package services;

import dataaccess.AuthTokenDao;
import dataaccess.MYSQLUserDao;
import dataaccess.UserDao;
import models.UserModel;
import models.AuthTokenModel;
import org.mindrot.jbcrypt.BCrypt;

import java.security.spec.ECField;
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
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        }
        UserModel usermodel;
        try{
            usermodel = userDao.getUser(user.getUsername());
        }catch (Exception e){
            throw new Exception("Error: registerUser, Problem: " + e.getMessage());
        }
        if (usermodel == null) {
            userDao.addUser(user);
            AuthTokenModel authTokenModel = new AuthTokenModel(user.getUsername(), UUID.randomUUID().toString());
            authTokenDao.addAuthToken(authTokenModel);
            return authTokenModel;
        } else {
            throw new UserAlreadyExistsException("Error: User with that Username already exists");
        }
    }

    public AuthTokenModel loginUser(UserModel user) throws Exception{
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        }
        UserModel userModel = null;
        try{
            userModel = userDao.getUser(user.getUsername());
        }catch (Exception e){
            throw new Exception("Error: loginUser, Problem: " + e.getMessage());
        }
        if ((userModel == null) || !BCrypt.checkpw(user.getPassword(), userModel.getPassword())) {
            throw new InvalidCredentialsException("Error: Username or Password is incorrect ");
        } else {
            AuthTokenModel authData = new AuthTokenModel(user.getUsername(), UUID.randomUUID().toString());
            authTokenDao.addAuthToken(authData);
            return authData;
        }
    }

    public void logoutUser(String authToken) throws Exception{
        if(!isValidUser(authToken)){
            throw new InvalidCredentialsException("Error: Unauthorized");
        }else{
            authTokenDao.deleteAuthToken(authToken);
        }
    }

    public boolean isValidUser(String authToken) throws Exception{
        return authTokenDao.authTokenExists(authToken);
    }

    public AuthTokenModel getAuthTokenModel(String authToken) throws Exception{
        return authTokenDao.getAuthTokenModel(authToken);
    }
}
