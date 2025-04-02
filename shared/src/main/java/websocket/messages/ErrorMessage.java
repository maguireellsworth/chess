package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private String errorMessage;

    public ErrorMessage(String message){
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
