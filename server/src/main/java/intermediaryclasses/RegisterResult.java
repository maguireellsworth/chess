package intermediaryclasses;

import java.util.UUID;

public class RegisterResult extends Result {
    private String username;
    private UUID authToken;

    public RegisterResult(String message, String username, UUID authToken){
        super(message);
        this.username = username;
        this.authToken = authToken;

    }
}
