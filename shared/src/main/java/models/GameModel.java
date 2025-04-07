package models;

import chess.ChessGame;

public class GameModel {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;

    public GameModel(ChessGame game, String gameName, int gameID) {
        this.game = game;
        this.gameName = gameName;
        this.blackUsername = null;
        this.whiteUsername = null;
        this.gameID = gameID;
    }

    public GameModel(ChessGame game, String gameName, int gameID, String whiteUsername, String blackUsername) {
        this.game = game;
        this.gameName = gameName;
        this.blackUsername = blackUsername;
        this.whiteUsername = whiteUsername;
        this.gameID = gameID;
    }

    public int getGameID(){
        return gameID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public void setGame(ChessGame game){
        this.game = game;
    }
}

