package dataaccess;

import models.AuthTokenModel;
import services.InvalidUserDataException;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class MYSQLAuthTokenDao implements AuthTokenDao{
    public MYSQLAuthTokenDao() throws Exception{
        configureTable();
    }
    @Override
    public void addAuthToken(AuthTokenModel authModel) throws Exception{
        var statement = "INSERT INTO authtokens values(?, ?)";
        executeUpdate(statement, authModel.getAuthToken(), authModel.getUsername());
    }

    @Override
    public boolean authTokenExists(String authToken) throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * from authtokens WHERE authtoken = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, authToken);
                try(var result = preparedStatement.executeQuery()){
                    return result.next();
                }
            }
        }catch (Exception e){
            throw new Exception("Error: authTokenExists, Problem: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuthToken(String authToken) throws Exception{
        var statement = "DELETE FROM authtokens where authtoken = ?";
        executeUpdate(statement, authToken);
    }

    @Override
    public AuthTokenModel getAuthTokenModel(String authToken) throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * FROM authtokens where authtoken = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, authToken);
                try(var result = preparedStatement.executeQuery()){
                    if(result.next()){
                        String authtoken = result.getString("authtoken");
                        String username = result.getString("username");
                        return new AuthTokenModel(username, authtoken);
                    }else{
                        return null;
                    }
                }
            }
        }catch (Exception e){
            throw new Exception("Error: getAuthTokenModel, Problem: " + e.getMessage());
        }

    }

    @Override
    public void updateAuthToken(AuthTokenModel authModel) throws Exception {
        var statement = "UPDATE authtokens SET authtoken = ? WHERE username = ?";
        executeUpdate(statement, authModel.getAuthToken(), authModel.getUsername());
    }

    @Override
    public void clear() throws Exception{
        var statement = "TRUNCATE TABLE authtokens";
        executeUpdate(statement);
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
            CREATE TABLE IF NOT EXISTS authtokens (
                authtoken char(36) UNIQUE NOT NULL,
                username varchar(50) UNIQUE NOT NULL
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
