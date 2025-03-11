package dataaccess;

import models.AuthTokenModel;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthTokenDao implements AuthTokenDao{
    private HashMap<String, AuthTokenModel> authTokens;

    public MemoryAuthTokenDao(){
        this.authTokens = new HashMap<>();
    }

    public void addAuthToken(AuthTokenModel authData){
        authTokens.put(authData.getAuthToken(), authData);
    }

    public boolean authTokenExists(String authToken){
        return authTokens.containsKey(authToken);
    }

    public void deleteAuthToken(String authToken){
        authTokens.remove(authToken);
    }

    public AuthTokenModel getAuthTokenModel(String authToken){
        return authTokens.get(authToken);
    }

    public void clear(){
        authTokens.clear();
    }
}
