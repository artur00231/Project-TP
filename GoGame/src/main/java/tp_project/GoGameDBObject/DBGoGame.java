package tp_project.GoGameDBObject;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity(name = "game")
public class DBGoGame {
    @Column(name = "player1_name")
    private String player1_name = "X";
    @Column(name = "player2_name")
    private String player2_name = "X";
    @Column(name = "beginning_player")
    private boolean beginning_player;
    @Column(name = "ended")
    private boolean ended = false;
    @Column(name = "time")
    private Timestamp game_date;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public DBGoGame(String player1_name, String player2_name, boolean begin_player1) {
        this.player1_name = player1_name;
        this.player2_name = player2_name;
        beginning_player = begin_player1;
        game_date = Timestamp.from(Instant.now());
    }

    public void setGameEnded(boolean ended) {
        this.ended = ended;
    }

    public String getPlayer1Name() {
        return player1_name;
    }

    public String getPlayer2Name() {
        return player2_name;
    }

    public Timestamp getGameDate() {
        return game_date;
    }

    public boolean isGameEnded() {
        return ended;
    }

    public int getID() {
        return id;
    }
}