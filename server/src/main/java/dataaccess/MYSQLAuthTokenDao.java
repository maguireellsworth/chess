package dataaccess;

import models.AuthTokenModel;
import services.InvalidUserDataException;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class MYSQLAuthTokenDao implements AuthTokenDao{
    private DatabaseHelper dbHelper;

    public MYSQLAuthTokenDao() throws Exception{
        this.dbHelper = new DatabaseHelper();
        dbHelper.configureTables();
    }
    @Override
    public void addAuthToken(AuthTokenModel authModel) throws Exception{
        var statement = "INSERT INTO authtokens values(?, ?)";
        dbHelper.executeUpdate(statement, authModel.getAuthToken(), authModel.getUsername());
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
        dbHelper.executeUpdate(statement, authToken);
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
    public void clear() throws Exception{
        var statement = "TRUNCATE TABLE authtokens";
        dbHelper.executeUpdate(statement);
    }

}
