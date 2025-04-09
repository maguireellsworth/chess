package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import models.AuthTokenModel;
import models.GameModel;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.glassfish.grizzly.utils.EchoFilter;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authToken, Session session, int gameID) {
        var connection = new Connection(authToken, session, gameID);
        connections.put(authToken, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void broadcastConnect(UserGameCommand command, String message, ChessGame game) throws ResponseException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            try {
                if (c.gameID == command.getGameID()) {
                    if(c.session.isOpen()){
                        if (command.getAuthToken().equals(c.authToken)) {
                            LoadGameMessage msg = new LoadGameMessage(game);
                            c.send(new Gson().toJson(msg));
                        } else {
                            NotificationMessage msg = new NotificationMessage(message);
                            c.send(new Gson().toJson(msg));
                        }
                    }else {
                        removeList.add(c);
                    }
                }
            } catch (Exception e) {
                throw new ResponseException(500, "Error: couldn't send message to client");
            }
        }
        removeConnections(removeList);
    }

    public void broadcastError(UserGameCommand command, Session session, String message) throws ResponseException{
        var removeList = new ArrayList<Connection>();
        for(var c : connections.values()){
            try{
                if(c.gameID == command.getGameID() && c.session == session){
                    if(c.session.isOpen()) {
                        ErrorMessage errorMessage = new ErrorMessage("Error: " + message);
                        c.send(new Gson().toJson(errorMessage));
                    }else{
                        removeList.add(c);
                    }
                }
            }catch(Exception e){
                throw new ResponseException(500, "Error: Couldn't send message to client");
            }
        }
        removeConnections(removeList);
    }

    public void broadcastLeave(UserGameCommand command, String message) throws ResponseException{
        var removeList = new ArrayList<Connection>();
        for(var c : connections.values()){
            try{
                if(c.gameID == command.getGameID()){
                    if(c.session.isOpen()){
                        if(!command.getAuthToken().equals(c.authToken)){
                            NotificationMessage notificationMessage = new NotificationMessage(message);
                            c.send(new Gson().toJson(notificationMessage));
                        }else{
                            c.session.close();
                            removeList.add(c);
                        }
                    }else{
                        removeList.add(c);
                    }
                }
            }catch(Exception e){
                throw new ResponseException(500, "Error: Couldn't send message to client");
            }
        }
        removeConnections(removeList);
    }

    public void broadcastMove(MakeMoveCommand command, ChessGame game) throws ResponseException{
        var removeList = new ArrayList<Connection>();
        LoadGameMessage loadGameMessage = new LoadGameMessage(game);
        for(var c : connections.values()){
            try{
                if(c.gameID == command.getGameID()){
                    if(c.session.isOpen()){
                        c.send(new Gson().toJson(loadGameMessage));
                    }
                    if (!c.authToken.equals(command.getAuthToken())) {
                        if(c.session.isOpen()){
                            String message = "player made a move (edit this later)";
                            NotificationMessage notificationMessage = new NotificationMessage(message);
                            c.send(new Gson().toJson(notificationMessage));
                        }else{
                            removeList.add(c);
                        }
                    }
                    //TODO if in check or stalemate send notificationMessage
                }
            }catch(Exception e){
                throw new ResponseException(500, "Error: Couldn't send message to client");
            }
        }
        removeConnections(removeList);
    }

    public void removeConnections(List<Connection> removeList){
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }
}

