package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{
    public String game;

    public LoadGameMessage(String game){
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    @Override
    public String getMessage() {
        return game;
    }
}
