package tp_project.GoGame;

import java.util.ArrayList;

import tp_project.Network.ICommand;

public class GoBoard implements ICommand {
    public int size = 0;
    private ArrayList<Integer> board = new ArrayList<>();
    public final int EMPTY = 0;
    public final int BLACK = 1;
    public final int WHITE = 2;

    public GoBoard(int size) {
        this.size = size;
        
        for (int i = 0; i < size * size; i++) {
            board.add(0);
        }
    }

    public void setValue(int x, int y, int val) {
        board.set(x + y * size, val);
    }

    public int getValue(int x, int y) {
        return board.get(x + y * size);
    }

    public Integer[] getAsArry() {
        return board.toArray(new Integer[]{});
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append(size).append(";");

        for (int i = 0; i < size * size; i++) {
            text.append(board.get(i)).append(";");
        }
        
        return text.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] raw_data = text.split(";");

        if (raw_data.length < 1) throw new IllegalArgumentException();

        try {
            size = Integer.parseInt(raw_data[0]);

            board.clear();

            for (int i = 0; i < size * size; i++) {
                board.add(Integer.parseInt(raw_data[1 + i]));
            }

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GoBoard";
    }

    
}