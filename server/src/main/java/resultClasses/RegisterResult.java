package resultClasses;

import java.util.UUID;

public class RegisterResult extends Result {
    private String username;
    private UUID authToken;

    public RegisterResult(String message, UUID authToken, String username){
        super(message);
        this.authToken = authToken;
        this.username = username;
    }
}
