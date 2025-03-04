package mytests;

import dataaccess.AuthTokenDao;
import dataaccess.GameDao;
import dataaccess.UserDao;
import models.AuthTokenModel;
import models.UserModel;
import org.junit.jupiter.api.*;
import server.Server;
import services.*;

public class MyTests {
    UserDao userDao = new UserDao();
    AuthTokenDao authTokenDao = new AuthTokenDao();
    GameDao gameDao = new GameDao();
    UserService userService = new UserService(userDao, authTokenDao);
    ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    GameService gameService = new GameService(gameDao, userService);


    @Test
    @DisplayName("Register User")
    public void registerUser(){
        UserModel newUser = new UserModel("username", "password", "email@email.com");
        AuthTokenModel authModel = userService.registerUser(newUser);
        Assertions.assertTrue(userService.isValidUser(authModel.getAuthToken()));
    }

    @Test
    @DisplayName("Register Bad User")
    public void registerBasUser(){
        UserModel badUser = new UserModel(null, "password", "email@email.com");
        Assertions.assertThrows(InvalidUserDataException.class, () -> {userService.registerUser(badUser);});
    }

    @Test
    @DisplayName("Login user")
    public void loginUser(){
        UserModel user = new UserModel("username", "password", "email@email.com");
        userService.registerUser(user);
        AuthTokenModel authTokenModel = userService.loginUser(user);
        Assertions.assertTrue(userService.isValidUser(authTokenModel.getAuthToken()));
    }

    @Test
    @DisplayName("Login Bad Password")
    public void loginBadPassword(){
        UserModel user = new UserModel("username", "password", "email@email.com");
        UserModel badUser = new UserModel("username", "notpassword", "email@email.com");
        userService.registerUser(user);
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.loginUser(badUser);});
    }
}
