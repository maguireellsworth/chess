package dataaccess;

import models.AuthTokenModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthTokenDao {
    private HashMap<UUID, String> authTokens;

    public AuthTokenDao(){
        this.authTokens = new HashMap<>();
    }

    public void addAuthToken(AuthTokenModel authData){
        authTokens.put(authData.getAuthToken(), authData.getUsername());
    }

    public boolean authTokenexists(UUID authToken){
        return authTokens.containsKey(authToken);
    }

    public void deleteAuthToken(UUID authToken){
        authTokens.remove(authToken);
    }

    public void clear(){
        authTokens.clear();
    }
}
