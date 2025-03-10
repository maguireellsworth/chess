package dataaccess;

import models.UserModel;
import services.InvalidUserDataException;

import javax.xml.crypto.Data;
import java.sql.SQLException;


public class MYSQLUserDao extends SQLDao{

    public MYSQLUserDao() throws Exception{
        configureTable();
    }

    public void addUser(UserModel user){
        var statement = "INSERT INTO users values(?, ?, ?)";
        executeUpdate(statement, user.getUsername(), user.getPassword(), user.getEmail());
    }

    public UserModel getUser(String username){

    }
}
