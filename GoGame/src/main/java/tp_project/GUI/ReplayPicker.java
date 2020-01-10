package tp_project.GUI;

import javax.swing.*;

import tp_project.GoGameDBObject.DBGoGame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Vector;

public class ReplayPicker {
    public enum Action {
        SELECT,
        GET,
        RETURN
    }

    private ActionListener action_listener;
    private JTextField date_input;
    private JComboBox<String> games_info;
    private ArrayList<DBGoGame> games;
    private JPanel main_panel;
    private JButton select;

    public ReplayPicker() {
        games = new ArrayList<>();

        main_panel = new JPanel();

        setupView();
    }

    public void setActionListener(ActionListener action_listener) {
        this.action_listener = action_listener;
    }

    public Optional<DBGoGame> getValue() {
        return Optional.ofNullable(games.get(games_info.getSelectedIndex()));
    }

    public JPanel getView() {
        return main_panel;

    }

    public void setAvaiableGames(ArrayList<DBGoGame> games) {
        this.games = games;

        updateView();
    }

    private void updateView() {
        Vector<String> games_info_raw = new Vector<>();

        for (DBGoGame game : games) {
            Date date = new Date(game.getGameDate().getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String formattedDate = sdf.format(date);
            StringBuilder info = new StringBuilder(formattedDate);
            info.append(" | ");
            info.append(game.getPlayer1Name());
            info.append(" vs ");
            info.append(game.getPlayer2Name());

            games_info_raw.add(info.toString());
        }

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>( games_info_raw );
        games_info.setModel( model );

        if (games_info_raw.size() == 0) {
            select.setEnabled(false);
        } else {
            select.setEnabled(true);
        }
    }

    private void setupView() {
        select = new JButton("Select");
        select.addActionListener(
            e -> action_listener.actionPerformed(new ActionEvent(Action.SELECT, 0, null)));
        select.setEnabled(false);
        JButton exit = new JButton("Exit");
        exit.addActionListener(
            e -> action_listener.actionPerformed(new ActionEvent(Action.RETURN, 0, null)));
        JButton check_date = new JButton("Set");
        check_date.addActionListener(
            e -> setDate());

        games_info = new JComboBox<>();

        //Curr date
        Date date = new Date(Instant.now().toEpochMilli());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);

        date_input = new JTextField(formattedDate);
        updateView();

        JPanel center_panel = new JPanel();
        center_panel.setLayout(new GridLayout(0, 1));
        center_panel.add(date_input);
        center_panel.add(check_date);
        center_panel.add(games_info);

        main_panel.setLayout(new BorderLayout());
        main_panel.add(select, BorderLayout.PAGE_START);
        main_panel.add(exit, BorderLayout.PAGE_END);
        main_panel.add(center_panel, BorderLayout.CENTER);
    }

    private void setDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse(date_input.getText());

            action_listener.actionPerformed(new ActionEvent(Action.GET, 0, date_input.getText()));
        } catch (ParseException exception) {
            JOptionPane.showMessageDialog(null, "Invalid date");
        }
    }
}
