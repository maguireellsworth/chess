package ui;

import chess.*;
import exception.ResponseException;
import intermediaryclasses.*;
import models.AuthTokenModel;
import models.GameModel;
import models.UserModel;
import serverfacade.ServerFacade;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;
import websocket.messages.LoadGameMessage;

import static ui.EscapeSequences.*;


import java.io.PrintStream;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChessClient {
    private String username = "Logged out";
    private String authToken = null;
    private ServerFacade serverFacade;
    private String serverUrl;
    private HashMap<Integer, GameModel> gameList;
    private GameModel game = null;
    private String playerColor = null;
    private WebSocketFacade wsFacade;
    private NotificationHandler notificationHandler;
    private boolean gameIsOver;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler){
        serverFacade = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        gameList = new HashMap<>();
        this.notificationHandler = notificationHandler;
    }

    public String getUsername(){
        return username;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "draw" -> draw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "highlight" -> highlight(params);
                case "quit" -> quit();
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String register(String... params) throws ResponseException{
        if(params.length != 3){
            return "Incorrect number of parameters. 'register' command requires parameters: <username> <password> <email>";
        }else{
            UserModel userModel = new UserModel(params[0], params[1], params[2]);
            try{
                RegisterResult result = serverFacade.registerUser(userModel);
                username = result.getUsername();
                authToken = result.getAuthToken();
                return "Successfully Registered!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Register");
            }
        }
    }

    private String login(String... params) throws ResponseException{
        if(params.length != 2){
            return "Incorrect number of parameters. 'login' command requires parameters: <username> <password>";
        }else if(isLoggedIn()){
            return "Already logged in. Valid commands:\n" + help();
        }else{
            UserModel user = new UserModel(params[0], params[1], null);
            try{
                RegisterResult result = serverFacade.loginUser(user);
                username = result.getUsername();
                authToken = result.getAuthToken();
                return "Successfully Logged In!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Login, Incorrect Username of Password");
            }
        }
    }

    private boolean isLoggedIn(){
        return authToken != null;
    }

    private String logout() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to run command 'logout'\n" + help();
        }else if(isInGame()){
            return "Currently in a game. Leave current game to use command 'logout'\n" + help();
        }else{
            try{
                serverFacade.logoutUser(authToken);
                username = "Logged out";
                authToken = null;
                return "Successfully Logged Out!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't Logout");
            }
        }
    }

    private String create(String... params) throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to run command 'create'\n" + help();
        }else if(params.length != 1){
            return "Incorrect number of parameters. 'create' command requires parameters: <gamename>";
        }else if(isInGame()){
            return "Currently in a game. Leave current game to use command 'create'\n " + help();
        }else{
            CreateRequest request = new CreateRequest(authToken,params[0]);
            try {
                CreateResult createResult = serverFacade.createGame(request);
                return "Successfully Created Game!";
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't create game");
            }
        }
    }

    private String list() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to run command 'list'\n" + help();
        }else if(isInGame()){
            return "Currently in a game. Leave current game to use command 'list'\n" + help();
        }else{
            try {
                gameList = new HashMap<>();
                List<GameModel> games = serverFacade.listGames(authToken).getGames();
                String format = "%d) GameName: %s, WhiteUsername: %s, BlackUsername: %s\n";
                StringBuilder returnString = new StringBuilder();
                for (int i = 0; i < games.size(); i++) {
                    returnString.append(String.format(
                            format,
                            i + 1,
                            games.get(i).getGameName(),
                            games.get(i).getWhiteUsername(),
                            games.get(i).getBlackUsername()
                            )
                    );
                    gameList.put(i + 1, games.get(i));
                }
                return returnString.toString();
            }catch(ResponseException e){
                throw new ResponseException(400, "Error: Couldn't list games");
            }
        }
    }

    private String join(String... params) throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'join'\n" + help();
        }else if(isInGame()){
            return "Currently in a game. Leave current game to use command 'join'\n" + help();
        }else if(params.length != 2){
            return "Incorrect number of parameters. 'join' command requires parameters: <id> <WHITE or BLACK>";
        }else{
            try {
                //server
                game = gameList.get(Integer.parseInt(params[0]));
                playerColor = params[1].toUpperCase();
                gameIsOver = false;
                JoinRequest joinRequest = new JoinRequest(params[1].toUpperCase(), game.getGameID());
                joinRequest.setAuthTokenModel(new AuthTokenModel(username, authToken));
                serverFacade.joinGame(joinRequest);

                //websocket
                wsFacade = new WebSocketFacade(serverUrl, notificationHandler);
                wsFacade.joinGame(authToken,  game.getGameID(), playerColor);

//                printBoard();
//                return "Successfully Joined Game!";
                return "";
            }catch (Exception e){
                game = null;
                playerColor = null;
                throw new ResponseException(400, "Error: Couldn't join game");
            }
        }
    }

    private String observe(String... params) throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'observe'\n" + help();
        }else if(params.length != 1){
            return "Incorrect number of parameters. 'observe' command requires parameters: <id>";
        }else{
            try {
                playerColor = null;
                game = gameList.get(Integer.parseInt(params[0]));

                //websocket
                wsFacade = new WebSocketFacade(serverUrl, notificationHandler);
                wsFacade.observeGame(authToken, game.getGameID());
                return "";
            }catch (Exception e){
                throw new ResponseException(400, "Error: Couldn't join game");
            }
        }
    }

    private String quit(){
        return (authToken == null)? "quitting" : "Must logout before quitting";
    }

    private String draw(){
        if(!isLoggedIn()){
            return "Must be logged in to use command 'draw'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'draw'\n" + help();
        }else{
            System.out.println(game.getGame().getTeamTurn() + "'s turn");
            printBoard(null);
            return "";
        }

    }

    private String leave() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'leave'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'leave'\n" + help();
        }else{
            try{
                wsFacade.leaveGame(authToken, game.getGameID());
                game = null;
                playerColor = null;
                return "";
            }catch(Exception e){
                throw new ResponseException(400, "Error: couldn't leave game, Problem: " + e.getMessage());
            }
        }
    }

    private String move(String... params) throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'move'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'move'\n" + help();
        }else if(params.length != 2){
            return  "Incorrect number of parameters. 'move' command requires parameters: <from> <to>";
        }else{
            try{
                ChessPosition start = notationToPosition(params[0]);
                ChessPosition end = notationToPosition(params[1]);
                ChessMove move = new ChessMove(start, end, null);
                wsFacade.makeMove(authToken, game.getGameID(), move);
                return "";
            }catch(Exception e){
                throw new ResponseException(400, "Error: couldn't make move, Problem: " + e.getMessage());
            }
        }
    }

    public String resign() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'resign'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'resign'\n" + help();
        }else{
            try{
                setGameIsOver(true);
                wsFacade.resign(authToken, game.getGameID());
                return "";
            }catch(Exception e){
                throw new ResponseException(500, "Error: couldn't resign");
            }

        }
    }
    public String highlight(String... params){
        if(!isLoggedIn()){
            return "Must be logged in to use command 'highlight'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'highlight'\n" + help();
        }else if(params.length != 1){
            return "Incorrect number of parameters. 'highlight' command requires parameters: <position>";
        }else{
            ChessPosition position = notationToPosition(params[0]);
            Collection<ChessMove> validMoves = game.getGame().validMoves(position);
            List<ChessPosition> validPositions = new ArrayList<>();
            validPositions.add(position);
            for(ChessMove move : validMoves){
                validPositions.add(move.getEndPosition());
            }
            printBoard(validPositions);
            return "";
        }
    }

    public boolean isInGame(){
        return game != null;
    }

    public String help(){
        String prelogin = """
                Options:
                - help
                - quit
                - login <username> <password>
                - register <username> <password> <email>
                """;
        String postLogin = """
                Options:
                - help
                - logout
                - create <gameName>
                - list
                - join <id> <WHITE or BLACK>
                - observe <id>
                """;
        String inGame = """
                Options:
                - help
                - draw
                - leave
                - move <from> <to>
                - resign
                - highlight <position>
                """;
        String gameOver = """
                Options;
                - help
                - draw
                - leave
                - highlight <position>
                """;
        if(isLoggedIn()){
            if(isInGame()){
                if(gameIsOver){
                    return gameOver;
                }else {
                    return inGame;
                }
            }else{
                return postLogin;
            }
        }else{
            return prelogin;
        }
    }

    public void setGameIsOver(boolean bool){
        gameIsOver = bool;
    }

    public ChessPosition notationToPosition(String space){
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String colString = space.substring(0, 1);
        int col = -1;
        for(int i = 0; i < letters.length; i++){
            if(letters[i].equals(colString)){
                col = i + 1;
                break;
            }
        }
        int row = Integer.parseInt(space.substring(1));

        return new ChessPosition(row, col);
    }

    public void updateGame(ChessGame game){
        this.game.setGame(game);
        printBoard(null);
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
