package tp_project.GoGameDBObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tp_project.GoGame.GoBoard;

@Entity(name = "board")
public class DBGoBoard {
    @Column(name = "board")
    public String row_board;
    @Column(name = "round_number")
    public int round_number;
    @Column(name = "game_id")
    public int game_id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public DBGoBoard() {}

    public DBGoBoard createNext(GoBoard next_board) {
        DBGoBoard next_DBGoBoard = new DBGoBoard();
        next_DBGoBoard.game_id = game_id;
        next_DBGoBoard.row_board = next_board.toText();
        next_DBGoBoard.round_number = round_number + 1;

        return next_DBGoBoard;
    }

    public int getID() {
        return id;
    }
}