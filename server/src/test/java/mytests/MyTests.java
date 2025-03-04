package mytests;

import dataaccess.AuthTokenDao;
import dataaccess.GameDao;
import dataaccess.UserDao;
import models.AuthTokenModel;
import models.UserModel;
import org.junit.jupiter.api.*;
import server.Server;
import services.ClearService;
import services.GameService;
import services.InvalidUserDataException;
import services.UserService;

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
        Assertions.assertEquals(newUser.getUsername(), authModel.getUsername());
    }

    @Test
    @DisplayName("Register Bad User")
    public void registerBasUser(){
        UserModel badUser = new UserModel(null, "password", "email@email.com");
        Exception e = Assertions.assertThrows(InvalidUserDataException.class, () -> {userService.registerUser(badUser);});
    }
}
