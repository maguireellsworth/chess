package dataaccess;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import chess.ChessGame;
import models.GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameDao {
    private HashMap<Integer, GameModel> games;

    public GameDao(){
        this.games = new HashMap<Integer, GameModel>();
    }

    public List<GameModel> listGames(){
        return new ArrayList<>(games.values());
    }

    public void clear(){
        games.clear();
    }

    public CreateResult createGame(CreateRequest request, int gameID){
        games.put(gameID, new GameModel(new ChessGame(), request.getGameName(), gameID));
        return new CreateResult(gameID);
    }

    public GameModel getGame(int gameID){
        return games.get(gameID);
    }
}
