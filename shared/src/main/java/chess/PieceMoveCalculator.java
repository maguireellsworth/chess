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

    public ChessPiece getPiece(){
        return board.getPiece(position);
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

    public Collection<ChessMove> isMoveValid(int[][] distances){
        List<ChessMove> moves = new ArrayList<>();
        for(int[] distance : distances){
            ChessPosition newPosition = getupdatedPosition(position, distance);
            if(isOutOfBounds(newPosition)){
                continue;
            }
            ChessPiece piece = board.getPiece(newPosition);
            if(piece == null || piece.getTeamColor() != board.getPiece(position).getTeamColor()){
                if(board.getPiece(position).getPieceType() == ChessPiece.PieceType.PAWN && (newPosition.getRow() == 8 || newPosition.getRow() == 1)){
                    moves.add(new ChessMove(getPosition(), newPosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(getPosition(), newPosition, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(getPosition(), newPosition, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(getPosition(), newPosition, ChessPiece.PieceType.QUEEN));
                }else {
                    ChessMove move = new ChessMove(getPosition(), newPosition, null);
                    moves.add(move);
                }
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

    //get all possible distances for pieces with variable distances
    public List<int[]> getPossibleDistances(int[][] directions){
        List<int[]> distance = new ArrayList<>();
        for(int[] dir : directions) {
            boolean blocked = false;
            ChessPosition currPosition = getPosition();
            while (!blocked) {
                ChessPosition nextPosition = getupdatedPosition(currPosition, dir);
                blocked = isBlocked(nextPosition);
                //not blocked so save move and continue
                if (!blocked) {
                    distance.add(new int[]{(nextPosition.getRow() - getPosition().getRow()), (nextPosition.getColumn() - getPosition().getColumn())});
                    currPosition = nextPosition;
                //blocked by piece - if enemy
                } else if (!isOutOfBounds(nextPosition) && getBoard().getPiece(nextPosition) != null && getBoard().getPiece(nextPosition).getTeamColor() != getBoard().getPiece(getPosition()).getTeamColor()) {
                    distance.add(new int[]{(nextPosition.getRow() - getPosition().getRow()), (nextPosition.getColumn() - getPosition().getColumn())});
                    currPosition = nextPosition;
                //blocked by wall
                } else {
                    break;
                }
            }
        }
        return distance;
    }
}
