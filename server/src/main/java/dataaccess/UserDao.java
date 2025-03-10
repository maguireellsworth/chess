package dataaccess;

import models.UserModel;

public interface UserDao {

    UserModel getUser(String username) throws Exception;

    void addUser(UserModel user) throws Exception;

    void clear() throws Exception;

}
