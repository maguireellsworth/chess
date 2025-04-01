package websocket;



import com.google.gson.Gson;
import exception.ResponseException;
import models.AuthTokenModel;
import serverfacade.ServerFacade;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler){
        try{
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    var notification = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(notification);
                }
            });
        }catch(Exception e){

        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(String authToken, int gameID) throws Exception{
        try{
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        }catch(Exception e){
            throw new ResponseException(500, "Error: couldn't join game ws");
        }

    }
}
