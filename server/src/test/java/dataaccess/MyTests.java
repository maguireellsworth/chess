package dataaccess;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.JoinRequest;
import models.AuthTokenModel;
import models.UserModel;
import org.junit.jupiter.api.*;
import services.*;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.UUID;

public class MyTests {

    private static UserDao userDao;
    private static AuthTokenDao authTokenDao;
    private static GameDao gameDao;
    private static UserService userService;
    private static ClearService clearService;
    private static GameService gameService;
    private static UserModel user;
    private static AuthTokenModel existingAuth;

    @BeforeAll
    public static void init()throws Exception{
        userDao = new MYSQLUserDao();
        authTokenDao = new MYSQLAuthTokenDao();
        gameDao = new MYSQLGameDao();
        userService = new UserService(userDao, authTokenDao);
        clearService = new ClearService(userDao, authTokenDao, gameDao);
        gameService = new GameService(gameDao, userService);
        user = new UserModel("username", "password", "email@email.com");
    }

    @BeforeEach
    public void setUp()throws Exception{
        userDao.clear();
        authTokenDao.clear();
        gameDao.clear();

        existingAuth = userService.registerUser(user);
    }

    @Test
    @DisplayName("Register User")
    public void registerUser()throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM users WHERE username = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, user.getUsername());
                try(var result = preparedStatement.executeQuery()){
                    Assertions.assertTrue(result.next());
                }
            }
        }
    }

    @Test
    @DisplayName("Register Bad User")
    public void badRegister(){
        UserModel badUser = new UserModel(null, "notpassword", "email@email.com");
        Assertions.assertThrows(InvalidUserDataException.class, ()-> {userService.registerUser(badUser);});
    }

    @Test
    @DisplayName("Success Login User")
    public void loginUser()throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM authtokens WHERE username = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, existingAuth.getUsername());
                try(var result = preparedStatement.executeQuery()){
                    Assertions.assertTrue(result.next());
                }
            }
        }
    }

    @Test
    @DisplayName("Bad Login")
    public void loginBadPassword()throws Exception{
        UserModel badUser = new UserModel("username", "notpassword", "email@email.com");
        Assertions.assertThrows(InvalidCredentialsException.class, ()->{userService.loginUser(badUser);});
    }

    @Test
    @DisplayName("Logout User")
    public void logoutUser() throws Exception{
        userService.logoutUser(existingAuth.getAuthToken());
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM authtokens WHERE authtoken = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, existingAuth.getAuthToken());
                try(var result = preparedStatement.executeQuery()){
                    Assertions.assertFalse(result.next());
                }
            }
        }
    }

    @Test
    @DisplayName("Logout Bad Authentication")
    public void logoutBadAuth() throws Exception{
        userService.loginUser(user);
        String diffAuthToken = UUID.randomUUID().toString();
        Assertions.assertThrows(InvalidCredentialsException.class, () -> {userService.logoutUser(diffAuthToken);});
    }

    @Test
    @DisplayName("Create Game")
    public void successCreateGame() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("TestGame");
        CreateResult createResult = gameService.createGame(request);
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM games WHERE game_id = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setInt(1, createResult.getGameID());
                try(var result = preparedStatement.executeQuery()){
                    Assertions.assertTrue(result.next());
                }
            }
        }
    }

    @Test
    @DisplayName("Create Game with bad Auth")
    public void createBadAuth(){
        CreateRequest request = new CreateRequest(UUID.randomUUID().toString());
        request.setGameName("badAuthGame");
        Assertions.assertThrows(InvalidCredentialsException.class, () ->{gameService.createGame(request);});
    }

    @Test
    @DisplayName("Join Game")
    public void joinGame() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("SuccessJoin");
        CreateResult createResult = gameService.createGame(request);
        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.getGameID());
        joinRequest.setAuthTokenModel(existingAuth);
        gameService.joinGame(joinRequest);
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM games WHERE  white_username = ?";
            try(var preparedstatement = conn.prepareStatement(statement)){
                preparedstatement.setString(1, existingAuth.getUsername());
                try(var result = preparedstatement.executeQuery()){
                    Assertions.assertTrue(result.next());
                }
            }
        }
    }

    @Test
    @DisplayName("Join Game with bad Auth")
    public void badAuthJoin() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("badAuthJoin");
        CreateResult createResult = gameService.createGame(request);
        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.getGameID());
        joinRequest.setAuthTokenModel(new AuthTokenModel(UUID.randomUUID().toString(), "badAuthusername"));
        Assertions.assertThrows(InvalidCredentialsException.class, () -> {gameService.joinGame(joinRequest);});
    }

    @Test
    @DisplayName("Join with taken color")
    public void badColorJoin() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("Color taken");
        CreateResult createResult = gameService.createGame(request);
        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.getGameID());
        joinRequest.setAuthTokenModel(existingAuth);
        gameService.joinGame(joinRequest);
        AuthTokenModel userTwoAuth = userService.registerUser(new UserModel("diffUsername", "diffpassword", "diffemail@email.com"));
        joinRequest.setAuthTokenModel(userTwoAuth);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> {gameService.joinGame(joinRequest);});
    }

    @Test
    @DisplayName("Good list")
    public void listGames() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("number 1");
        gameService.createGame(request);
        request.setGameName("number 2");
        gameService.createGame(request);
        request.setGameName("number 3");
        gameService.createGame(request);
        List allgames = gameService.listGames(existingAuth.getAuthToken());
        Assertions.assertEquals(3, allgames.size());
    }

    @Test
    @DisplayName("Clear database")
    public void clear() throws Exception{
        CreateRequest request = new CreateRequest(existingAuth.getAuthToken());
        request.setGameName("number 1");
        gameService.createGame(request);

        clearService.clearDB();

        try(var conn = DatabaseManager.getConnection()){
            var userStatement = "SELECT * FROM users";
            var authStatement = "SELECT * FROM authtokens";
            var gameStatement = "SELECT * FROM games";
            try(var ups = conn.prepareStatement(userStatement)){
                try(var urs = ups.executeQuery()){
                    Assertions.assertFalse(urs.next());
                }
            }
            try(var aps = conn.prepareStatement(authStatement)){
                try(var ars = aps.executeQuery()){
                    Assertions.assertFalse(ars.next());
                }
            }
            try(var gps = conn.prepareStatement(gameStatement)){
                try(var grs = gps.executeQuery()){
                    Assertions.assertFalse(grs.next());
                }
            }
        }
    }

}
