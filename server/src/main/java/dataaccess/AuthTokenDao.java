package dataaccess;

import models.AuthTokenModel;

import java.util.HashMap;
import java.util.UUID;

public class AuthTokenDao {
    private HashMap<UUID, AuthTokenModel> authTokens;

    public AuthTokenDao(){
        this.authTokens = new HashMap<>();
    }

    public void addAuthToken(AuthTokenModel authData){
        authTokens.put(authData.getAuthToken(), authData);
    }

    public boolean authTokenExists(UUID authToken){
        return authTokens.containsKey(authToken);
    }

    public void deleteAuthToken(UUID authToken){
        authTokens.remove(authToken);
    }

    public AuthTokenModel getAuthTokenModel(UUID authToken){
        return authTokens.get(authToken);
    }

    public void clear(){
        authTokens.clear();
    }
}
