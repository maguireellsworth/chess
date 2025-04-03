package websocket.commands;

public class LeaveCommand extends UserGameCommand{
    String playerColor;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID, String playerColor){
        super(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }
    
    public String getPlayerColor(){
        return playerColor;
    }
}
