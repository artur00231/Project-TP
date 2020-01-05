package tp_project.GoGameDBObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tp_project.GoGame.GoStatus;

@Entity(name = "status")
public class DBGoStatus {
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
}