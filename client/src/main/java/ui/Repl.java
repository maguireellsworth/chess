package ui;

import websocket.NotificationHandler;
import websocket.messages.ServerMessage;

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
    public void notify(ServerMessage notification) {
        System.out.println(SET_TEXT_COLOR_RED + "white has joined as WHITE");
        printPrompt();
    }
}
