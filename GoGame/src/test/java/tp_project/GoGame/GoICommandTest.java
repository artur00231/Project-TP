package tp_project.GoGame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tp_project.Network.CommandFactory;

public class GoICommandTest {
    @Test
    public void gostatus_test1() {
        GoStatus status = new GoStatus();
        assertEquals("X;X;X;false;false;false;XX;", status.toText());

        status.player1 = "AA";
        status.player2 = "BB";
        status.curr_move = "BB";
        status.player_1_giveup = true;
        status.player_2_giveup = false;
        status.game_ended = false;
        status.winner = "AA";

        assertEquals("AA;BB;BB;true;false;false;AA;", status.toText());

        status.fromText("C;D;E;false;true;false;BB;");
        assertEquals("C;D;E;false;true;false;BB;", status.toText());

        assertTrue(CommandFactory.isValidType(status.getCommandType()));
    }

    @Test
    public void gomove_test1() {
        GoMove move = new GoMove(GoMove.TYPE.PASS);
        assertEquals("PASS;0;0;", move.toText());
        move = new GoMove(GoMove.TYPE.GIVEUP);
        assertEquals("GIVEUP;0;0;", move.toText());
        move = new GoMove(GoMove.TYPE.MOVE);
        assertEquals("MOVE;0;0;", move.toText());
        
        move.setXY(15, 40);
        assertEquals("MOVE;15;40;", move.toText());

        assertTrue(CommandFactory.isValidType(move.getCommandType()));
    }

    @Test
    public void gomove_test2() {
        GoMove move = new GoMove(GoMove.TYPE.PASS);
        move.fromText("PASS;0;0;");
        assertEquals("PASS;0;0;", move.toText());
        move.fromText("GIVEUP;0;0;");
        assertEquals("GIVEUP;0;0;", move.toText());
        move.fromText("MOVE;7;13;");
        assertEquals("MOVE;7;13;", move.toText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void gomove_test3() {
        GoMove move = new GoMove(GoMove.TYPE.PASS);
        move.fromText("MOVE;dsa;aad;");
    }

    @Test
    public void goboard_test1() {
        GoBoard board = new GoBoard(3);
        assertEquals("3;0;0;0;0;0;0;0;0;0;", board.toText());

        board.setValue(2, 2, 2);
        board.setValue(0, 1, 1);
        board.setValue(0, 0, 3);
        assertEquals("3;3;0;0;1;0;0;0;0;2;", board.toText());
        assertEquals(1, board.getValue(0, 1));

        assertTrue(CommandFactory.isValidType(board.getCommandType()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void goboard_test2() {
        GoBoard board = new GoBoard(3);
        board.fromText("3;0;0;");
    }

    @Test(expected = IllegalArgumentException.class)
    public void goboard_test3() {
        GoBoard board = new GoBoard(3);
        board.fromText("2;0;dsa;0;0;");
    }
}