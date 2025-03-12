package service;

import dataaccess.*;
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
    AuthTokenDao authTokenDao = new MemoryAuthTokenDao();
    GameDao gameDao = new MemoryGameDao();
    UserService userService = new UserService(userDao, authTokenDao);
    ClearService clearService = new ClearService(userDao, authTokenDao, gameDao);
    GameService gameService = new GameService(gameDao, userService);
    UserModel user = new UserModel("username", "password", "email@email.com");

    @Test
    @DisplayName("Register User")
    public void registerUser()throws Exception{
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
    public void loginUser()throws Exception{
        userService.registerUser(user);
        AuthTokenModel authTokenModel = userService.loginUser(user);
        Assertions.assertTrue(userService.isValidUser(authTokenModel.getAuthToken()));
    }

    @Test
    @DisplayName("Login Bad Password")
    public void loginBadPassword()throws Exception{
        UserModel badUser = new UserModel("username", "notpassword", "email@email.com");
        userService.registerUser(user);
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.loginUser(badUser);});
    }

    @Test
    @DisplayName("Logout User")
    public void logoutUser()throws Exception{
        userService.registerUser(user);
        AuthTokenModel authTokenModel = userService.loginUser(user);
        userService.logoutUser(authTokenModel.getAuthToken());
        Assertions.assertFalse(userService.isValidUser(authTokenModel.getAuthToken()));
    }

    @Test
    @DisplayName("Logout Bad AuthToken")
    public void logoutBadAuth()throws Exception{
        AuthTokenModel authToken = userService.registerUser(user);
        String uuid = UUID.randomUUID().toString();
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.logoutUser(uuid);});
    }

    @Test
    @DisplayName("Join Game Color Already Taken")
    public void joinGameTaken()throws Exception{
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
