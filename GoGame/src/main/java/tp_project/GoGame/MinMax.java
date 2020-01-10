package tp_project.GoGame;

import tp_project.GoGameLogic.GoGameLogic;

import java.util.ArrayList;
import java.util.Random;

public class MinMax {
    Random rand = new Random();

    GoMove getMove(GoGameLogic.Board b) {
        ArrayList<GoMove> m = new ArrayList<>();
        m.add(new GoMove(GoMove.TYPE.PASS));
        int min = bestScore(b.pass(), 2);
        System.out.println("Pass: " + min);

        for (int i = 0; i < b.getSize(); ++i) {
            for (int j = 0; j < b.getSize(); ++j) {
                GoMove curr_m = new GoMove(GoMove.TYPE.MOVE);
                curr_m.x = j;
                curr_m.y = i;
                GoGameLogic.Board next = b.makeMove(curr_m, b.getCurrent_player());
                if (next == null) {System.out.print(" -  "); continue;}
                int curr = bestScore(next,2);
                System.out.print(String.format("%3d ", curr));
                if (curr < min) {
                    min = curr;
                    m.clear();
                    m.add(curr_m);
                } else if (curr == min) m.add(curr_m);
            }
            System.out.println();
        }
        System.out.println("Min: " + min);
        System.out.println(m.size());
        return m.get(rand.nextInt(m.size()));
    }

    int bestScore(GoGameLogic.Board b, int depth) {
        GoGameLogic.Player p = b.getCurrent_player();
        GoGameLogic.Score s = b.getScore(true);
        if (depth == 0) {
            int max = (p.equals(GoGameLogic.Player.WHITE) ? s.white - s.black : s.black - s.white);
            return max;
        }else {
            int min = bestScore(b.pass(), depth - 1);
            for(int i = 0; i < b.getSize(); ++i) {
                for (int j = 0; j < b.getSize(); ++j) {
                    GoMove m = new GoMove(GoMove.TYPE.MOVE);
                    m.x = i;
                    m.y = j;
                    GoGameLogic.Board next = b.makeMove(m, p);
                    if (next != null) {
                        int next_score = bestScore(next, depth - 1);
                        if (next_score < min) min = next_score;
                    }
                }
            }
            return -min;
        }
    }
}