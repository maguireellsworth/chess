package intermediaryclasses;

import models.AuthTokenModel;

public class JoinRequest {
    private AuthTokenModel authModel;
    private String playerColor;
    private int gameID;

    public JoinRequest(String playerColor, int gameID){
        this.authModel = null;
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public void setAuthTokenModel(AuthTokenModel authToken){
        this.authModel = authToken;
    }

    public AuthTokenModel getAuthTokenModel() {
        return authModel;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }
}
