package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] spaces = new ChessPiece[8][8];
    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        spaces[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return spaces[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] lineUp = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };
        //add white pieces
        for(int i = 0; i <= 7; i++){
            addPiece(new ChessPosition(1, i + 1), new ChessPiece(ChessGame.TeamColor.WHITE, lineUp[i]));
            addPiece((new ChessPosition(2, i + 1)), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
        //add black pieces
        for(int i = 0; i <= 7; i++){
            addPiece(new ChessPosition(8, i + 1), new ChessPiece(ChessGame.TeamColor.BLACK, lineUp[i]));
            addPiece((new ChessPosition(7, i + 1)), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }


    //create a blank chessboard
    public void clearBoard(){
        spaces = new ChessPiece[8][8];
    }

    public Collection<ChessMove> getEnemyAttack (ChessGame.TeamColor color){
        List<ChessMove> positions = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            for(int j = 0; i < 7; i++){
                ChessPiece piece = getPiece(new ChessPosition(j, i));
                if(piece == null){
                    continue;
                }else if(piece.getTeamColor() != color){
                    positions.addAll(piece.pieceMoves(this, new ChessPosition(j, i)));
                }
            }
        }
        return positions;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        for(int i = 0; i < spaces.length; i++){
            for(int j = 0; j < spaces[i].length; j++){
                ChessPiece myPiece = spaces[i][j];
                ChessPiece compPiece = that.spaces[i][j];
                if(myPiece == null && compPiece == null){
                    break;
                }
                if((myPiece == null && compPiece != null) || (myPiece != null && compPiece == null)){
                    return false;
                }
                if(myPiece.getPieceType() != compPiece.getPieceType()
                        || myPiece.getTeamColor() != compPiece.getTeamColor()){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(spaces);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "spaces=" + Arrays.toString(spaces) +
                '}';
    }
}
