package chess;

import java.util.Collection;

public class KingMoveCalculator extends PieceMoveCalculator {
    public KingMoveCalculator(ChessBoard board, ChessPosition position){
        super(board, position);
    }



    public Collection<ChessMove> calculateMoves(){
        int[][] distance = {
                {1, -1}, {1, 0}, {1, 1},
                {0, -1}, {0, 1},
                {-1, -1}, {-1, 0}, {-1, 1}
        };
        return super.isMoveValid(distance);
    }
}
