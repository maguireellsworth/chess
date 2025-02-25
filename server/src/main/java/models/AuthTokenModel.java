package models;

import java.util.UUID;

public class AuthTokenModel {
    private String username;
    private UUID authToken;

    public AuthTokenModel(String username, UUID authToken){
        this.username = username;
        this.authToken = authToken;
    }

    public String getUsername(){
        return username;
    }

    public UUID getAuthToken(){
        return authToken;
    }
}
