package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType piecetype;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.piecetype = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return piecetype;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch(this.piecetype){
            case KING:
                return new KingMoveCalculator(board, myPosition).calculateMoves();
            case KNIGHT:
                return new KnightMoveCalculator(board, myPosition).calculateMoves();
            case ROOK:
                return new RookMoveCalculator(board, myPosition).calculateMoves();
            case BISHOP:
                return new BishopMoveCalculator(board, myPosition).calculateMoves();
            case QUEEN:
                return new QueenMoveCalculator(board,myPosition).calculateMoves();
            case PAWN:
                return new PawnMoveCalculator(board, myPosition).calculateMoves();
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && piecetype == that.piecetype;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, piecetype);
    }
    
    @Override
    public String toString() {
        if (piecetype == null) {
            return "O"; // Represents an uninitialized or empty piece
        }

        char symbol = (piecetype == PieceType.KNIGHT) ? 'N' : piecetype.name().charAt(0);

        // Convert case based on color
        return (pieceColor == ChessGame.TeamColor.WHITE) ? String.valueOf(symbol) : String.valueOf(Character.toLowerCase(symbol));
    }
}
