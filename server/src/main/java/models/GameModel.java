package models;

import chess.ChessGame;

import java.util.UUID;

public class GameModel {
    private UUID gameID;
    private String whiteUsername;
    private String blckUsername;
    private String gameName;
    private ChessGame game;

//    public GameModel(UUID gameID)

    public GameModel(ChessGame game, String gameName, UUID gameID) {
        this.game = game;
        this.gameName = gameName;
        this.blckUsername = null;
        this.whiteUsername = null;
        this.gameID = gameID;
    }
}

