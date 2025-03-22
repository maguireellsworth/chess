package ui;

import exception.ResponseException;
import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.RegisterResult;
import models.UserModel;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private String username;
    private String authToken = null;
    private ServerFacade server;
    private String serverUrl;

    public ChessClient(String serverUrl){
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> create(params);
                case "quit" -> "quitting";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        if(params.length != 3){
            return "Incorrect number of parameters. 'register' command requires parameters: <username> <password> <email>";
        }else{
            UserModel userModel = new UserModel(params[0], params[1], params[2]);
            try{
                RegisterResult result = server.registerUser(userModel);
                username = result.getUsername();
                authToken = result.getAuthToken();
                return "Successfully Registered!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Register, Problem: " + e.getMessage());
            }
        }
    }

    public String login(String... params) throws ResponseException{
        if(params.length != 2){
            return "Incorrect number of parameters. 'login' command requires parameters: <username> <password>";
        }else if(isLoggedIn()){
            return "Already logged in. Valid commands:\n" + help();
        }else{
            UserModel user = new UserModel(params[0], params[1], null);
            try{
                RegisterResult result = server.loginUser(user);
                username = result.getUsername();
                authToken = result.getAuthToken();
                return "Successfully Logged In!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Login, Problem: " + e.getMessage());
            }
        }
    }

    public boolean isLoggedIn(){
        return authToken != null;
    }

    public String logout() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to run command 'logout'\n" + help();
        }else{
            try{
                server.logoutUser(authToken);
                username = null;
                authToken = null;
                return "Successfully Logged Out!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Logout, Problem: " + e.getMessage());
            }
        }
    }

    public String create(String... params) throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to run command 'create'\n" + help();
        }else if(params.length != 1){
            return "Incorrect number of parameters. 'create' command requires parameters: <gamename>";
        }else{
            CreateRequest request = new CreateRequest(authToken,params[0]);
            try {
                CreateResult createResult = server.createGame(request);
                return "Successfully Created Game!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't create game, Problem: " + e.getMessage());
            }
        }
    }

    public String help(){
        String prelogin = """
                Options:
                - help
                - quit
                - login <username> <password>
                - register <username> <password> <email>
                """;
        String postLogin = """
                Options:
                - help
                - logout
                - create <gameName>
                - list
                - join <id> <WHITE or BLACK>
                - observe <id>
                """;
        return isLoggedIn()? postLogin : prelogin;
    }
}
