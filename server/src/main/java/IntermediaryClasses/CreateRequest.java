package IntermediaryClasses;

import java.util.UUID;

public class CreateRequest {
    private UUID authToken;
    private String gameName;

    public CreateRequest(UUID authToken){
        this.authToken = authToken;
        this.gameName = null;
    }

    public UUID getAuthToken(){
        return authToken;
    }

    public String getGameName(){
        return gameName;
    }

    public void setGameName(String gameName){
        this.gameName = gameName;
    }
}
