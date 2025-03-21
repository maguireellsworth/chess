package ui;

import exception.ResponseException;
import intermediaryclasses.RegisterResult;
import models.UserModel;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private String username;
    private String authToken;
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
//                case "login" -> login(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException{
        // create UserModel from params
        if(params.length != 3){
            return "Incorrect number of parameters. 'register' command requires parameters: <username> <password> <email> ";
        }else{
            UserModel userModel = new UserModel(params[0], params[1], params[2]);
            try{
                RegisterResult result = server.registerUser(userModel);
                username = result.getUsername();
                authToken = result.getAuthToken();
                return "Successfully Registered";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't talk to server, Problem: " + e.getMessage());
            }
        }
    }

//    public String login(String... params) throws ResponseException{
//        //create UserModel from params
//        if(params.length)
//        //make call to serverFacade
//        return null;
//    }

    public String help(){
        return """
                options:
                - help
                - quit
                - login <username> <password>
                - register <username> <password> <email>
                """;
    }
}
