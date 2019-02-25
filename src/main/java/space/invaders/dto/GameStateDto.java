package space.invaders.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class GameStateDto implements Serializable {
    public static final ScreenSize screenSize = new ScreenSize(600, 400);
    public static final TicksPerSecond speed = new TicksPerSecond(20);

    public final State state;
    public final PlayerDto player;
    public final List<BulletDto> bullets;
    public final List<AlienDto> aliens;

    public enum State {
        Playing,
        GameLost,
        GameWon
    }

    public GameStateDto(State state, PlayerDto player, List<BulletDto> bullets, List<AlienDto> aliens) {
        this.state = state;
        this.player = player;
        this.bullets = Collections.unmodifiableList(bullets);
        this.aliens = Collections.unmodifiableList(aliens);
    }

    public static class ScreenSize implements Serializable {
        public final int width;
        public final int height;

        ScreenSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static class TicksPerSecond implements  Serializable {
        public final int value;

        TicksPerSecond(int value) {
            this.value = value;
        }
    }
}

