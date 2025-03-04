package services;

import IntermediaryClasses.CreateRequest;
import IntermediaryClasses.CreateResult;
import IntermediaryClasses.JoinRequest;
import dataaccess.GameDao;
import models.GameModel;

import java.util.List;
import java.util.UUID;

public class GameService {
    private GameDao gameDao;
    private UserService userService;
    private int gameID;

    public GameService(GameDao gameDao, UserService userService){
        this.gameDao = gameDao;
        this.userService = userService;
        this.gameID = 1;
    }

    public List<GameModel> listGames(UUID authToken){
        if(userService.isValidUser(authToken)){
            return gameDao.listGames();
        }else{
            throw new InvalidCredentialsException("Error: Unauthorized");
        }
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
            throw new InvalidCredentialsException("Error: Unauthorized");
        }
    }

    public void joinGame(JoinRequest request){
        //if not authenticated or bad input
        String color = request.getPlayerColor();
        if(!userService.isValidUser(request.getAuthTokenModel().getAuthToken())){
            throw new InvalidCredentialsException("Error: Unauthorized");
        }else if(color == null || (!color.equals("WHITE") && !color.equals("BLACK")) || request.getGameID() == 0){
            throw new InvalidUserDataException("Error: Bad Request");
        }

        //position is already taken else set position
        GameModel game = gameDao.getGame(request.getGameID());
        if(request.getPlayerColor().equals("WHITE")){
            if(game.getWhiteUsername() == null){
                   game.setWhiteUsername(request.getAuthTokenModel().getUsername());
            }else{
                throw new UserAlreadyExistsException("Error: Color already selected by another player");
            }
        }else{
            if (game.getBlackUsername() == null) {
                game.setBlackUsername(request.getAuthTokenModel().getUsername());
            } else {
                throw new UserAlreadyExistsException("Error: Color already selected by another player");
            }
        }
    }

}
