package chess;

import java.util.Collection;
import java.util.List;

public class BishopMoveCalculator extends pieceMoveCalculator{
    public BishopMoveCalculator(ChessBoard board, ChessPosition position){
        super(board, position);
    }

    public Collection<ChessMove> calculateMoves(){
        int[][] directions = {
                {1, 1}, {-1, 1}, {-1, -1}, {1, -1}
        };
        List<int[]> distances = getPossibleDistances(directions);
        return super.isMoveValid(distances.toArray(new int[0][]));
    }
}
