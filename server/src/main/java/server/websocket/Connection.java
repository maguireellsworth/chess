package server.websocket;

import models.AuthTokenModel;
import org.eclipse.jetty.websocket.api.Session;

public class Connection {
    public String authToken;
    public Session session;
    public int gameID;

    public Connection(String authToken, Session session, int gameID){
        this.authToken = authToken;
        this.session = session;
        this.gameID = gameID;
    }

    public void send(String msg) throws Exception{
        this.session.getRemote().sendString(msg);
    }
}
