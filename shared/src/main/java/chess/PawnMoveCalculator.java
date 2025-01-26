package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMoveCalculator extends pieceMoveCalculator{
    public PawnMoveCalculator(ChessBoard board, ChessPosition position){
        super(board, position);
    }

    public List<int[]> getDistances(ChessGame.TeamColor color, int[][] directions, ChessPiece piece){
        List<int[]> distances = new ArrayList<>();
        boolean hasMoved = false;
        //check if first move
        if(color == ChessGame.TeamColor.WHITE && getPosition().getRow() != 2){
            hasMoved = true;
        }else if(color == ChessGame.TeamColor.BLACK && getPosition().getRow() != 7){
            hasMoved = true;
        }
        //first move
        if(!hasMoved
                && !isBlocked(getupdatedPosition(getPosition(), directions[0]))
                && !isBlocked(getupdatedPosition(getPosition(), directions[1]))){
            distances.add(directions[1]);
        }
        //default move forward
        if(!isBlocked(getupdatedPosition(getPosition(), directions[0]))){
            distances.add(directions[0]);
        }
        //attacking
        if(isBlocked(getupdatedPosition(getPosition(), directions[2]))
                || isBlocked(getupdatedPosition(getPosition(), directions[3]))){
            if(getBoard().getPiece(getupdatedPosition(getPosition(), directions[2])).getTeamColor() != color){
                distances.add(directions[2]);
            }
            if(getBoard().getPiece(getupdatedPosition(getPosition(), directions[3])).getTeamColor() != color){
                distances.add(directions[3]);
            }
        }
        return distances;
    }

    public Collection<ChessMove> calculateMoves(){
        List<int[]> distances = new ArrayList<>();
        ChessPiece piece = getPiece();
        int[][] whiteDirections = {{1, 0}, {2, 0}, {1, -1}, {1, 1}};
        int[][] blackDirections = {{-1, 0}, {-2, 0}, {-1, -1}, {-1, 1}};
        if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
            distances = getDistances(piece.getTeamColor(), whiteDirections, piece);
        }else if(piece.getTeamColor() == ChessGame.TeamColor.BLACK){
            distances = getDistances(piece.getTeamColor(), blackDirections, piece);
        }
        return super.isMoveValid(distances.toArray(new int[0][]));
    }
}
