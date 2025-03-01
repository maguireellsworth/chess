package server;

import com.google.gson.Gson;
import dataaccess.GameDao;
import models.GameModel;
import org.eclipse.jetty.server.Authentication;
import resultClasses.ListResult;
import resultClasses.Result;
import services.*;
import spark.*;
import dataaccess.AuthTokenDao;
import dataaccess.UserDao;
import models.UserModel;
import models.AuthTokenModel;
import resultClasses.RegisterResult;

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
        this.userDao= new UserDao();
        this.authTokenDao = new AuthTokenDao();
        this.gameDao = new GameDao();
        this.userService = new UserService(userDao, authTokenDao);
        this.clearService = new ClearService(userDao, authTokenDao, gameDao);
        this.gameService = new GameService(gameDao);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
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
        }catch (InvalidUserDataException e){
            res.status(400);
            return gson.toJson(new Result(e.getMessage()));
        }catch (UserAlreadyExistsException e){
            res.status(403);
            return gson.toJson(new Result(e.getMessage()));
        }
    }

    public Object loginUser(Request req, Response res) throws Exception{
        Gson gson = new Gson();
        UserModel user =  gson.fromJson(req.body(), UserModel.class);
        try{
            AuthTokenModel authToken = userService.loginUser(user);
            return gson.toJson(new RegisterResult(null, authToken.getUsername(), authToken.getAuthToken()));
        }catch (InvalidUserDataException e){
            res.status(400);
            return gson.toJson(new Result(e.getMessage()));
        }catch (InvalidCredentialsException e){
            res.status(401);
            return gson.toJson(new Result(e.getMessage()));
        }
    }

    public Object logoutUser(Request req, Response res){
        Gson gson = new Gson();
        try{
            userService.logoutUser(UUID.fromString(req.headers("authorization")));
            return "";
        }catch (InvalidCredentialsException e){
            res.status(401);
            return gson.toJson(new Result(e.getMessage()));
        }
    }

    public Object listGames(Request req, Response res){
        Gson gson = new Gson();
        try{
            if(userService.validateUser(UUID.fromString(req.headers("authorization")))){
                List<GameModel> games = gameService.listGames();
                return gson.toJson(new ListResult(null, games));
            }else{
                res.status(400);
                return gson.toJson(new Result("Error: unauthorized"));
            }
        }catch (Exception e){
            return gson.toJson(new Result(e.getMessage()));
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
}
