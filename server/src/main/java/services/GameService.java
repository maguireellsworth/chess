package services;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.JoinRequest;
import dataaccess.GameDao;
import models.GameModel;

import java.util.List;
import java.util.UUID;

public class GameService {
    private GameDao gameDao;
    private UserService userService;


    public GameService(GameDao gameDao, UserService userService){
        this.gameDao = gameDao;
        this.userService = userService;
    }

    public List<GameModel> listGames(String authToken)throws Exception{
        if(userService.isValidUser(authToken)){
            return gameDao.listGames();
        }else{
            throw new InvalidCredentialsException("Error: Unauthorized");
        }
    }

    public CreateResult createGame(CreateRequest request)throws Exception{
        if(request.getGameName() == null){
            throw new InvalidUserDataException("Error: Empty fields are not allowed");
        } else if(userService.isValidUser(request.getAuthToken())){
            return  gameDao.createGame(request);
        }else{
            throw new InvalidCredentialsException("Error: Unauthorized");
        }
    }

    public void joinGame(JoinRequest request)throws Exception{
        //if not authenticated or bad input
        String color = request.getPlayerColor();
        if(!userService.isValidUser(request.getAuthTokenModel().getAuthToken())){
            throw new InvalidCredentialsException("Error: Unauthorized");
        }else if(color == null || (!color.equals("WHITE") && !color.equals("BLACK")) || request.getGameID() == 0){
            throw new InvalidUserDataException("Error: Bad Request");
        }
        gameDao.joinGame(request);
    }

}
