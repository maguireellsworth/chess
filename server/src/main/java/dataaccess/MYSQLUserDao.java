package dataaccess;

import models.UserModel;
import services.InvalidUserDataException;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;


public class MYSQLUserDao implements UserDao {

    public MYSQLUserDao() throws Exception {
        configureTable();
    }

    public void addUser(UserModel user) {
        var statement = "INSERT INTO users values(?, ?, ?)";
        executeUpdate(statement, user.getUsername(), user.getPassword(), user.getEmail());
    }

    public UserModel getUser(String username) {
        //TODO Actually implement! This does not work!
        return new UserModel(null, null, null);
    }

    public void clear() {

    }

    private String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
            );
            
            """
    };
    public void executeUpdate(String statement, Object... params){
        try(var conn = DatabaseManager.getConnection()){
            try(var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)){
                for(int i = 0; i < params.length; i++){
                    var param = params[i];
                    if(param instanceof String p){
                        preparedStatement.setString(i + 1, p);
                    }else if(param instanceof Integer p){
                        preparedStatement.setInt(i + 1, p);
                    }
                }
            }
        }catch(Exception e){

        }
    }

    public void configureTable() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new InvalidUserDataException("Error: (this is the wrong type) Unable to configure Database");
        }
    }
}