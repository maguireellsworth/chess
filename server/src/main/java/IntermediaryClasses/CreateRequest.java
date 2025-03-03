package IntermediaryClasses;

import java.util.UUID;

public class CreateRequest {
    private UUID authToken;
    private String gameName;

    public CreateRequest(UUID authToken, String gameName){
        this.authToken = authToken;
        this.gameName = gameName;
    }

    public UUID getAuthToken(){
        return authToken;
    }

    public String getGameName(){
        return gameName;
    }
}
