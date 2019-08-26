package space.invaders.monitoring;

import akka.actor.AbstractActor;
import akka.actor.Props;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
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
    private final int rows = 4;
    private final int cols = 5;
    private GridElement[][] grid = new GridElement[rows][cols];
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

    private void insertGame(String name, GamePane gp) {
        GridElement gridElement = new GridElement(name, gp);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == null) {
                    grid[i][j] = gridElement;
                    root.add(gridElement.pane, j, i);
                    return;
                }
            }
        }

        String oldestGame = games.isEmpty() ? grid[0][0].name : games.get(0);
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                GridElement current = grid[i][j];
                if(oldestGame.equals(current.name)){
                    games.remove(current.name);
                    root.getChildren().remove(current.pane);
                    grid[i][j] = gridElement;
                    root.add(gridElement.pane, j, i);
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

    private static class GridElement {
        String name;
        Pane pane;

        private String getTitle(String name) {
            return name.replace("game-", "").toUpperCase();
        }

        private Pane createPane(GamePane game, String gameName) {
            Pane gamePane = game.getGamePane();
            Scale scale = new Scale();
            scale.setX(0.5);
            scale.setY(0.5);
            gamePane.getTransforms().add(scale);
            Label label = new Label();
            label.getStyleClass().add("player-label");
            label.setText(getTitle(gameName));
            label.setTextAlignment(TextAlignment.CENTER);
            label.setAlignment(Pos.TOP_CENTER);
            label.minWidthProperty().bind(Bindings.add(0, gamePane.widthProperty()));
            return new VBox(label, gamePane);
        }

        GridElement(String name, GamePane gamePane) {
            this.name = name;
            this.pane = createPane(gamePane, name);
        }
    }
}
