package tp_project.GoGameDBObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tp_project.GoGame.GoStatus;
import tp_project.Network.ICommand;

@Entity(name = "status")
public class DBGoStatus implements ICommand {
    @Column(name = "player_1_giveup")
    public boolean player_1_giveup = false;
    @Column(name = "player_2_giveup")
    public boolean player_2_giveup = false;

    @Column(name = "winner")
    public boolean winner;
    @Column(name = "player1_total_score")
    public int player1_total_score;
    @Column(name = "player2_total_score")
    public int player2_total_score;
    @Column(name = "stones_capured_by_player1")
    public int stones_capured_by_player1;
    @Column(name = "stones_capured_by_player2")
    public int stones_capured_by_player2;
    @Column(name = "game_id")
    public int game_id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    public DBGoStatus() {
    }

    public DBGoStatus(GoStatus status, boolean player1_win) {
        player_1_giveup = status.player_1_giveup;
        player_2_giveup = status.player_2_giveup;
        player1_total_score = status.player1_total_score;
        player2_total_score = status.player2_total_score;
        stones_capured_by_player1 = status.stones_capured_by_player1;
        stones_capured_by_player2 = status.stones_capured_by_player2;
        winner = player1_win;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toText() {
        StringBuilder builder = new StringBuilder().append(id).append(";").append(game_id).append(";");
        builder.append(player_1_giveup).append(";").append(player_2_giveup).append(";");
        builder.append(player1_total_score).append(";").append(player2_total_score).append(";");
        builder.append(stones_capured_by_player1).append(";").append(stones_capured_by_player2).append(";");
        builder.append(winner).append(";");

        return builder.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] raw_data = text.split(";");

        if (raw_data.length != 9) throw new IllegalArgumentException();

        try {
            id = Integer.parseInt(raw_data[0]);
            game_id = Integer.parseInt(raw_data[1]);
            
            player_1_giveup = Boolean.parseBoolean(raw_data[2]);
            player_2_giveup = Boolean.parseBoolean(raw_data[3]);

            player1_total_score = Integer.parseInt(raw_data[4]);
            player2_total_score = Integer.parseInt(raw_data[5]);
            stones_capured_by_player1 = Integer.parseInt(raw_data[6]);
            stones_capured_by_player2 = Integer.parseInt(raw_data[7]);

            winner = Boolean.parseBoolean(raw_data[8]);
            

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "DBGoStatus";
    }
}