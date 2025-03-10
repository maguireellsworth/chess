package server;

import dataaccess.*;
import intermediaryclasses.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import models.GameModel;
import services.*;
import spark.*;
import models.UserModel;
import models.AuthTokenModel;

import java.util.List;
import java.util.UUID;

public class Server {
    private UserDao userDao;
    private AuthTokenDao authTokenDao;
    private GameDao gameDao;
    private UserService userService;
    private ClearService clearService;
    private GameService gameService;

    public Server(){
        try{
            this.userDao= new MYSQLUserDao();
            this.authTokenDao = new AuthTokenDao();
            this.gameDao = new GameDao();
            this.userService = new UserService(userDao, authTokenDao);
            this.clearService = new ClearService(userDao, authTokenDao, gameDao);
            this.gameService = new GameService(gameDao, userService);
        }catch (Exception e){
            System.out.println("Database could not be initialized");
        }

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDB);


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public Object registerUser(Request req, Response res) throws Exception{
        Gson gson = new Gson();
        UserModel user = gson.fromJson(req.body(), UserModel.class);
        try{
            AuthTokenModel authToken = userService.registerUser(user);
            return gson.toJson(new RegisterResult(null, authToken.getUsername(), authToken.getAuthToken()));
        }catch (Exception e) {
            return exceptionHandler(e, res);
        }
    }

    public Object loginUser(Request req, Response res) throws Exception{
        Gson gson = new Gson();
        UserModel user =  gson.fromJson(req.body(), UserModel.class);
        try{
            AuthTokenModel authToken = userService.loginUser(user);
            return gson.toJson(new RegisterResult(null, authToken.getUsername(), authToken.getAuthToken()));
        }catch (Exception e) {
            return exceptionHandler(e, res);
        }
    }

    public Object logoutUser(Request req, Response res){
        Gson gson = new Gson();
        try{
            String uuidString = getValidUUIDString(req);
            userService.logoutUser(UUID.fromString(uuidString));
            return "";
        }catch (Exception e){
            return exceptionHandler(e, res);
        }
    }

    public Object listGames(Request req, Response res){
        Gson gson = new Gson();
        try{
            String uuidString = getValidUUIDString(req);
            List<GameModel> games = gameService.listGames(UUID.fromString(uuidString));
            return gson.toJson(new ListResult(null, games));
        }catch (Exception e){
            return exceptionHandler(e, res);
        }
    }

    public Object createGame(Request req, Response res) throws Exception{
        Gson gson = new Gson();
        try{
            String uuidString = getValidUUIDString(req);
            UUID authToken = UUID.fromString(uuidString);
            CreateRequest request = new CreateRequest(authToken);
            String gamename= gson.fromJson(req.body(), JsonObject.class).get("gameName").getAsString();
            request.setGameName(gamename);
            CreateResult gameID = gameService.createGame(request);
            return gson.toJson(gameID);
        }catch (Exception e){
            return exceptionHandler(e, res);
        }
    }

    public Object joinGame(Request req, Response res){
        Gson gson = new Gson();
        try{
            String uuidString = getValidUUIDString(req);
            UUID authToken = UUID.fromString(uuidString);
            AuthTokenModel authTokenModel = userService.getAuthTokenModel(authToken);
            JoinRequest request = gson.fromJson(req.body(), JoinRequest.class);
            request.setAuthTokenModel(authTokenModel);
            gameService.joinGame(request);
            return "";
        }catch (Exception e) {
            return exceptionHandler(e, res);
        }
    }

    public Object clearDB(Request req, Response res){
        Gson gson = new Gson();
        try{
            clearService.clearDB();
            return "";
        }catch (Exception e){
            res.status(400);
            return gson.toJson(new Result(e.getMessage()));
        }
    }

    public Object exceptionHandler(Exception e, Response res){
        Gson gson = new Gson();
        String type = e.getClass().getSimpleName();
        switch (type) {
            case "InvalidUserDataException" -> res.status(400);
            case "InvalidCredentialsException" -> res.status(401);
            case "UserAlreadyExistsException" -> res.status(403);
            default -> res.status(500);
        }
        return gson.toJson(new Result(e.getMessage()));
    }

    public String getValidUUIDString(Request req){
        String uuid = req.headers("authorization");
        if(uuid.length() != 36){
            throw new InvalidCredentialsException("Error: Unauthorized");
        }
        return uuid;
    }
}
