package website;

import GoServer.GoServerClient;

import GoGame.GoBoard;
import GoGame.GoStatus;

public class GameViewBuilder implements IViewBuilder {
    private String err_msg = "";
    private boolean add_err = false;

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

        GoBoard board = null;
        GoStatus status = null;

        client.getGoPlayerAdapter().reset();
        client.getGoPlayer().getGameBoard();
        while (!client.getGoPlayerAdapter().is_board) {
            if (!client.getGoPlayer().update()) {
                addErrorMessage("Server internal error");
                break;
            }
        }

        if (client.getGoPlayerAdapter().is_board) {
            board = (GoBoard) client.getGoPlayerAdapter().last_board;
        }

        client.getGoPlayerAdapter().reset();
        client.getGoPlayer().getGameStatus();
        while (!client.getGoPlayerAdapter().is_status) {
            if (!client.getGoPlayer().update()) {
                addErrorMessage("Server internal error");
                break;
            }
        }

        if (client.getGoPlayerAdapter().is_status) {
            status = (GoStatus) client.getGoPlayerAdapter().last_status;
        }

        if (board != null && status != null) {
            site.append(BoardBuilder.buildBoard(board));
            site.append("\n");
        }

        if (add_err) {
            site.append("<script>\n");
            site.append("function err_foo() {\n");
            site.append("window.alert(\"" + err_msg + "\");\n");
            site.append("}\n");
            site.append("window.onload=err_foo;\n");
            site.append("</script>\n");
        }

        site.append(manager.getResource("pass_button"));
        site.append(manager.getResource("give_up_button"));
        site.append("\n</body>\n</html>");

        return site.toString();
    }

    @Override
    public boolean autoRefresh() {
        return true;
    }
}