package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.AuthTokenModel;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import services.GameService;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import services.UserService;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {
    private ConnectionManager connections = new ConnectionManager();
    private UserService userService;
    private GameService gameService;

    public WebSocketHandler(UserService userService, GameService gameService){
        this.userService = userService;
        this.gameService = gameService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception{
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("commandType").getAsString();
        UserGameCommand command = switch(type){
            case "CONNECT" -> new Gson().fromJson(jsonObject, ConnectCommand.class);
            case "MAKE_MOVE" -> new Gson().fromJson(jsonObject, MakeMoveCommand.class);
            default -> new Gson().fromJson(jsonObject, UserGameCommand.class);
        };


//        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()){
            case CONNECT -> joinGame((ConnectCommand) command, session);
        }
    }

    public  void joinGame(ConnectCommand command, Session session) throws Exception{
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();
        connections.add(authToken, session, gameID);

        if(!userService.isValidUser(authToken) || !gameService.isValidGame(gameID)){
            connections.broadcastError(command);
            return;
        }

        AuthTokenModel authModel = userService.getAuthTokenModel(authToken);
        var message = String.format("%s has joined the game as %s", authModel.getUsername(), command.getPlayerColor());
//        ChessGame game = new ChessGame();
        connections.broadcastConnect(command, message);
    }
}
