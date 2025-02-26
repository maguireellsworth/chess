package server;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import resultClasses.Result;
import services.*;
import spark.*;
import dataaccess.AuthTokenDao;
import dataaccess.UserDao;
import models.UserModel;
import models.AuthTokenModel;
import resultClasses.RegisterResult;

import java.util.UUID;

public class Server {
    private UserDao userDao;
    private AuthTokenDao authTokenDao;
    private UserService userService;
    private ClearService clearService;

    public Server(){
        this.userDao= new UserDao();
        this.authTokenDao = new AuthTokenDao();
        this.userService = new UserService(userDao, authTokenDao);
        this.clearService = new ClearService(userDao, authTokenDao);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
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
