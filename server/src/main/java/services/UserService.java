package services;

import dataaccess.UserDao;
import models.UserModel;
import resultClasses.RegisterResult;

public class UserService {
    private UserDao userDao;

    public UserService(UserDao userDao){
        this.userDao = userDao;
    }

    public RegisterResult registerUser(UserModel user){
        if(userDao.getUser(user.getUsername())){

        }
    }
}
