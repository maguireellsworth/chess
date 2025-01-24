package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMoveCalculator extends pieceMoveCalculator{
    public KingMoveCalculator(ChessBoard board, ChessPosition position){
        super(board, position);
    }



    public Collection<ChessMove> calculateMoves(){
        List<ChessMove> moves = new ArrayList<>();
        int[][] distance = {
                {1, -1}, {1, 0}, {1, 1},
                {0, -1}, {0, 1},
                {-1, -1}, {-1, 0}, {-1, 1}
        };
        for (int[] ints : distance) {
            ChessPosition newPosition = new ChessPosition(getPosition().getRow() + ints[0], getPosition().getColumn() + ints[1]);
            if (super.isMoveValid(newPosition)) {
                ChessMove move = new ChessMove(getPosition(), newPosition, null);
                moves.add(move);
            }
        }
        return moves;
    }
}
