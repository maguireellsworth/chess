package intermediaryclasses;

public class RegisterResult extends Result {
    private String username;
    private String authToken;

    public RegisterResult(String message, String username, String authToken){
        super(message);
        this.username = username;
        this.authToken = authToken;

    }
}
