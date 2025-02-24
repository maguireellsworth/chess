import chess.*;
import dataaccess.UserDao;
import server.Server;
import services.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        try{
            Server server = new Server();
            server.run(8080);
        }catch (Exception e){
            System.out.printf("Error: %s%n", e);
        }

    }
}