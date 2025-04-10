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
    private PrintBoard print;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler){
        serverFacade = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        gameList = new HashMap<>();
        this.notificationHandler = notificationHandler;
        print = new PrintBoard(game, playerColor);
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
            print.setPlayerColor(playerColor);
            print.setGame(game);
            print.printBoard(null);
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
        }
        try{
            ChessPosition start = notationToPosition(params[0]);
            ChessPosition end = notationToPosition(params[1]);
            ChessPiece movedPiece = game.getGame().getBoard().getPiece(start);
            ChessPiece.PieceType promotionPiece = null;
            if((end.getRow() == 8 || end.getRow() == 1 )&& movedPiece.getPieceType() == ChessPiece.PieceType.PAWN){
                boolean loop = true;
                while(loop){
                    System.out.println(SET_TEXT_COLOR_GREEN + "Promote pawn to what piece type?\nOptions: rook, knight, bishop, queen");
                    Scanner scanner = new Scanner(System.in);
                    String promotionPieceString = scanner.nextLine();
                    ChessPiece.PieceType pieceType = getType(promotionPieceString);
                    if(pieceType != null){
                        loop = false;
                        promotionPiece = pieceType;
                    }else{
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid piece type" + RESET_TEXT_COLOR);
                    }
                }
            }
            ChessMove move = new ChessMove(start, end, promotionPiece);
            wsFacade.makeMove(authToken, game.getGameID(), move);
            return "";
        }catch(Exception e){
            throw new ResponseException(400, "Error: couldn't make move, Problem: " + e.getMessage());
        }
        
    }

    public ChessPiece.PieceType getType(String pieceType){
        return switch(pieceType){
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            default -> null;
        };
    }

    public String resign() throws ResponseException{
        if(!isLoggedIn()){
            return "Must be logged in to use command 'resign'\n" + help();
        }else if(!isInGame()){
            return "Must be in a game to use command 'resign'\n" + help();
        }else{
            try{
                System.out.println("Are you sure you want to resign?\nType yes to confirm\nType anything else to cancel");
                Scanner scanner = new Scanner(System.in);
                String confirm = scanner.nextLine();
                if(confirm.equals("yes")){
                    setGameIsOver(true);
                    wsFacade.resign(authToken, game.getGameID());
                    return "";
                }else{
                    return "Game will continue";
                }
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
            print.setPlayerColor(playerColor);
            print.setGame(game);
            print.printBoard(validPositions);
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
        print.setPlayerColor(playerColor);
        print.setGame(this.game);
        print.printBoard(null);
    }
}
