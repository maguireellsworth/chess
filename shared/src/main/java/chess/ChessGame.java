package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor teamTurn;
    ChessBoard chessBoard;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if(piece == null){
            return null;
        }
        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(chessBoard, startPosition);
        for(ChessMove move : possibleMoves){
            ChessBoard testBoard = makeTestMove(move);
            if(!isInCheck(piece.getTeamColor(), testBoard)){
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public ChessBoard makeTestMove(ChessMove move){
        //make deep copy of board
        ChessBoard testBoard = chessBoard.makeDeepCopy();
        //move piece on testBoard
        ChessPiece piece = testBoard.getPiece(move.getStartPosition());
        testBoard.addPiece(move.getEndPosition(), piece);
        testBoard.addPiece(move.getStartPosition(), null);
        return testBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }


    public ChessPosition getKingPosition(ChessBoard board, TeamColor color){
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);
                if(piece == null){
                    continue;
                }else if(piece.getPieceType() == ChessPiece.PieceType.KING && color == piece.getTeamColor()){
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> enemyMoves;
        if(teamColor == TeamColor.WHITE){
            enemyMoves = chessBoard.getEnemyAttack(TeamColor.BLACK);
            ChessPosition whiteKingPos = getKingPosition(chessBoard, TeamColor.WHITE);
            for(ChessMove move: enemyMoves){
                if(move.getEndPosition().equals(whiteKingPos)){
                    return true;
                }
            }
        }else{
            enemyMoves = chessBoard.getEnemyAttack(TeamColor.WHITE);
            ChessPosition blackKingPos =  getKingPosition(chessBoard, TeamColor.BLACK);
            for(ChessMove move: enemyMoves){
                if(move.getEndPosition().equals(blackKingPos)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        Collection<ChessMove> enemyMoves;
        if(teamColor == TeamColor.WHITE){
            enemyMoves = board.getEnemyAttack(TeamColor.BLACK);
            ChessPosition whiteKingPos = getKingPosition(board, TeamColor.WHITE);
            for(ChessMove move: enemyMoves){
                if(move.getEndPosition().equals(whiteKingPos)){
                    return true;
                }
            }
        }else{
            enemyMoves = board.getEnemyAttack(TeamColor.WHITE);
            ChessPosition blackKingPos = getKingPosition(board, TeamColor.BLACK);
            for(ChessMove move: enemyMoves){
                ChessPosition moveEndPos = move.getEndPosition();
                if(move.getEndPosition().equals(blackKingPos)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
//        board.clearBoard();
//        for(int i = 0; i < 7; i++){
//            for(int j = 0; j < 7; j++){
//                ChessPosition position = new ChessPosition(j + 1, i + 1);
//                ChessPiece piece = board.getPiece(position);
//                if(piece.getPieceType() == ChessPiece.PieceType.KING)
//                chessBoard.addPiece(position, piece);
//            }
//        }
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
