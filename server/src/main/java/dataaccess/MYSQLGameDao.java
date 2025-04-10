package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import intermediaryclasses.CreateRequest;
import intermediaryclasses.CreateResult;
import intermediaryclasses.JoinRequest;
import models.GameModel;
import services.InvalidUserDataException;
import services.UserAlreadyExistsException;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MYSQLGameDao implements GameDao{
    private DatabaseHelper dbHelper;

    public MYSQLGameDao() throws Exception{
        this.dbHelper = new DatabaseHelper();
        dbHelper.configureTables();
    }

    @Override
    public List<GameModel> listGames() throws Exception{
        List allGames = new ArrayList<>();
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM games";
            try(var preparedStatement = conn.prepareStatement(statement)){
                try(var result = preparedStatement.executeQuery()){
                        while(result.next()){
                            allGames.add(readGameModel(result));
                        }
                    return allGames;
                }
            }
        }catch (Exception e){
            throw new Exception("Error: listGames, Problem: " + e.getMessage());
        }
    }

    public GameModel readGameModel(ResultSet result)throws Exception{
        int gameID = result.getInt("game_id");
        String whiteUsername = result.getString("white_username");
        String blackUsername = result.getString("black_username");
        String gameName = result.getString("game_name");
        String chessJSON = result.getString("game");
        ChessGame game = new Gson().fromJson(chessJSON, ChessGame.class);
        GameModel model =  new GameModel(game, gameName, gameID);
        model.setWhiteUsername(whiteUsername);
        model.setBlackUsername(blackUsername);
        return model;
    }

    @Override
    public void clear() throws Exception{
        var statement = "TRUNCATE TABLE games";
        dbHelper.executeUpdate(statement);
    }

    @Override
    public CreateResult createGame(CreateRequest request) throws Exception{
        var statement = "INSERT INTO games (game_name, game) values(?, ?)";
        String  json = new Gson().toJson(new ChessGame());
        int gameID = dbHelper.executeUpdate(statement, request.getGameName(), json);
        return new CreateResult(gameID);
    }

    @Override
    public GameModel getGame(int gameID) throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * from games WHERE game_id = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setInt(1, gameID);
                try(var result = preparedStatement.executeQuery()){
                    if(result.next()){
                        int id = result.getInt("game_id");
                        String whiteUsername = result.getString("white_username");
                        String blackUsername = result.getString("black_username");
                        String gameName = result.getString("game_name");
                        ChessGame game = new Gson().fromJson(result.getString("game"), ChessGame.class);
                        return new GameModel(game, gameName, id, whiteUsername, blackUsername);
                    }else{
                        return null;
                    }
                }
            }
        }catch (Exception e){
            throw new Exception("Error: authTokenExists, Problem: " + e.getMessage());
        }
    }

    public void joinGame(JoinRequest joinRequest) throws Exception{
        if(!colorAvailable(joinRequest)){
            throw new UserAlreadyExistsException("Error: Color is already taken");
        }
        String statement = getUpdateUserStatement(joinRequest.getPlayerColor());
        dbHelper.executeUpdate(statement, joinRequest.getAuthTokenModel().getUsername(), joinRequest.getGameID());
    }

    public void leaveGame(String playerColor, int gameID) throws Exception{
        String statement = getUpdateUserStatement(playerColor);
        dbHelper.executeUpdate(statement, null, gameID);
    }

    public String getUpdateUserStatement(String playerColor){
        if(playerColor.equals("WHITE")){
            return "UPDATE games SET white_username = ? WHERE game_id = ?";
        }else if(playerColor.equals("BLACK")){
            return "UPDATE games SET black_username = ? WHERE game_id = ?";
        }else{
            throw new InvalidUserDataException("Error: Player Color cannot be left blank");
        }
    }

    public boolean colorAvailable(JoinRequest joinRequest)throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            String columnName;
            if(joinRequest.getPlayerColor().equals("WHITE")){
                columnName = "white_username";
            }else if(joinRequest.getPlayerColor().equals("BLACK")){
                columnName = "black_username";
            }else{
                throw new InvalidUserDataException("Error: Incorrect player color");
            }
            String statement = "SELECT * FROM games WHERE game_id = ? AND " + columnName + " IS NULL";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setInt(1, joinRequest.getGameID());
                try(var result = preparedStatement.executeQuery()){
                    return result.next();
                }
            }
        }catch(Exception e){
            throw new Exception("Error: spotAvailable, Problem: " + e.getMessage());
        }
    }

    public boolean gameExists(int gameID) throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * from games WHERE game_id = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setInt(1, gameID);
                try(var result = preparedStatement.executeQuery()){
                    return result.next();
                }
            }
        }catch (Exception e){
            throw new Exception("Error: authTokenExists, Problem: " + e.getMessage());
        }
    }

    public void updateGame(GameModel gameModel) throws Exception{
        String statement = "UPDATE games SET game = ? WHERE game_id = ?";
        dbHelper.executeUpdate(statement, new Gson().toJson(gameModel.getGame()), gameModel.getGameID());
    }
}
