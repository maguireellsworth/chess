package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMoveCalculator extends PieceMoveCalculator {
    public KnightMoveCalculator(ChessBoard board, ChessPosition position){
        super(board, position);
    }

    public Collection<ChessMove> calculateMoves(){
        List<ChessMove> moves = new ArrayList<>();
        int[][] distance = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };
        return super.isMoveValid(distance);
    }
}
