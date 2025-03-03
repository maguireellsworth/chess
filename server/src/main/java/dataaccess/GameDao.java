package dataaccess;

import IntermediaryClasses.CreateRequest;
import IntermediaryClasses.CreateResult;
import chess.ChessGame;
import models.GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameDao {
    private HashMap<String, GameModel> games;

    public GameDao(){
        this.games = new HashMap<>();
    }

    public List<GameModel> listGames(){
        return new ArrayList<>(games.values());
    }

    public void clear(){
        games.clear();
    }

    public CreateResult createGame(CreateRequest request, int gameID){
        games.put(request.getGameName(), new GameModel(new ChessGame(), request.getGameName(), gameID));
        return new CreateResult(gameID);
    }
}
