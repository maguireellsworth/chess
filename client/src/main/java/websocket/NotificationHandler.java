package websocket;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    <T extends ServerMessage> void notify(T notification);
}
