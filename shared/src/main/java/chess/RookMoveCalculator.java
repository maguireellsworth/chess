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
                //not blocked so save move and continue
                if(!blocked){
                    distance.add(new int[]{(nextPosition.getRow() - getPosition().getRow()), (nextPosition.getColumn() - getPosition().getColumn())});
                    currPosition = nextPosition;
                //blocked by piece - if enemy
                }else if(!isOutOfBounds(nextPosition) && getBoard().getPiece(nextPosition) != null && getBoard().getPiece(nextPosition).getTeamColor() != getBoard().getPiece(getPosition()).getTeamColor()){
                    distance.add(new int[]{(nextPosition.getRow() - getPosition().getRow()), (nextPosition.getColumn() - getPosition().getColumn())});
                    currPosition = nextPosition;
                }else{
                    break;
                }
            }
        }

        return super.isMoveValid(distance.toArray(new int[0][]));
    }
}
