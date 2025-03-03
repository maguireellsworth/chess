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

    public int getGameID(){
        return gameID;
    }
}

