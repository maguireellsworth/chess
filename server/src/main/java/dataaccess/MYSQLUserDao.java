package dataaccess;

import models.UserModel;
import org.mindrot.jbcrypt.BCrypt;


public class MYSQLUserDao implements UserDao {
    private DatabaseHelper dbHelper;

    public MYSQLUserDao() throws Exception {
        this.dbHelper = new DatabaseHelper();
        dbHelper.configureTables();
    }

    public void addUser(UserModel user) throws Exception{
        var statement = "INSERT INTO users (username, password_hash, email) values(?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        dbHelper.executeUpdate(statement, user.getUsername(), hashedPassword, user.getEmail());
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
        dbHelper.executeUpdate(statement);
    }


}