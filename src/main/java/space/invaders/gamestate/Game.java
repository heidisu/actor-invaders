package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import space.invaders.dto.AlienDto;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.PlayerDto;

import java.util.ArrayList;
import java.util.List;

public class Game extends AbstractActor {
    private final ActorRef guiActor;
    private final int width = GameStateDto.screenSize.width;
    private final int height = GameStateDto.screenSize.height;
    private ActorRef player;
    private PlayerDto playerDto;
    private List<BulletDto> bullets = new ArrayList<>();
    private List<AlienDto> aliens = new ArrayList<>();

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    static public Props props(ActorRef guiActor) {
        return Props.create(Game.class, () -> new Game(guiActor));
    }

    public static class Tick {

    }

    public static class Start {

    }

    public static class Fire {

    }

    public static class MoveLeft {

    }

    public static class MoveRight {

    }

    private Game(ActorRef guiActor) {
        this.guiActor = guiActor;
    }



    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

}