package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import models.AuthTokenModel;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.glassfish.grizzly.utils.EchoFilter;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
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

    public void broadcastConnect(UserGameCommand command, String message) throws ResponseException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            try {
                if (c.gameID == command.getGameID()) {
                    if(c.session.isOpen()){
                        if (command.getAuthToken().equals(c.authToken)) {
                            LoadGameMessage msg = new LoadGameMessage("load game message");
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
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void broadcastError(UserGameCommand command){
        for(var c : connections.values()){
            try{
                if(c.gameID == command.getGameID() && command.getAuthToken().equals(c.authToken)){
                    ErrorMessage message = new ErrorMessage("Error: error not implemented");
                    c.send(new Gson().toJson(message));
                }
            }catch(Exception e){

            }
        }
    }
}

