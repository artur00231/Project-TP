package website;

import javax.servlet.http.HttpServletRequest;

import GoGame.GoClient;
import GoServer.GoClientFactory;
import GoServer.GoServerClient;
public class ActionExecutor {
    public String err_message = "";
    
    public boolean execAction(HttpServletRequest request, GoServerClient client) {
        if (client.getGoClient() == null) {
            return execStartAction(request, client);
        }

        switch (client.getGoClient().getPosition()) {
            case SERVER:
                return execServerAction(request, client);
            case GAMESERVICE:
                return execServiceAction(request, client);
            default:
        }

        return false;
    }

    private boolean execStartAction(HttpServletRequest request, GoServerClient client) {
        err_message = "Invalid input";

        if (request.getParameter("name") == null && request.getParameter("type") == null)
            return true;

        if (request.getParameter("name") == null || request.getParameter("name").equals(""))
            return false;

        if (request.getParameter("type") == null || !request.getParameter("type").equals("play"))
            return false;

        err_message = "Server internal error";

        GoClient go_client = GoClientFactory.createCilent(request.getParameter("name"));
        if (go_client == null) return false;

        go_client.update();
        go_client.update(); //To be sure

        client.setGoClient(go_client);

        return true;
    }

    private boolean execServerAction(HttpServletRequest request, GoServerClient client) {
        err_message = "Invalid input";

        if (request.getParameter("exit") != null && request.getParameter("exit").equals("exit")) {
            client.getGoClient().exit();

            while (client.getGoClient().getPosition() == Server.Client.POSITION.SERVER) {
                client.getGoClient().update();
            }

            client.setGoClient(null);

            return true;
        }

        if (request.getParameter("create") != null && request.getParameter("create").equals("create")) {
            if (request.getParameter("size") != null) {
                int size = 0;
                try {
                    size = Integer.parseInt(request.getParameter("size"));
                    if (size < 9 || size > 19) throw new NumberFormatException();
                } catch (NumberFormatException exception) {
                    return false;
                }

                client.getGoClient().createGame();
                while (client.getGoClient().getPosition() == Server.Client.POSITION.SERVER) {
                    client.getGoClient().update();
                }

                client.getGoClient().setGameSize(size);
                client.getGoClient().update();
                client.getGoClient().update(); //To be sure

                return true;
            } else {
                return false;
            }
        }

        if (request.getParameter("connect") != null) {
            client.getGoClientAdapter().reset();
            client.getGoClient().connect(request.getParameter("connect"));
            
            while (!client.getGoClientAdapter().isPosChanged()) {
                client.getGoClient().update();

                if (client.getGoClient().getPosition() == Server.Client.POSITION.DISCONNECTED) {
                    return false;
                }
            }

            if (client.getGoClient().getPosition() != Server.Client.POSITION.GAMESERVICE) {
                err_message = "Cannot join";
                return false;
            }
        }
    
        return true;
    }

    private boolean execServiceAction(HttpServletRequest request, GoServerClient client) {
        err_message = "Server internal error";

        if (request.getParameter("exit") != null && request.getParameter("exit").equals("exit")) {
            client.getGoClient().exit();

            while (client.getGoClient().getPosition() == Server.Client.POSITION.GAMESERVICE) {
                client.getGoClient().update();
            }

            return true;
        }

        if (request.getParameter("kick") != null) {
            client.getGoClientAdapter().reset();

            client.getGoClient().kick(request.getParameter("kick"));
            while (!client.getGoClientAdapter().stateChanged()) {
                client.getGoClient().update();

                if (client.getGoClient().getPosition() == Server.Client.POSITION.DISCONNECTED) {
                    return false;
                }
            }

            return true;
        }

        if (request.getParameter("ready") != null) {
            boolean ready = Boolean.parseBoolean(request.getParameter("ready"));

            client.getGoClientAdapter().reset();
            client.getGoClient().setReady(ready);

            while (!client.getGoClientAdapter().stateChanged()) {
                client.getGoClient().update();
    
                if (client.getGoClient().getPosition() == Server.Client.POSITION.DISCONNECTED) {
                    return false;
                }
            }

            return true;
        }

        if (request.getParameter("bot") != null && request.getParameter("bot").equals("bot")) {
            client.getGoClientAdapter().reset();
            client.getGoClient().addBot();

            while (!client.getGoClientAdapter().stateChanged()) {
                client.getGoClient().update();
    
                if (client.getGoClient().getPosition() == Server.Client.POSITION.DISCONNECTED) {
                    return false;
                }
            }

            if (client.getGoClientAdapter().isError()) {
                err_message = "Bot cannot be added";

                return false;
            }

            return true;
        }
    
        return true;
    }
}