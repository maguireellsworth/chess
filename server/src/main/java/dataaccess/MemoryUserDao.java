package dataaccess;

import models.UserModel;
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
        users.put(user.getUsername(), user);
    }

    public void clear(){
        users.clear();
    }
}