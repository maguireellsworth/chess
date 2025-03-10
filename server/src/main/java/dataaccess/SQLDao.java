package dataaccess;

import services.InvalidUserDataException;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLDao {

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

    public void configureTable() throws Exception {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()){
            for(var statement: createStatements){
                try(var preparedStatement = conn.prepareStatement(statement)){
                    preparedStatement.executeUpdate();
                }
            }
        }catch (SQLException e) {
            throw new InvalidUserDataException("Error: (this is the wrong type) Unable to configure Database");
        }
    }

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
}
