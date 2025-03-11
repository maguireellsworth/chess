package dataaccess;

import models.AuthTokenModel;

import java.util.UUID;

public interface AuthTokenDao {

    void addAuthToken(AuthTokenModel authModel)throws Exception;

    boolean authTokenExists(String authToken) throws Exception;

    void deleteAuthToken(String authToken);

    AuthTokenModel getAuthTokenModel(String authToken);

    void clear();
}
