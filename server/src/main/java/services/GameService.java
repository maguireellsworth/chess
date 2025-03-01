package services;

import chess.ChessGame;
import dataaccess.GameDao;
import models.GameModel;

import java.util.List;

public class GameService {
    private GameDao gameDao;

    public GameService(GameDao gameDao){
        this.gameDao = gameDao;
    }

    public List<GameModel> listGames(){
        return gameDao.listGames();
    }

}
