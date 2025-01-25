package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class pieceMoveCalculator {
    private ChessBoard board;
    private ChessPosition position;

    public pieceMoveCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
    }

    public ChessPosition getPosition(){
        return position;
    }

    public ChessPosition getupdatedPosition(ChessPosition currPosition, int[] direction){
        int newRow = currPosition.getRow() + direction[0];
        int newCol = currPosition.getColumn() + direction[1];
        return new ChessPosition(newRow, newCol);
    }

    public ChessBoard getBoard(){
        return board;
    }

    public abstract Collection<ChessMove> calculateMoves();

    public Collection<ChessMove> isMoveValid(int[][] ints){
        List<ChessMove> moves = new ArrayList<>();
        for(int[] item : ints){
            ChessPosition newPosition = getupdatedPosition(position, item);
            if(newPosition.getRow() < 1 || newPosition.getRow() > 8 || newPosition.getColumn() < 1 || newPosition.getColumn() > 8){
                continue;
            }
            ChessPiece piece = board.getPiece(newPosition);
            if(piece == null || piece.getTeamColor() != board.getPiece(position).getTeamColor()){
                ChessMove move = new ChessMove(getPosition(), newPosition, null);
                moves.add(move);
            }
        }
        return moves;
    }

    //return true if next position is unobtainable
    public boolean isBlocked(ChessPosition nextPosition){
        return isOutOfBounds(nextPosition) || board.getPiece(nextPosition) != null;
    }

    //return true if current position is out of bounds
    public boolean isOutOfBounds(ChessPosition position){
        return position.getRow() < 1 || position.getRow() > 8 || position.getColumn() < 1 || position.getColumn() > 8;
    }
}
