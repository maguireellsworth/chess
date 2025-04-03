package dataaccess;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import chess.ChessGame;
import intermediaryclasses.JoinRequest;
import models.GameModel;
import services.UserAlreadyExistsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDao implements GameDao{
    private HashMap<Integer, GameModel> games;
    private int gameID;

    public MemoryGameDao(){
        this.games = new HashMap<Integer, GameModel>();
        this.gameID = 1;

    }

    public List<GameModel> listGames(){
        return new ArrayList<>(games.values());
    }

    public void clear(){
        games.clear();
    }

    public CreateResult createGame(CreateRequest request){
        incrementID();
        games.put(gameID, new GameModel(new ChessGame(), request.getGameName(), gameID));
        return new CreateResult(gameID);
    }

    public GameModel getGame(int gameID){
        return games.get(gameID);
    }

    public boolean gameExists(int gameI){
        return games.get(gameID) != null;
    }


    private void incrementID(){
        gameID += 1;
    }

    public void joinGame(JoinRequest joinRequest){
        //position is already taken else set position
        GameModel game = getGame(joinRequest.getGameID());
        if(joinRequest.getPlayerColor().equals("WHITE")){
            if(game.getWhiteUsername() == null){
                game.setWhiteUsername(joinRequest.getAuthTokenModel().getUsername());
            }else{
                throw new UserAlreadyExistsException("Error: Color already selected by another player");
            }
        }else{
            if (game.getBlackUsername() == null) {
                game.setBlackUsername(joinRequest.getAuthTokenModel().getUsername());
            } else {
                throw new UserAlreadyExistsException("Error: Color already selected by another player");
            }
        }
    }
}