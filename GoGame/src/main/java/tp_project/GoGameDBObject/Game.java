package tp_project.GoGameDBObject;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "gra")
public class Game {
    @Column(name = "player1_name")
    private String player1_name = "X";
    @Column(name = "player2_name")
    private String player2_name = "X";
    @Column(name = "ended")
    private boolean ended = false;
    @Column(name = "time")
    private Date game_date;

    @Id
    private int id;

    public Game(String player1_name, String player2_name) {
        this.player1_name = player1_name;
        this.player2_name = player2_name;
        game_date = Date.valueOf(LocalDate.now());
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

    public Date getGameDate() {
        return game_date;
    }

    public boolean isGameEnded() {
        return ended;
    }
}