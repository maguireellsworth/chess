package dataaccess;

import models.GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameDao {
    private HashMap<UUID, GameModel> games;

    public GameDao(){
        this.games = new HashMap<>();
    }

    public List<GameModel> listGames(){
        return new ArrayList<>(games.values());
    }

    public void clear(){
        games.clear();
    }
}
