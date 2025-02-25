package dataaccess;

import models.AuthTokenModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthTokenDao {
    private HashMap<String, UUID> authTokens;

    public AuthTokenDao(){
        this.authTokens = new HashMap<>();
    }

    public Map addAuthToken(AuthTokenModel authData){
        authTokens.put(authData.getUsername(), authData.getAuthToken());
    }
}
