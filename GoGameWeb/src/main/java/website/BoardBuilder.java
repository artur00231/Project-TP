package website;

import GoGame.GoBoard;

public class BoardBuilder {
    static public String buildBoard(GoBoard board) {
        StringBuilder board_builder = new StringBuilder();
        ResourceManager resource_manager = new ResourceManager();

        board_builder.append("<table style=\"width:100%\">\n");

        for (int y = 0; y < board.size; y++) {
            board_builder.append("<tr>\n");

            for (int x = 0; x < board.size; x++) {
                board_builder.append("<th>\n");

                String board_button = resource_manager.getResource("board_button");
                board_button = board_button.replaceAll("XXX", Integer.toString(y));
                board_button = board_button.replaceAll("YYY", Integer.toString(x));

                if (board.getValue(x, y) == board.BLACK) {
                    board_button = board_button.replaceAll("STONE", "B");
                } else if (board.getValue(x, y) == board.WHITE) {
                    board_button = board_button.replaceAll("STONE", "W");
                } else {
                    board_button = board_button.replaceAll("STONE", "&nbsp;");
                }

                board_builder.append(board_button);
                board_builder.append("</th>\n");
            }

            board_builder.append("</tr>\n");
        };

        return board_builder.toString();
    }
}