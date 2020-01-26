package website;

import GoGame.GoBoard;

public class BoardBuilder {
    static public String buildBoard(GoBoard board) {
        StringBuilder board_builder = new StringBuilder();
        ResourceManager resource_manager = new ResourceManager();

        board_builder.append("<div id =\"board\">");

        for (int y = 0; y < board.size; y++) {
            board_builder.append("<div class = \"row\">");

            for (int x = 0; x < board.size; x++) {

                String board_button = resource_manager.getResource("board_button");
                board_button = board_button.replaceAll("XXX", Integer.toString(y));
                board_button = board_button.replaceAll("YYY", Integer.toString(x));

                if (board.getValue(x, y) == board.BLACK) {
                    board_button = board_button.replaceAll("STONE", "b");
                } else if (board.getValue(x, y) == board.WHITE) {
                    board_button = board_button.replaceAll("STONE", "w");
                } else {
                    board_button = board_button.replaceAll("STONE", "e");
                }

                board_builder.append(board_button);
            }

            board_builder.append("</div>\n");
        };
        board_builder.append("</div>");

        return board_builder.toString();
    }
}