package chess;

import java.util.Collection;

public abstract class pieceMoveCalculator {
    private ChessBoard board;
    private ChessPosition position;

    public pieceMoveCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
    }

    public ChessPosition getPosition(){
        return position;
    }

    public ChessBoard getBoard(){
        return board;
    }

    public abstract Collection<ChessMove> calculateMoves();

    public boolean isMoveValid(ChessPosition newPosition){
        //TODO given newPosition check if that space is valid(on the board) and open or occupied
        return true;
    }
}
