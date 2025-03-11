package intermediaryclasses;

import java.util.UUID;

public class CreateRequest {
    private String authToken;
    private String gameName;

    public CreateRequest(String authToken){
        this.authToken = authToken;
        this.gameName = null;
    }

    public String getAuthToken(){
        return authToken;
    }

    public String getGameName(){
        return gameName;
    }

    public void setGameName(String gameName){
        this.gameName = gameName;
    }
}
