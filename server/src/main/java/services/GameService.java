package services;

import IntermediaryClasses.CreateRequest;
import IntermediaryClasses.CreateResult;
import IntermediaryClasses.JoinRequest;
import dataaccess.GameDao;
import models.GameModel;

import java.util.List;

public class GameService {
    private GameDao gameDao;
    private UserService userService;
    private int gameID;

    public GameService(GameDao gameDao, UserService userService){
        this.gameDao = gameDao;
        this.userService = userService;
        this.gameID = 1;
    }

    public List<GameModel> listGames(){
        
        return gameDao.listGames();
    }

    private void incrementID(){
        gameID += 1;
    }

    public CreateResult createGame(CreateRequest request){
        if(request.getGameName() == null){
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        } else if(userService.isValidUser(request.getAuthToken())){
            incrementID();
            return  gameDao.createGame(request, gameID);
        }else{
            throw new InvalidCredentialsException("Error: unauthorized");
        }
    }

    public void joinGame(JoinRequest request){
        if(!userService.isValidUser(request.getAuthTokenModel().getAuthToken())){
            throw new InvalidCredentialsException("Error: unauthorized");
        }else{
            GameModel game = gameDao.getGame(request.getGameID());
            //TODO check player color and add if empty, throw error if not
        }
    }

}
