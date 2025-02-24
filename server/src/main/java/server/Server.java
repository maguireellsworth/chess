package server;

import com.google.gson.Gson;
import dataaccess.UserDao;
import models.UserModel;
import org.eclipse.jetty.server.Authentication;
import services.UserService;
import spark.*;

public class Server {
    private UserDao userDao;
    private UserService userService;

    public Server(){
        this.userDao= new UserDao();
        this.userService = new UserService(userDao);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);


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
        userService.registerUser(user);
        return new Gson().toJson(res);
    }
}
