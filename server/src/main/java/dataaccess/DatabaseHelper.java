package dataaccess;

import services.InvalidUserDataException;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class DatabaseHelper {

    public int executeUpdate(String statement, Object... params)throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            try(var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        preparedStatement.setString(i + 1, p);
                    } else if (param instanceof Integer p) {
                        preparedStatement.setInt(i + 1, p);
                    }else{
                        preparedStatement.setNull(i + 1, Types.VARCHAR);
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

    private String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT AUTO_INCREMENT PRIMARY KEY,
                white_username varchar(50),
                black_username varchar(50),
                game_name varchar(50) UNIQUE,
                game TEXT
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS authtokens (
                authtoken char(36) UNIQUE NOT NULL,
                username varchar(50) NOT NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL
            );
            
            """
    };

    public void configureTables() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for(String statement : createStatements){
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new InvalidUserDataException("Error: " + e.getMessage());
        }
    }
}
