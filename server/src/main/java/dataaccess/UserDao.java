package dataaccess;

import models.UserModel;
import java.util.HashMap;

public interface UserDao {

    UserModel getUser(String username);

    void addUser(UserModel user);

    void clear();

}
