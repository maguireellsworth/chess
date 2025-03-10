package dataaccess;

import models.UserModel;
import org.mindrot.jbcrypt.BCrypt;
import services.InvalidUserDataException;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;


public class MYSQLUserDao implements UserDao {

    public MYSQLUserDao() throws Exception {
        configureTable();
    }

    public void addUser(UserModel user) throws Exception{
        var statement = "INSERT INTO users (username, password_hash, email) values(?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        executeUpdate(statement, user.getUsername(), hashedPassword, user.getEmail());
    }

    public UserModel getUser(String username) throws Exception{
        try(var conn = DatabaseManager.getConnection()){
            var statement = "SELECT * from users WHERE username = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, username);
                try(var result = preparedStatement.executeQuery()){
                    if(result.next()){
                        int id = result.getInt("id");
                        String uname = result.getString("username");
                        String password = result.getString("password_hash");
                        String email = result.getString("email");
                        return new UserModel(uname, password, email);
                    }else{
                        return null;
                    }
                }
            }
        }catch (Exception e){
            throw new Exception("Error: getUser, Problem: " + e.getMessage());
        }

    }

    public void clear() throws Exception{
        var statement = "TRUNCATE TABLE users";
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

    private String createStatement =
            """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL
            );
            
            """;

    public void configureTable() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
                try (var preparedStatement = conn.prepareStatement(createStatement)) {
                    preparedStatement.executeUpdate();
                }
        } catch (SQLException e) {
            throw new InvalidUserDataException("Error: " + e.getMessage());
        }
    }
}