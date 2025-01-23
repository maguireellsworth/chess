package chess;

import java.util.Collection;

public abstract class pieceMoveCalculator {
    private ChessBoard board;
    private ChessPosition position;

    public pieceMoveCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
    }

    public abstract Collection<ChessMove> calculateMoves();
}
