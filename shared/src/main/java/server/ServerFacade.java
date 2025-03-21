package server;

import com.google.gson.Gson;
import exception.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import intermediaryclasses.CreateRequest;
import intermediaryclasses.JoinRequest;
import models.AuthTokenModel;
import models.GameModel;
import models.UserModel;
import intermediaryclasses.RegisterResult;

public class ServerFacade {
    private String serverUrl;

    public ServerFacade(String serverUrl){
        this.serverUrl = serverUrl;
    }

    public RegisterResult registerUser(UserModel userModel) throws ResponseException{
        String path = "/user";
        return this.makeRequest("POST", path, userModel, RegisterResult.class, null);
    }

    public RegisterResult loginUser(UserModel userModel) throws ResponseException{
        String path = "/session";
        return this.makeRequest("POST", path, userModel, RegisterResult.class, null);
    }

    public void logoutUser(String authToken) throws ResponseException{
        String path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    public GameModel[] listGames(String authToken)throws ResponseException{
        var path = "/game";
        record listGamesResponse(GameModel[] gameModels) {
        }
        var response = this.makeRequest("GET", path, null, listGamesResponse.class, authToken);
        return response.gameModels();
    }

    public int createGame(CreateRequest createRequest, String authToken) throws ResponseException{
        String path = "/game";
        return this.makeRequest("POST", path, createRequest, null, authToken);
    }

    public void joinGame(JoinRequest joinRequest, String authToken) throws ResponseException{
        String path = "/game";
        this.makeRequest("PUT", path, joinRequest, null, authToken);
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if(authToken != null){
                http.setRequestProperty("authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
