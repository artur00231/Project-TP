package website;

import GoServer.GoServerClient;

import GoGame.GoBoard;
import GoGame.GoStatus;

public class GameViewBuilder implements IViewBuilder {
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

        String score = manager.getResource("score");

        if (client.getGoPlayerAdapter().is_status) {
            status = (GoStatus) client.getGoPlayerAdapter().last_status;
        }

        if (board != null && status != null) {
            

            if (!status.curr_move.equals(client.getGoClient().getID())) {
                score = score.replaceAll("MOVE", "&nbsp;");
            } else {
                score = score.replaceAll("MOVE", "Your move");
            }

            score = score.replaceAll("SCORE", getScore(status, client.getGoClient().getID()));

            site.append(score);
            site.append(BoardBuilder.buildBoard(board));
            site.append("\n");
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

        site.append(manager.getResource("pass_button"));
        site.append(manager.getResource("give_up_button"));
        site.append("\n</body>\n</html>");

        site.append("<style>");
        site.append(manager.getResource("style"));
        site.append("</style><script>");
        site.append(manager.getResource("js"));
        site.append("</script>");

        return site.toString();
    }

    @Override
    public boolean autoRefresh() {
        return true;
    }

    @Override
    public int autoRefreshTime() {
        return 1;
    }

    @Override
    public void addInfoMessage(String message) {
        add_info = true;
        info_msg = message;
    }

    private String getScore(GoStatus status, String player_ID) {
        String my_score;
        String my_points;
        String opponent_score;
        String opponent_points;
        if (status.player1.equals(player_ID)) {
            my_score = Integer.toString(status.player1_total_score);
            opponent_score = Integer.toString(status.player2_total_score);
            my_points = Integer.toString(status.stones_capured_by_player1);
            opponent_points = Integer.toString(status.stones_capured_by_player2);
            int score_length = my_score.length() > opponent_score.length() ? my_score.length() : opponent_score.length();
            int points_length = my_points.length() > opponent_points.length() ? my_points.length() : opponent_points.length();
            score_length = score_length > 1 ? score_length : 2;
            points_length = points_length > 1 ? points_length : 2;

            my_score = String.format("%0" + score_length + "d", status.player1_total_score);
            opponent_score = String.format("%0" + score_length + "d", status.player2_total_score);
            my_points = String.format("%0" + points_length + "d", status.stones_capured_by_player1);
            opponent_points = String.format("%0" + points_length + "d", status.stones_capured_by_player2);
        } else {
            my_score = Integer.toString(status.player2_total_score);
            opponent_score = Integer.toString(status.player1_total_score);
            my_points = Integer.toString(status.stones_capured_by_player2);
            opponent_points = Integer.toString(status.stones_capured_by_player1);
            int score_length = my_score.length() > opponent_score.length() ? my_score.length() : opponent_score.length();
            int points_length = my_points.length() > opponent_points.length() ? my_points.length() : opponent_points.length();
            score_length = score_length > 1 ? score_length : 2;
            points_length = points_length > 1 ? points_length : 2;

            my_score = String.format("%0" + score_length + "d", status.player2_total_score);
            opponent_score = String.format("%0" + score_length + "d", status.player1_total_score);
            my_points = String.format("%0" + points_length + "d", status.stones_capured_by_player2);
            opponent_points = String.format("%0" + points_length + "d", status.stones_capured_by_player1);
        }

        return "Score<br>" + my_points + "-" + opponent_points + "<br>End score<br>" + my_score + "-" + opponent_score;
    }
}