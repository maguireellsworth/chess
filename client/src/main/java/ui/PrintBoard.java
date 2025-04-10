package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import models.GameModel;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ui.EscapeSequences.*;

public class PrintBoard {
    private String playerColor;
    private GameModel game;

    public PrintBoard(GameModel game, String playerColor){
        this.playerColor = playerColor;
        this.game = game;
    }

    public void setPlayerColor(String playerColor){
        this.playerColor = playerColor;
    }

    public void setGame(GameModel gameModel){
        this.game = gameModel;
    }

    public void printBoard(List<ChessPosition> validMoves){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        String[] letters = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        String[] numbers = {" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 "};
        if(playerColor == null || playerColor.equals("WHITE")){
            printWhiteBoard(out, letters, numbers, game.getGame().getBoard(), validMoves);
        }else{
            printBlackBoard(out, letters, numbers, game.getGame().getBoard(), validMoves);
        }
    }

    public void printWhiteBoard(PrintStream out,
                                String[] letters,
                                String[] numbers,
                                ChessBoard board,
                                List<ChessPosition> validMoves){
        printLabel(out, letters);
        for(int i = 7; i >= 0; i--){
            printRowNumber(out, i, numbers);
            for(int j = 0; j <= 7; j++){
                printBoardRows(out, i, j, board, validMoves);
            }
            printRowNumber(out, i, numbers);
            reset(out);
            out.println();
        }
        printLabel(out, letters);
    }

    public void printBlackBoard(PrintStream out,
                                String[] letters,
                                String[] numbers,
                                ChessBoard board,
                                List<ChessPosition> validMoves){
        letters = reverseArray(letters);
        printLabel(out, letters);
        for(int i = 0; i <= 7; i++){
            printRowNumber(out, i, numbers);
            for(int j = 7; j >= 0; j--){
                printBoardRows(out, i, j, board, validMoves);
            }
            printRowNumber(out, i, numbers);
            reset(out);
            out.println();
        }
        printLabel(out, letters);
    }

    public String[] reverseArray(String[] letters){
        for (int i = 0; i < letters.length / 2; i++) {
            String t = letters[i];
            letters[i] = letters[letters.length - 1 - i];
            letters[letters.length - 1 - i] = t;
        }
        return letters;
    }


    public void printLabel(PrintStream out, String[] letters){
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_YELLOW);
        out.print("   ");
        for(int i = 0; i < 8; i++){
            out.print(letters[i]);
        }
        out.print("   ");
        reset(out);
        out.println();
    }

    public void printRowNumber(PrintStream out, int index, String[] numbers){
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_YELLOW);
        out.print(numbers[index]);
    }

    public void printBoardRows(PrintStream out, int i, int j, ChessBoard board, List<ChessPosition> validMoves){
        String spaceColor = null;
        validMoves = validMoves == null ? new ArrayList<>() : validMoves;
        if(i % 2 == 0){
            if(validMoves.contains(new ChessPosition(i + 1, j + 1))){
                spaceColor = (j % 2 == 0) ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
            }else{
                spaceColor = (j % 2 == 0) ? SET_BG_COLOR_BLUE : SET_BG_COLOR_RED;
            }
        }else{
            if(validMoves.contains(new ChessPosition(i + 1, j + 1))){
                spaceColor = (j % 2 == 0) ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
            }else{
                spaceColor = (j % 2 == 0) ? SET_BG_COLOR_RED : SET_BG_COLOR_BLUE;
            }
        }
        //TODO make sure list isnt empty
        if(!validMoves.isEmpty()  && validMoves.getFirst().equals(new ChessPosition(i + 1, j + 1))){
            spaceColor = SET_BG_COLOR_MAGENTA;
        }
        out.print(spaceColor);
        ChessPiece piece = board.getPiece(new ChessPosition(i + 1, j + 1));
        printPiece(piece, out);
    }

    public void printPiece(ChessPiece piece, PrintStream out){
        if(piece == null){
            out.print("   ");
            return;
        }
        ChessGame.TeamColor color = piece.getTeamColor();
        String printColor = (color == ChessGame.TeamColor.WHITE)? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        out.print(printColor);
        out.print(SET_TEXT_BOLD);
        switch(piece.getPieceType()){
            case PAWN -> out.print(" P ");
            case ROOK -> out.print(" R ");
            case KNIGHT -> out.print(" N ");
            case BISHOP -> out.print(" B ");
            case QUEEN -> out.print(" Q ");
            case KING -> out.print(" K ");
        }
        out.print(RESET_TEXT_BOLD_FAINT);
    }

    public void reset(PrintStream out){
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
    }
}
