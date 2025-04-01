package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import models.AuthTokenModel;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;

import services.UserService;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {
    private ConnectionManager connections = new ConnectionManager();
    private UserService userService;

    public WebSocketHandler(UserService userService){
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception{
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()){
            case CONNECT -> joinGame(command, session);
        }
    }

    public void joinGame(UserGameCommand command, Session session)throws Exception{
        String authToken = command.getAuthToken();
        AuthTokenModel authModel = userService.getAuthTokenModel(authToken);

        connections.add(authToken, session, command.getGameID());
        var message = String.format("%s has joined the game as ______", authModel.getUsername());
        ChessGame game = new ChessGame();
        LoadGameMessage notification = new LoadGameMessage(message);
        connections.broadcast(command, message);
    }
}
