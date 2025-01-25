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

    public ChessBoard getBoard(){
        return board;
    }

    public abstract Collection<ChessMove> calculateMoves();

    public Collection<ChessMove> isMoveValid(int[][] ints){
        List<ChessMove> moves = new ArrayList<>();
        for(int[] item : ints){
            int newRow = getPosition().getRow() + item[0];
            int newCol = getPosition().getColumn() + item[1];
            if(newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8){
                continue;
            }
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece piece = board.getPiece(newPosition);
            if(piece == null || piece.getTeamColor() != board.getPiece(position).getTeamColor()){
                ChessMove move = new ChessMove(getPosition(), newPosition, null);
                moves.add(move);
            }
        }
        return moves;
    }
}
