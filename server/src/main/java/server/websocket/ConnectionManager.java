package server.websocket;

import chess.ChessGame;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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
                if (c.gameID != command.getGameID()) {continue;};

                if (!c.session.isOpen()) {
                    removeList.add(c);
                    continue;
                }

                if (command.getAuthToken().equals(c.authToken)) {
                    LoadGameMessage msg = new LoadGameMessage(game);
                    c.send(new Gson().toJson(msg));
                } else {
                    NotificationMessage msg = new NotificationMessage(message);
                    c.send(new Gson().toJson(msg));
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
                        ErrorMessage errorMessage = new ErrorMessage(message);
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

    public void broadcastLeave(UserGameCommand command, String message) throws ResponseException {
        var removeList = new ArrayList<Connection>();

        for (var c : connections.values()) {
            try {
                if (c.gameID != command.getGameID()) {continue;};

                if (!c.session.isOpen()) {
                    removeList.add(c);
                    continue;
                }

                if (!command.getAuthToken().equals(c.authToken)) {
                    NotificationMessage notificationMessage = new NotificationMessage(message);
                    c.send(new Gson().toJson(notificationMessage));
                } else {
                    c.session.close();
                    removeList.add(c);
                }

            } catch (Exception e) {
                throw new ResponseException(500, "Error: Couldn't send message to client");
            }
        }

        removeConnections(removeList);
    }

    public void broadcastMove(MakeMoveCommand command, ChessGame game, String commandUser) throws ResponseException{
        var removeList = new ArrayList<Connection>();
        LoadGameMessage loadGameMessage = new LoadGameMessage(game);
        for(var c : connections.values()){
            try{
                if(c.gameID != command.getGameID()) {continue;};
                    //send LOAD_GAME to everyone in the game
                if(c.session.isOpen()){
                    c.send(new Gson().toJson(loadGameMessage));
                }
                //send Notification to everyone except the player that made the move
                if (!c.authToken.equals(command.getAuthToken())) {
                    if(c.session.isOpen()){
                        String pieceType = game.getBoard().getPiece(command.getMove().getEndPosition()).getPieceType().toString();
                        String startPosition = positionToNotation(command.getMove().getStartPosition());
                        String endPosition = positionToNotation(command.getMove().getEndPosition());
                        String message = String.format("%s moved %s from %s to %s", commandUser, pieceType, startPosition, endPosition);
                        NotificationMessage notificationMessage = new NotificationMessage(message);
                        c.send(new Gson().toJson(notificationMessage));
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

    public String positionToNotation(ChessPosition position){
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
        return letters[position.getColumn() - 1] + position.getRow();
    }

    public <T extends UserGameCommand> void broadcastNotification(T command, String message) throws ResponseException{
        var removeList = new ArrayList<Connection>();
        for(var c: connections.values()){
            try{
                if(c.gameID != command.getGameID()) {continue;}
                if(c.session.isOpen()){
                    c.send(new Gson().toJson(new NotificationMessage(message)));
                }else{
                    removeList.add(c);
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

