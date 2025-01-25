package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator extends pieceMoveCalculator{
    public RookMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    public Collection<ChessMove> calculateMoves(){
        List<int[]> distance = new ArrayList<>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for(int[] dir : directions){
            boolean blocked = false;
            ChessPosition currPosition = getPosition();
            while(!blocked){
                ChessPosition nextPosition = getupdatedPosition(currPosition, dir);
                blocked = isBlocked(nextPosition);
                if(!blocked){
                    distance.add(new int[]{nextPosition.getRow(), nextPosition.getColumn()});
                    currPosition = nextPosition;
                }else if(!isOutOfBounds(nextPosition) && getBoard().getPiece(nextPosition).getTeamColor() != getBoard().getPiece(currPosition).getTeamColor()){
                        distance.add(new int[]{nextPosition.getRow(), nextPosition.getColumn()});
                        currPosition = nextPosition;
                }else{
                    break;
                }
            }
        }

        return super.isMoveValid(distance.toArray(new int[0][]));
    }
}
