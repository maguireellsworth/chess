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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MYSQLGameDao implements GameDao{
    public MYSQLGameDao() throws Exception{
        configureTable();
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
        executeUpdate(statement);
    }

    @Override
    public CreateResult createGame(CreateRequest request) throws Exception{
        var statement = "INSERT INTO games (game_name, game) values(?, ?)";
        String  json = new Gson().toJson(new ChessGame());
        int gameID = executeUpdate(statement, request.getGameName(), json);
        return new CreateResult(gameID);
    }

    @Override
    public GameModel getGame(int gameID) {
        return null;
    }

    public void joinGame(JoinRequest joinRequest) throws Exception{
        if(!colorAvailble(joinRequest)){
            throw new UserAlreadyExistsException("Error: Color is already taken");
        }
        String statement;
        if(joinRequest.getPlayerColor().equals("WHITE")){
            statement = "UPDATE games SET white_username = ? WHERE game_id = ?";
        }else if(joinRequest.getPlayerColor().equals("BLACK")){
            statement = "UPDATE games SET black_username = ? WHERE game_id = ?";
        }else{
            throw new InvalidUserDataException("Error: Player Color cannot be left blank");
        }
        executeUpdate(statement, joinRequest.getAuthTokenModel().getUsername(), joinRequest.getGameID());
    }

    public boolean colorAvailble(JoinRequest joinRequest)throws Exception{
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
            throw new Exception("Error: spotAvailble, Problem: " + e.getMessage());
        }
    }

    public int executeUpdate(String statement, Object... params)throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            try(var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        preparedStatement.setString(i + 1, p);
                    } else if (param instanceof Integer p) {
                        preparedStatement.setInt(i + 1, p);
                    }
                }
                preparedStatement.executeUpdate();

                var rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }catch(Exception e){
            throw new Exception("Error: " + e.getMessage());
        }
    }

    private String CreateStatement =
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT AUTO_INCREMENT PRIMARY KEY,
                white_username varchar(50),
                black_username varchar(50),
                game_name varchar(50) UNIQUE,
                game TEXT
            );
            """;

    public void configureTable() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(CreateStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new InvalidUserDataException("Error: " + e.getMessage());
        }
    }
}
