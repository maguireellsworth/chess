package dataaccess;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import chess.ChessGame;
import intermediaryclasses.JoinRequest;
import models.GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface GameDao {
    List<GameModel> listGames() throws Exception;

    void clear() throws Exception;

    CreateResult createGame(CreateRequest request) throws Exception;

    GameModel getGame(int gameID) throws Exception;

    void joinGame(JoinRequest joinRequest) throws Exception;
}
