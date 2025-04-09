package ui;

import com.google.gson.Gson;
import websocket.NotificationHandler;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.PrintStream;
import java.util.Scanner;
import static ui.EscapeSequences.*;

//import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_RED + WHITE_QUEEN +
                SET_TEXT_COLOR_YELLOW + "Welcome to Chess! Sign in to start." + RESET_TEXT_COLOR +
                SET_TEXT_COLOR_RED + WHITE_KING);
        System.out.print(SET_TEXT_COLOR_BLUE + client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quitting")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + SET_TEXT_COLOR_YELLOW + client.getUsername() +  RESET_TEXT_COLOR + " >>> " + SET_TEXT_COLOR_GREEN);
    }

    @Override
    public <T extends ServerMessage> void notify(T message) {
        if(message instanceof NotificationMessage){
            System.out.println(SET_TEXT_COLOR_YELLOW + message.getMessage());
            printPrompt();
        }else if(message instanceof LoadGameMessage){
            System.out.println("It made it here!");
            client.updateGame(((LoadGameMessage) message).getGame());
            printPrompt();
        }else if(message instanceof ErrorMessage){
            System.out.println(SET_TEXT_COLOR_RED + message.getMessage());
            reset();
            printPrompt();
        }
    }

    public void reset(){
        System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

}
