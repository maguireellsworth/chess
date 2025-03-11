package dataaccess;

import models.UserModel;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

public class MemoryUserDao implements UserDao {
    private HashMap<String, UserModel> users;

    public MemoryUserDao(){
        this.users = new HashMap<>();
    }

    public UserModel getUser(String username){
        return users.get(username);
    }

    public void addUser(UserModel user){
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        users.put(user.getUsername(), new UserModel(user.getUsername(), hashedPassword, user.getEmail()));
    }

    public void clear(){
        users.clear();
    }
}