package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
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

import java.util.Objects;

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
            case MAKE_MOVE -> makeMove((MakeMoveCommand) command, session);
            case RESIGN -> resign(command, session);
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
            GameModel model = gameDao.getGame(command.getGameID());
            String commandUser = userService.getAuthTokenModel(command.getAuthToken()).getUsername();

            //update game in database
            if(isPlayer(model, commandUser)){
                String playerColor = commandUser.equals(model.getWhiteUsername()) ? "WHITE" : "BLACK";
                gameDao.leaveGame(playerColor, command.getGameID());
            }

            AuthTokenModel authModel = userService.getAuthTokenModel(command.getAuthToken());
            String message = String.format("%s has left the game", authModel.getUsername());
            connections.broadcastLeave(command, message);
        }catch(Exception e){
            throw new ResponseException(500, "Error: leaveGame handler, Problem: " + e.getMessage());
        }

    }

    public void makeMove(MakeMoveCommand command, Session session) throws Exception{
        try{
            if(isNotValidCommand(command)){
                String message = "User not authorized or invalid game";
                connections.broadcastError(command, session, message);
                return;
            }
            GameModel gameModel = gameDao.getGame(command.getGameID());
            ChessGame game = gameModel.getGame();
            String commandUsername = userService.getAuthTokenModel(command.getAuthToken()).getUsername();
            ChessPiece piece = gameModel.getGame().getBoard().getPiece(command.getMove().getStartPosition());
            boolean moveIsOk = checkMove(command, session, gameModel, commandUsername, piece);
            if(moveIsOk){
                gameModel.getGame().makeMove(command.getMove());
                gameDao.updateGame(gameModel);
                connections.broadcastMove(command, gameModel.getGame(), commandUsername);
                if(game.isInCheckmate(ChessGame.TeamColor.WHITE)){
                    String message = String.format("%s is in checkmate, %s won the game!", gameModel.getWhiteUsername(), gameModel.getBlackUsername());
                    connections.broadcastNotification(command, message);
                }else if(game.isInCheckmate(ChessGame.TeamColor.BLACK)){
                    String message = String.format("%s is in checkmate, %s won the game!", gameModel.getBlackUsername(), gameModel.getWhiteUsername());
                    connections.broadcastNotification(command, message);
                }else if(game.isInCheck(ChessGame.TeamColor.WHITE)) {
                    String message = String.format("%s is in check", gameModel.getWhiteUsername());
                    connections.broadcastNotification(command, message);
                }else if(game.isInCheck(ChessGame.TeamColor.BLACK)){
                    String message = String.format("%s is in check", gameModel.getBlackUsername());
                    connections.broadcastNotification(command, message);
                }
            }

        }catch(Exception e){
            throw new ResponseException(500, "Error: makeMove Handler, Problem: " + e.getMessage());
        }
    }

    public void resign(UserGameCommand command, Session session) throws ResponseException{
        try{
            if(isNotValidCommand(command)){
                String message = "User not authorized or invalid game";
                connections.broadcastError(command, session, message);
            }else{
                GameModel game = gameDao.getGame(command.getGameID());
                String commandUser = userService.getAuthTokenModel(command.getAuthToken()).getUsername();
                if(isPlayer(game, commandUser)){
                    String message = String.format("%s has resigned, game is over", commandUser);
                    connections.broadcastNotification(command, message);
                }else{
                    String message = "Observers cannot resign";
                    connections.broadcastError(command, session, message);
                }
            }
        }catch(Exception e){
            throw new ResponseException(500, "Error: resign, Problem: " + e.getMessage());
        }
    }

    public boolean checkMove(MakeMoveCommand command,
                             Session session,
                             GameModel gameModel,
                             String commandUsername,
                             ChessPiece piece) throws Exception{
        boolean moveIsOk = true;
        if(!isPlayer(gameModel, commandUsername)){
            String message = "Observers cannot make moves";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }
        else if(!isPlayerTurn(gameModel, commandUsername)){
            String message = "Not your turn";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }else if(isNotValidMoveFormat(command.getMove())){
            String message = "Invalid move format";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }else if(piece == null){
            String message = "Space is empty at desired location";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }else if(!isPlayerPiece(gameModel, commandUsername, piece)){
            String message = "Not your piece";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }else if(!gameModel.getGame().isValidMove(command.getMove())) {
            String message = "Invalid move";
            connections.broadcastError(command, session, message);
            moveIsOk = false;
        }
        return moveIsOk;
    }

    public boolean isPlayerPiece(GameModel model, String commandUser, ChessPiece piece){
        String pieceColor = piece.getTeamColor().toString();
        String playerColor = commandUser.equals(model.getWhiteUsername()) ? "WHITE" : "BLACK";
        return pieceColor.equals(playerColor);
    }

    public boolean isPlayerTurn(GameModel model, String commandUser){
        String playerTurn = model.getGame().getTeamTurn().toString();
        String turnUser = playerTurn.equals("WHITE") ? model.getWhiteUsername() : model.getBlackUsername();
        return turnUser.equals(commandUser);
    }

    public boolean isPlayer(GameModel model, String commandUser){
        String white = model.getWhiteUsername();
        String black = model.getBlackUsername();
        return ((commandUser != null && commandUser.equals(white)) ||
                (commandUser != null && commandUser.equals(black)));
    }

    public boolean isNotValidMoveFormat(ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        return (start.getColumn() == -1
                || start.getRow() < 0
                ||start.getRow() > 8
                || end.getColumn() == -1
                || end.getRow() < 0
                || end.getRow() > 8);
    }

    public <T extends UserGameCommand> boolean isNotValidCommand(T command) throws Exception{
        String authToken = command.getAuthToken();
        int gameID = command.getGameID();

        return !(userService.isValidUser(authToken) && gameService.isValidGame(gameID));
    }
}
