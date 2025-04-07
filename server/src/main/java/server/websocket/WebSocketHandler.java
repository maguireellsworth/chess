package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.GameDao;
import exception.ResponseException;
import intermediaryclasses.JoinRequest;
import models.AuthTokenModel;
import models.GameModel;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import services.GameService;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import services.UserService;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final UserService userService;
    private final GameService gameService;
    private final GameDao gameDao;

    public WebSocketHandler(UserService userService, GameService gameService, GameDao gameDao){
        this.userService = userService;
        this.gameService = gameService;
        this.gameDao = gameDao;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception{
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String type = jsonObject.get("commandType").getAsString();
        UserGameCommand command = switch(type){
            case "CONNECT" -> new Gson().fromJson(jsonObject, ConnectCommand.class);
            case "LEAVE" -> new Gson().fromJson(jsonObject, LeaveCommand.class);
            case "MAKE_MOVE" -> new Gson().fromJson(jsonObject, MakeMoveCommand.class);
            default -> new Gson().fromJson(jsonObject, UserGameCommand.class);
        };

        switch(command.getCommandType()){
            case CONNECT -> joinGame((ConnectCommand) command, session);
            case LEAVE -> leaveGame((LeaveCommand) command, session);
//            case MAKE_MOVE -> makeMove((MakeMoveCommand) command, session);
        }
    }

    public  void joinGame(ConnectCommand command, Session session) throws ResponseException {
        try {
            connections.add(command.getAuthToken(), session, command.getGameID());

            if (isNotValidCommand(command)) {
                String message = "User not authorized or invalid game";
                connections.broadcastError(command, session, message);
                return;
            }

            AuthTokenModel authModel = userService.getAuthTokenModel(command.getAuthToken());
            GameModel gameModel = gameDao.getGame(command.getGameID());
            String message;
            if (command.getPlayerColor() != null) {
                message = String.format("%s has joined the game as %s", authModel.getUsername(), command.getPlayerColor());
            } else {
                message = String.format("%s is observing the game", authModel.getUsername());
            }
            connections.broadcastConnect(command, message, gameModel.getGame());
        }catch(Exception e){
            throw new ResponseException(500, "Error: joinGame handler, Problem: " + e.getMessage());
        }
    }

    public void leaveGame(LeaveCommand command, Session session) throws Exception{
        try{
            if(isNotValidCommand(command)){
                String message = "User not authorized or invalid game";
                connections.broadcastError(command, session, message);
                return;
            }

            //update game in database
            gameDao.leaveGame(command.getPlayerColor(), command.getGameID());

            AuthTokenModel authModel = userService.getAuthTokenModel(command.getAuthToken());
            String message = String.format("%s has left the game", authModel.getUsername());
            connections.broadcastLeave(command, message);
        }catch(Exception e){
            throw new ResponseException(500, "Error: leaveGame handler, Problem: " + e.getMessage());
        }

    }


    public <T extends UserGameCommand> boolean isNotValidCommand(T command) throws Exception{
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        return !(userService.isValidUser(authToken) && gameService.isValidGame(gameID));
    }
}
