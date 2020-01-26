package GoServer;

import java.util.NoSuchElementException;

import GoGame.GoClient;

public class GoClientFactory {
    static final String game_IP = "127.0.0.1";
    static final int port = 10045;

    private GoClientFactory() {}

    static public GoClient createCilent(String name) {
        try {
            GoClient client = GoClient.create(game_IP, port, name).get();

            return client;
        } catch (NoSuchElementException exception) {
            return null;
        }
    }
}