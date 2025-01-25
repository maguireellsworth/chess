package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator extends pieceMoveCalculator{
    public RookMoveCalculator(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    public Collection<ChessMove> calculateMoves(){
//        int[][] distance = {};
        List<int[]> distance = new ArrayList<int[]>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for(int[] dir : directions){
            boolean blocked = false;
            ChessPosition currPosition = getPosition();
            do{
                ChessPosition nextPosition = getupdatedPosition(dir);
                blocked = isBlocked(nextPosition, dir);
                if(!blocked){
                    distance.add(new int[]{nextPosition.getRow(), nextPosition.getColumn()});
                }else{
                    //if try block fails then piece is blocked by wall
                    try{
                        ChessPiece nextPiece = getBoard().getPiece(nextPosition);
                        ChessPiece currPiece = getBoard().getPiece(currPosition);
                        if(nextPiece.getTeamColor() != currPiece.getTeamColor()){
                            distance.add(new int[]{nextPosition.getRow(), nextPosition.getColumn()});
                        }
                    }catch (Exception e){
                        assert true;
                    }
                }
            }while(!blocked);
        }
        return super.isMoveValid(distance.toArray(new int[0][]));
    }
}
