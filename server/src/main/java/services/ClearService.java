package services;

import dataaccess.AuthTokenDao;
import dataaccess.UserDao;

public class ClearService {
    private UserDao userDao;
    private AuthTokenDao authTokenDao;

    public ClearService(UserDao userDao, AuthTokenDao authTokenDao){
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
    }

    public void clearDB(){
        userDao.clear();
        authTokenDao.clear();
    }
}
