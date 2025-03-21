package client;

import exception.ResponseException;
import intermediaryclasses.RegisterResult;
import models.UserModel;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import ui.Repl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;


public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl = "http://localhost:8080";
    private static ServerFacade serverFacade;
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade(serverUrl);
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
        UserModel user = new UserModel("user", "pass", "email");
        RegisterResult result = serverFacade.registerUser(user);
        Assertions.assertEquals(result.getUsername(), user.getUsername());
        Assertions.assertNotNull(result.getAuthToken());
    }

    @Test
    @DisplayName("Register Throws Error")
    public void registerError() {
        UserModel user = new UserModel("user", null, "email");
        Assertions.assertThrows(ResponseException.class, () -> { serverFacade.registerUser(user);});
    }

    @Test
    @DisplayName("Help Command Displays Options")
    @Tag("Repl")
    public void replHelp() {
        String input = "help\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        new Repl(serverUrl).run();

        String consoleOutput = outputStream.toString();
        String expectedOutput =
                """
                options:
                - help
                - quit
                - login <username> <password>
                - register <username> <password> <email>
                """;

//        Assertions.assertEquals(expectedOutput, consoleOutput);
        Assertions.assertTrue(consoleOutput.contains((expectedOutput)));
    }

}
