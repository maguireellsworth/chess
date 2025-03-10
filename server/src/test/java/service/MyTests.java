package service;

import dataaccess.AuthTokenDao;
import dataaccess.GameDao;
import dataaccess.MemoryUserDao;
import dataaccess.UserDao;
import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.JoinRequest;
import models.AuthTokenModel;
import models.UserModel;
import org.junit.jupiter.api.*;
import services.*;

import java.util.UUID;

public class MyTests {
    UserDao userDao = new MemoryUserDao();
    AuthTokenDao authTokenDao = new AuthTokenDao();
    GameDao gameDao = new GameDao();
    UserService userService = new UserService(userDao, authTokenDao);
    ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    GameService gameService = new GameService(gameDao, userService);
    UserModel user = new UserModel("username", "password", "email@email.com");

    @Test
    @DisplayName("Register User")
    public void registerUser(){
        AuthTokenModel authModel = userService.registerUser(user);
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
        userService.registerUser(user);
        AuthTokenModel authTokenModel = userService.loginUser(user);
        Assertions.assertTrue(userService.isValidUser(authTokenModel.getAuthToken()));
    }

    @Test
    @DisplayName("Login Bad Password")
    public void loginBadPassword(){
        UserModel badUser = new UserModel("username", "notpassword", "email@email.com");
        userService.registerUser(user);
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.loginUser(badUser);});
    }

    @Test
    @DisplayName("Logout User")
    public void logoutUser(){
        userService.registerUser(user);
        AuthTokenModel authTokenModel = userService.loginUser(user);
        userService.logoutUser(authTokenModel.getAuthToken());
        Assertions.assertFalse(userService.isValidUser(authTokenModel.getAuthToken()));
    }

    @Test
    @DisplayName("Logout Bad AuthToken")
    public void logoutBadAuth(){
        AuthTokenModel authToken = userService.registerUser(user);
        UUID uuid = UUID.randomUUID();
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.logoutUser(uuid);});
    }

    @Test
    @DisplayName("Join Game Color Already Taken")
    public void joinGameTaken(){
        AuthTokenModel authTokenP1 = userService.registerUser(user);
        CreateRequest createRequest = new CreateRequest(authTokenP1.getAuthToken());
        createRequest.setGameName("Coding is fun");
        CreateResult result = gameService.createGame(createRequest);
        JoinRequest joinRequestP1 = new JoinRequest("WHITE", result.getGameID());
        joinRequestP1.setAuthTokenModel(authTokenP1);
        gameService.joinGame(joinRequestP1);

        AuthTokenModel authTokenP2 = userService.registerUser(new UserModel("x", "y", "z"));
        JoinRequest joinRequestP2 = new JoinRequest("WHITE", result.getGameID());
        joinRequestP2.setAuthTokenModel(authTokenP2);
        Assertions.assertThrows(UserAlreadyExistsException.class, ()->{gameService.joinGame(joinRequestP2);});
    }
}
