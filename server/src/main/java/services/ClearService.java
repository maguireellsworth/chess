package services;

import dataaccess.AuthTokenDao;
import dataaccess.GameDao;
import dataaccess.UserDao;

public class ClearService {
    private UserDao userDao;
    private AuthTokenDao authTokenDao;
    private GameDao gameDao;

    public ClearService(UserDao userDao, AuthTokenDao authTokenDao, GameDao gameDao){
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
        this.gameDao = gameDao;
    }

    public void clearDB(){
        userDao.clear();
        authTokenDao.clear();
        gameDao.clear();
    }
}
