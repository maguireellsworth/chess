package websocket;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import models.AuthTokenModel;
import serverfacade.ServerFacade;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException{
        try{
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                    String type = jsonObject.get("serverMessageType").getAsString(); // Extract message type

                    ServerMessage notification = switch (type) {
                        case "LOAD_GAME" -> new Gson().fromJson(jsonObject, LoadGameMessage.class);
                        case "NOTIFICATION" -> new Gson().fromJson(jsonObject, NotificationMessage.class);
                        case "ERROR" -> new Gson().fromJson(jsonObject, ErrorMessage.class);
                        default -> new ErrorMessage("Couldn't parse message");
                    };
                    notificationHandler.notify(notification);
                }
            });
        }catch(Exception e){
            throw new ResponseException(500, "Error: Couldn't establish WebSocket Connection");
        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception{
        try{
            ConnectCommand command = new ConnectCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, playerColor);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }catch(Exception e){
            throw new ResponseException(500, "Error: couldn't join game ws");
        }

    }
}
