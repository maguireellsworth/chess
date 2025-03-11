package models;

import java.util.UUID;

public class AuthTokenModel {
    private String username;
    private String authToken;

    public AuthTokenModel(String username, String authToken){
        this.username = username;
        this.authToken = authToken;
    }

    public String getUsername(){
        return username;
    }

    public String getAuthToken(){
        return authToken;
    }
}
