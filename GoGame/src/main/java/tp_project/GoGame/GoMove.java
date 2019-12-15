package tp_project.GoGame;

import tp_project.Network.ICommand;

public class GoMove implements ICommand {
    public enum TYPE { MOVE, PASS, GIVEUP };
    public TYPE move_type;
    public int x = 0, y = 0;

    public GoMove(TYPE type) {
        move_type = type;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append(move_type.toString()).append(";");
        text.append(x).append(";");
        text.append(y).append(";");

        return text.toString();
    }

    @Override
    public void fromText(String text) throws IllegalArgumentException {
        String[] raw_data = text.split(";");

        if (raw_data.length != 3) throw new IllegalArgumentException();

        try {
            move_type = TYPE.valueOf(raw_data[0]);

            x = Integer.parseInt(raw_data[1]);
            y = Integer.parseInt(raw_data[2]);

        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getCommandType() {
        return "GoMove";
    }
    
}