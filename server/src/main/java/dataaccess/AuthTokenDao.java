package dataaccess;

import models.AuthTokenModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthTokenDao {
    private HashMap<String, AuthTokenModel> authTokens;

    public AuthTokenDao(){
        this.authTokens = new HashMap<>();
    }

    public void addAuthToken(AuthTokenModel authData){
        authTokens.put(authData.getUsername(), authData);
    }
}
