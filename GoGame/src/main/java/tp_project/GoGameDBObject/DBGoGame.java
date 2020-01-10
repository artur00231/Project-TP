package tp_project.GoGameDBObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tp_project.Network.ICommand;


@Entity(name = "game")
public class DBGoGame implements ICommand {
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

    public DBGoGame() {

    }
    
    public DBGoGame(String player1_name, String player2_name, boolean begin_player1) {
        this.player1_name = player1_name;
        this.player2_name = player2_name;
        beginning_player = begin_player1;

        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
        ZonedDateTime gmt = zdt.withZoneSameInstant(ZoneId.of("GMT"));
        game_date = Timestamp.valueOf(gmt.toLocalDateTime());
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

    @Override
    public String toText() {
        StringBuilder builder = new StringBuilder().append(id).append(";");
        builder.append(player1_name).append(";").append(player2_name).append(";");
        builder.append(beginning_player).append(";").append(ended).append(";");
        
        builder.append(game_date.getTime()).append(";");

        return builder.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] raw_data = text.split(";");

        if (raw_data.length != 6) throw new IllegalArgumentException();

        try {
            id = Integer.parseInt(raw_data[0]);
            player1_name = raw_data[1];
            player2_name = raw_data[2];
            beginning_player = Boolean.parseBoolean(raw_data[3]);
            ended = Boolean.parseBoolean(raw_data[4]);
            long time = Long.parseLong(raw_data[5]);
            game_date = new Timestamp(time);

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "DBGoGame";
    }
}