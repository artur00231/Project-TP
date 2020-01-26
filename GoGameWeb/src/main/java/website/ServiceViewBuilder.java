package website;

import GoServer.GoServerClient;
import Server.GameServiceInfo;

import java.util.Map.Entry;

import GoGame.GoGameServiceInfo;
import Server.Client.POSITION;

public class ServiceViewBuilder implements IViewBuilder {
    private String err_msg = "";
    private boolean add_err = false;
    private String info_msg = "";
    private boolean add_info = false;

    @Override
    public void addErrorMessage(String message) {
        add_err = true;
        err_msg = message;
    }

    @Override
    public String buildWebsite(GoServerClient client) {
        StringBuilder site = new StringBuilder();
        ResourceManager manager = new ResourceManager();

        site.append("<!DOCTYPE html>\n<html>\n<head>\n<title>GoGame</title>\n</head>\n<body>\n");

        GameServiceInfo service_info = null;
        GoGameServiceInfo go_service_info = null;

        client.getGoClientAdapter().reset();
        client.getGoClientAdapter().clearCommands();
        client.getGoClient().getGameServiceInfo();
        while (!client.getGoClientAdapter().isRecived()) {
            client.getGoClient().update();
            if (client.getGoClient().getPosition() != POSITION.GAMESERVICE) {
                addErrorMessage("Server internal error");
                break;
            }
        }

        if (client.getGoClientAdapter().isRecived()) {
            service_info = (GameServiceInfo) client.getGoClientAdapter().popCommand().command;
        }

        client.getGoClientAdapter().reset();
        client.getGoClientAdapter().clearCommands();
        client.getGoClient().getGoGameServiceInfo();
        while (!client.getGoClientAdapter().isRecived()) {
            client.getGoClient().update();
            if (client.getGoClient().getPosition() != POSITION.GAMESERVICE) {
                addErrorMessage("Server internal error");
                break;
            }
        }

        if (client.getGoClientAdapter().isRecived()) {
            go_service_info = (GoGameServiceInfo) client.getGoClientAdapter().popCommand().command;
        }

        String ready_button = manager.getResource("ready_button");
        String add_bot_button = manager.getResource("add_bot_button");

        if (service_info != null && go_service_info != null) {
            for (Entry<String, String> player : service_info.players.entrySet()) {
                String game_service = manager.getResource("player_info");

                game_service = game_service.replaceAll("name", player.getValue());
                if (!go_service_info.getPlayersInfo().stream().filter(x -> x.ID.equals(player.getKey())).findAny().get().ready) {
                    game_service = game_service.replaceAll("checked", "");
                }

                if (service_info.host_id.equals(client.getGoClient().getID())) {
                    if (player.getKey().equals(client.getGoClient().getID())) {
                        game_service = game_service.replaceAll("kick_button", "");
                    } else {
                        String kick_button = manager.getResource("kick_button");
                        kick_button = kick_button.replaceAll("player_id", player.getKey());
                        game_service = game_service.replaceAll("kick_button", kick_button);
                    }

                } else {
                    game_service = game_service.replaceAll("kick_button", "");
                }
                
                site.append(game_service);
            }
            
            if (!go_service_info.getPlayersInfo().stream().filter(x -> x.ID.equals(client.getGoClient().getID())).findAny().get().ready) {
                ready_button = ready_button.replaceAll("ready_state", "true");
            } else {
                ready_button = ready_button.replaceAll("ready_state", "false");
            }

            if (!service_info.host_id.equals(client.getGoClient().getID())) {
                add_bot_button = "";
            }
        }

        if (add_err || add_info) {
            site.append("<script>\n");
            site.append("function msg_foo() {\n");
            if (add_err)
                site.append("window.alert(\"" + err_msg + "\");\n");
            if (add_info)
                site.append("window.alert(\"" + info_msg + "\");\n");
            site.append("}\n");
            site.append("window.onload=msg_foo;\n");
            site.append("</script>\n");
        }

        site.append(manager.getResource("exit_button"));
        site.append(ready_button);
        site.append(add_bot_button);
        site.append("\n</body>\n</html><style>");
        site.append(manager.getResource("style"));
        site.append("</style>");

        return site.toString();
    }

    @Override
    public boolean autoRefresh() {
        return true;
    }

    @Override
    public int autoRefreshTime() {
        return 2;
    }

    @Override
    public void addInfoMessage(String message) {
        add_info = true;
        info_msg = message;
    }
}