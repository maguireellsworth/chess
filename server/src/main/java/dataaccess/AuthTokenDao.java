package dataaccess;

import models.AuthTokenModel;

import java.util.UUID;

public interface AuthTokenDao {

    void addAuthToken(AuthTokenModel authModel) throws Exception;

    boolean authTokenExists(String authToken) throws Exception;

    void deleteAuthToken(String authToken) throws Exception;

    AuthTokenModel getAuthTokenModel(String authToken) throws Exception;

    void updateAuthToken(AuthTokenModel authModel) throws Exception;

    void clear() throws Exception;
}
