package client;

import chess.ChessPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.ChessClient;
import websocket.NotificationHandler;
import websocket.messages.ServerMessage;

public class ChessClientTests {
    private static ChessClient client;

    @BeforeAll
    public static void init(){
        client = new ChessClient("", new NotificationHandler() {
            @Override
            public <T extends ServerMessage> void notify(T notification) {

            }
        });
    }

    @Test
    @DisplayName("Converts Chess Notation to Position")
    public void notationToPosition(){
        ChessPosition actualPosition = client.notationToPosition("a2");
        ChessPosition expectedPosition = new ChessPosition(2, 1);

        Assertions.assertEquals(actualPosition, expectedPosition);
    }
}
