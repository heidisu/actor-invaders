package space.invaders.monitoring;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import space.invaders.dto.GameStateDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMonitor extends AbstractActor {
    private GridPane root;
    private Map<String, GamePane> gameStates = new HashMap<>();
    private String[][] grid = new String[5][4];
    private List<String> games = new ArrayList<>();

    static public Props props() {
        return Props.create(GameMonitor.class, GameMonitor::new);
    }

    private GameMonitor() {
        initialize();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GameStateDto.class, gs -> {
                    String name = getContext().getSender().path().name();
                    gameStates.computeIfAbsent(name, nm -> new GamePane(gs));
                    GamePane game = gameStates.get(name);
                    if (game.isGameOver()) {
                        games.remove(name);
                        gameStates.put(name, new GamePane(gs));
                    } else {
                        gameStates.get(name).updateState(gs);
                    }
                    update();
                })
                .build();
    }

    private String getTitle(String name) {
        return name.replace("game-", "");
    }

    private Pane getGameWithTitle(GamePane game, String gameName) {
        Pane gamePane = game.getGamePane();
        VBox pane = new VBox();
        pane.setMaxSize(100.0, 100.0);
        pane.setMinSize(100.0, 100.0);
        Scale scale = new Scale();
        scale.setX(0.5);
        scale.setY(0.5);
        gamePane.getTransforms().add(scale);
        Label label = new Label();
        label.getStyleClass().add("player-label");
        label.setText(getTitle(gameName));
        label.setAlignment(Pos.TOP_CENTER);
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(label);
        pane.getChildren().add(gamePane);
        return pane;
    }


    private void insertGame(String name, GamePane gp) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == null) {
                    grid[i][j] = name;
                    root.add(getGameWithTitle(gp, name), j, i);
                    return;
                }
            }
        }
    }

    private void update() {
        gameStates.forEach((key, value) -> {
            if (!games.contains(key)) {
                insertGame(key, value);
                games.add(key);
            }
        });
    }

    private ColumnConstraints create20PercentCol() {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(20);
        return col;
    }

    private RowConstraints create25percentRow() {
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(25);
        return row;
    }

    private void initialize() {
        Stage stage = new Stage();
        stage.setTitle("Space invaders");
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> System.exit(0));

        root = new GridPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setVgap(10);
        root.setHgap(10);

        root.getColumnConstraints()
                .addAll(
                        create20PercentCol(),
                        create20PercentCol(),
                        create20PercentCol(),
                        create20PercentCol(),
                        create20PercentCol());
        root.getRowConstraints()
                .addAll(create25percentRow(), create25percentRow(), create25percentRow(), create25percentRow());

        Scene scene = new Scene(root, 1550, 950);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.setResizable(true);
        root.applyCss();
        root.getChildren().add(new Label());
        stage.show();
    }
}
