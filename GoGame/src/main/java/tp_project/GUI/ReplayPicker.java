package tp_project.GUI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ReplayPicker extends JFXPanel {
    public enum Action {
        SELECT,
        RETURN
    }

    ActionListener action_listener;

    public ReplayPicker(ActionListener a) {
        action_listener = a;

        DatePicker date_picker = new DatePicker();

        ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList("game 1", "game 2"));

        GridPane grid = new GridPane();
        grid.add(new Label("Date: "), 0, 0);
        grid.add(date_picker, 1, 0);

        grid.add(new Label("Game"), 0, 1);
        grid.add(combo_box, 1, 1);

        grid.add(new Button("SELECT"), 0, 2);
        grid.add(new Button("RETURN"), 1, 2);

        Platform.runLater(() -> {
            this.setScene(new Scene(grid));
        });
    }
}
