package GoGame;

import Network.ICommand;

public class GoStatus implements ICommand {
    public String player1 = "X";
    public String player2 = "X";
    
    public String curr_move = "X";
    public boolean player_1_giveup = false;
    public boolean player_2_giveup = false;
    public boolean game_ended = false;
    public String winner = "XX";
    public int player1_total_score = 0;
    public int player2_total_score = 0;
    public int stones_capured_by_player1;
    public int stones_capured_by_player2;

    @Override
    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append(player1).append(";");
        text.append(player2).append(";");
        text.append(curr_move).append(";");
        text.append(player_1_giveup).append(";");
        text.append(player_2_giveup).append(";");
        text.append(game_ended).append(";");
        text.append(winner).append(";");
        text.append(player1_total_score).append(";");
        text.append(player2_total_score).append(";");
        text.append(stones_capured_by_player1).append(";");
        text.append(stones_capured_by_player2).append(";");

        return text.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] raw_data = text.split(";");

        if (raw_data.length != 11) throw new IllegalArgumentException();

        try {
            player1 = raw_data[0];
            player2 = raw_data[1];
            curr_move = raw_data[2];

            player_1_giveup = Boolean.parseBoolean(raw_data[3]);
            player_2_giveup = Boolean.parseBoolean(raw_data[4]);
            game_ended = Boolean.parseBoolean(raw_data[5]);
            winner = raw_data[6];
            player1_total_score = Integer.parseInt(raw_data[7]);
            player2_total_score = Integer.parseInt(raw_data[8]);
            stones_capured_by_player1 = Integer.parseInt(raw_data[9]);
            stones_capured_by_player2 = Integer.parseInt(raw_data[10]);
        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GoStatus";
    }
}