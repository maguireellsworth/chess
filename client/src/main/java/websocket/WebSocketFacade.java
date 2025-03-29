package websocket;



import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;

    public void WebSocketFacade(String url, NotificationHandler notificationHandler){
        try{
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
        }catch(Exception e){

        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
