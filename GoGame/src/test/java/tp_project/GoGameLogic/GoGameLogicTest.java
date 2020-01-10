package tp_project.GoGameLogic;

import org.junit.Test;
import tp_project.GoGame.GoGame;
import tp_project.GoGame.GoMove;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GoGameLogicTest {
    @Test
    public void SampleGame() {
        GoMove m = new GoMove(GoMove.TYPE.MOVE);
        GoGameLogic.Player b = GoGameLogic.Player.BLACK;
        GoGameLogic.Player w = GoGameLogic.Player.WHITE;
        GoGameLogic game = new GoGameLogic(5);
        m.x = 1;
        m.y = 1;
        assertTrue(game.makeMove(m, b));
        assertFalse(game.makeMove(m, w));
        m.x = 2;
        assertFalse(game.makeMove(m, b));
        assertTrue(game.makeMove(m, w));
        m.x = 1; m.y = 3;
        assertTrue(game.makeMove(m, b));
        m.x = 2; m.y = 3;
        assertTrue(game.makeMove(m, w));
        m.x = 0; m.y = 2;
        assertTrue(game.makeMove(m, b));
        m.x = 3; m.y = 2;
        assertTrue(game.makeMove(m, w));
        m.x = 2; m.y = 2;
        assertTrue(game.makeMove(m, b));
        m.x = 1; m.y = 2;
        assertTrue(game.makeMove(m, w));
        m.x = 2; m.y = 2;
        assertFalse(game.makeMove(m, b));
        m.x = 0; m.y = 0;
        assertTrue(game.makeMove(m, b));
        m.x = 0; m.y = 1;
        assertFalse(game.makeMove(m, w));
        m.x = 3; m.y = 4;
        assertTrue(game.makeMove(m, w));
        m.x = 2; m.y = 2;
        assertTrue(game.makeMove(m, b));
        m.x = 4; m.y = 3;
        assertTrue(game.makeMove(m, w));
        m.x = 3; m.y = 3;
        assertFalse(game.makeMove(m, b));

        GoGameLogic.Cell E = GoGameLogic.Cell.EMPTY;
        GoGameLogic.Cell B = GoGameLogic.Cell.BLACK;
        GoGameLogic.Cell W = GoGameLogic.Cell.WHITE;

        GoGameLogic.Cell[][] expected = {
                {B, E, E, E, E},
                {E, B, W, E, E},
                {B, E, B, W, E},
                {E, B, W, E, W},
                {E, E, E, W, E}
        };

        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                assertTrue(expected[i][j].equals(game.getBoard()[i][j]));
            }
        }

        GoGameLogic.Score s = game.getGameScore(true);
        assertEquals(7, s.black);
        assertEquals(7, s.white);

        m.x = 1;
        m.y = 2;
        game.makeMove(m, b);
        GoGameLogic.Board board = new GoGameLogic.Board(5);
        board.setBoard(game.getBoard());
        assertEquals(4, board.getBreaths(1, 2));
    }
}
