package client;

import exception.ResponseException;
import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.JoinRequest;
import intermediaryclasses.RegisterResult;
import models.AuthTokenModel;
import models.GameModel;
import models.UserModel;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import ui.Repl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.spec.ECField;
import java.util.List;
import java.util.Objects;

import static chess.ChessGame.TeamColor.WHITE;


public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl = "http://localhost:8080";
    private static ServerFacade serverFacade;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private InputStream originalIn;
    private static UserModel user;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(serverUrl);
        user = new UserModel("TestUser", "TestPassword", "TestEmail");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        URL url = (new URI(serverUrl + "/db")).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to clear database: " + responseCode);
        }
    }

    @BeforeEach
    @Tag("Repl")
    public void setUpRepl(){
        originalOut = System.out;
        originalIn = System.in;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    @Tag("Repl")
    public void restoreSystemInOut(){
        System.setOut(originalOut);
        System.setIn(originalIn);
    }


    @Test
    @DisplayName("Register ServerFacade Connects")
    public void registerFacade() throws Exception{
        RegisterResult result = serverFacade.registerUser(user);
        Assertions.assertEquals(result.getUsername(), user.getUsername());
        Assertions.assertNotNull(result.getAuthToken());
    }

    @Test
    @DisplayName("Register Facade No Password Throws Error")
    public void registerError() {
        UserModel user = new UserModel("user", null, "email");
        Assertions.assertThrows(ResponseException.class, () -> { serverFacade.registerUser(user);});
    }

    @Test
    @DisplayName("Help Command Displays Prelogin Options")
    @Tag("Repl")
    public void replHelpPrelogin() {
        String input = "help\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput =
                """
                Options:
                - help
                - quit
                - login <username> <password>
                - register <username> <password> <email>
                """;

//        Assertions.assertEquals(expectedOutput, consoleOutput);
        Assertions.assertTrue(consoleOutput.contains((expectedOutput)));
    }

    @Test
    @DisplayName("Help Command Displays Postlogin Options")
    @Tag("Repl")
    public void replHelpPostlogin() {
        String input = "register user pass email\nhelp\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput =
                """
                 Options:
                 - help
                 - logout
                 - create <gameName>
                 - list
                 - join <id> <WHITE or BLACK>
                 - observe <id>
                 """;;

//        Assertions.assertEquals(expectedOutput, consoleOutput);
        Assertions.assertTrue(consoleOutput.contains((expectedOutput)));
    }

    @Test
    @DisplayName("Repl Register Command Registers")
    @Tag("Repl")
    public void replRegister(){
        String input = "register replUser replPass replEmail\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Successfully Registered";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Register Wrong Input Count")
    @Tag("Repl")
    public void replRegisterNoPassword(){
        String input = "register user email\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Incorrect number of parameters. 'register' command requires parameters: <username> <password> <email>";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Login Wrong Input Count")
    @Tag("Repl")
    public void replLoginNoPassword(){
        String input = "login username\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Incorrect number of parameters. 'login' command requires parameters: <username> <password>";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

//    @Test
//    @DisplayName("ServerFacade Logout")
//    public void logoutFacade() throws Exception{
//        //register
//        RegisterResult result = serverFacade.registerUser(user);
//        //logout
//        serverFacade.logoutUser(result.getAuthToken());
//    }

    @Test
    @DisplayName("ServerFacade Login")
    public void loginFacade() throws Exception{
        //register
        RegisterResult registerResult = serverFacade.registerUser(user);
        //logout
        serverFacade.logoutUser(registerResult.getAuthToken());
        //login
        RegisterResult loginResult = serverFacade.loginUser(user);

        Assertions.assertEquals(user.getUsername(), loginResult.getUsername());
        Assertions.assertNotNull(loginResult.getAuthToken());
        Assertions.assertNotEquals(registerResult.getAuthToken(), loginResult.getAuthToken());
    }


    @Test
    @DisplayName("Repl Login Already Logged In")
    @Tag("Repl")
    public void replLoginTwice(){
        String input = "register user pass email\nlogin user pass\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Already logged in. Valid commands:";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Login Success")
    @Tag("Repl")
    public void replLogin(){
        String input = "register user pass email\nlogout\nlogin user pass\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Successfully Logged In!";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Create Facade Creates Game")
    public void createFacade()throws Exception{
        RegisterResult registerResult = serverFacade.registerUser(user);
        CreateRequest createRequest = new CreateRequest(registerResult.getAuthToken(), "TestGame");
        CreateResult createResult = serverFacade.createGame(createRequest);
        Assertions.assertEquals(1, createResult.getGameID());
    }

    @Test
    @DisplayName("Repl Create Game Not Logged In")
    @Tag("Repl")
    public void createNotLoggedIn(){
        String input = "create gamename\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Must be logged in to run command 'create'";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Create Game Wrong Parameters")
    @Tag("Repl")
    public void createWrongParameters(){
        String input = "register user pass email\ncreate\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Incorrect number of parameters. 'create' command requires parameters: <gamename>";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Create Game Successfully")
    @Tag("Repl")
    public void createSuccessfully(){
        String input = "register user pass email\ncreate gamename\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Successfully Created Game!";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Facade List Games")
    public void listGames() throws Exception{
        RegisterResult registerResult = serverFacade.registerUser(user);
        CreateResult createResult1 = serverFacade.createGame(new CreateRequest(registerResult.getAuthToken(), "testgame1"));
        CreateResult createResult2 = serverFacade.createGame(new CreateRequest(registerResult.getAuthToken(), "testgame2"));
        CreateResult createResult3 = serverFacade.createGame(new CreateRequest(registerResult.getAuthToken(), "testgame3"));
        List<GameModel> games = serverFacade.listGames(registerResult.getAuthToken()).getGames();
        Assertions.assertEquals(3, games.size());
    }

    @Test
    @DisplayName("Repl List Games Not Logged In")
    @Tag("Repl")
    public void listNotLoggedIn(){
        String input = "list\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Must be logged in to run command 'list'";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl List One Game")
    @Tag("Repl")
    public void replListOneGame(){
        String input = "register user pass email\ncreate testgame\nlist\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "1) GameName: testgame, WhiteUsername: null, BlackUsername: null";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl List Multiple Games")
    @Tag("Repl")
    public void replListMultipleGames(){
        String input = "register user pass email\ncreate game1\ncreate game2\ncreate game3\nlist\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = """
                1) GameName: game1, WhiteUsername: null, BlackUsername: null
                2) GameName: game2, WhiteUsername: null, BlackUsername: null
                3) GameName: game3, WhiteUsername: null, BlackUsername: null
                """;

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Facade Join Game")
    public void joinGameFacade() throws Exception {
        RegisterResult registerResult = serverFacade.registerUser(user);
        CreateResult createResult = serverFacade.createGame(new CreateRequest(registerResult.getAuthToken(), "TestGame"));
        JoinRequest joinRequest = new JoinRequest("WHITE", 1);
        AuthTokenModel authModel = new AuthTokenModel(user.getUsername(), registerResult.getAuthToken());
        joinRequest.setAuthTokenModel(authModel);
        Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(joinRequest));
    }

    @Test
    @DisplayName("Repl Join Not Logged In")
    @Tag("Repl")
    public void replJoinNotLoggedIn(){
        String input = "join 1 WHITE\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Must be logged in to use command 'join'";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Join Wrong Parameters")
    @Tag("Repl")
    public void replJoinWrongParameters(){
        String input = "register user pass email\njoin 1\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "Incorrect number of parameters. 'join' command requires parameters: <id> <WHITE or BLACK";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }

    @Test
    @DisplayName("Repl Join Successfully")
    @Tag("Repl")
    public void replJoinGame(){
        String input = "register user pass email\ncreate TestGame\nlist\njoin 1 WHITE\nlist\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput = "1) GameName: testgame, WhiteUsername: user, BlackUsername: null";

        Assertions.assertTrue(consoleOutput.contains(expectedOutput));
    }
}
