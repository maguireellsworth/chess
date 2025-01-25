package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator extends pieceMoveCalculator{
    public RookMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    public Collection<ChessMove> calculateMoves(){
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        List<int[]> distance = getPossibleDistances(directions);
        return super.isMoveValid(distance.toArray(new int[0][]));
    }
}
