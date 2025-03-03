package IntermediaryClasses;

import models.AuthTokenModel;

public class JoinRequest {
    private AuthTokenModel authToken;
    private String playerColor;
    private int gameID;

    public JoinRequest(String playerColor, int gameID){
        this.authToken = null;
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public void setAuthTokenModel(AuthTokenModel authToken){
        this.authToken = authToken;
    }

    public AuthTokenModel getAuthTokenModel() {
        return authToken;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }
}
