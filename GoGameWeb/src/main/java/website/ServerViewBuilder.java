package website;

import GoServer.GoServerClient;
import Server.GameServiceInfo;
import Server.GameServicesInfo;
import Server.Client.POSITION;

public class ServerViewBuilder implements IViewBuilder {
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

        client.getGoClientAdapter().reset();
        client.getGoClientAdapter().clearCommands();
        client.getGoClient().getGameServicesInfo();
        while (!client.getGoClientAdapter().isRecived()) {
            client.getGoClient().update();
            if (client.getGoClient().getPosition() != POSITION.SERVER) {
                addErrorMessage("Server internal error");
                break;
            }
        }

        if (client.getGoClientAdapter().isRecived()) {
            GameServicesInfo info = (GameServicesInfo) client.getGoClientAdapter().popCommand().command;

            for (GameServiceInfo service_info : info.game_services) {
                if (service_info.max_players == service_info.players.size()) {
                    continue;
                }

                String game_service = manager.getResource("game_service");
                game_service = game_service.replaceAll("host_name", service_info.players.get(service_info.host_id));
                game_service = game_service.replaceAll("service_id", service_info.ID);

                site.append(game_service);
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
        site.append(manager.getResource("create_button"));
        site.append(manager.getResource("refresh_button"));
        site.append("\n</body>\n</html><style>");
        site.append(manager.getResource("style"));
        site.append("</style>");

        return site.toString();
    }

    @Override
    public boolean autoRefresh() {
        return false;
    }

    @Override
    public int autoRefreshTime() {
        return 9999;
    }

    @Override
    public void addInfoMessage(String message) {
        add_info = true;
        info_msg = message;
    }
}