package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMoveCalculator extends pieceMoveCalculator{
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
