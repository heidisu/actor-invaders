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
    private ActorRef bulletManager;
    private ActorRef alienManager;
    private ActorRef player;
    private PlayerDto playerDto;
    private List<BulletDto> bullets = new ArrayList<>();
    private List<AlienDto> aliens = new ArrayList<>();

    public static class Bullets {
        public final List<BulletDto> bullets;

        public Bullets(List<BulletDto> bullets) {
            this.bullets = bullets;
        }
    }

    public static class Aliens {
        public final List<AlienDto> aliens;

        public Aliens(List<AlienDto> aliens) {
            this.aliens = aliens;
        }
    }

    private Receive idle() {
        return receiveBuilder()
                .match(Start.class, s -> {
                    player = getContext().actorOf(Player.props(), "player");
                    bulletManager = getContext().actorOf(BulletManager.props());
                    alienManager = getContext().actorOf(AlienManager.props(bulletManager));
                    getContext().become(playing());
                })
                .build();
    }
    private Receive playing() {
        return receiveBuilder()
                .match(Tick.class, t -> {
                    guiActor.tell(new GameStateDto(GameStateDto.State.Playing, playerDto, bullets, aliens), getSelf());
                    bulletManager.tell(t, getSelf());
                    alienManager.tell(t, getSelf());
                })
                .match(MoveLeft.class, ml -> {
                    player.tell(ml, getSelf());
                })
                .match(MoveRight.class, mr -> {
                    player.tell(mr, getSelf());
                })
                .match(PlayerDto.class, dto -> {
                    playerDto = dto;
                    if (playerDto.lives < 1) {
                        guiActor.tell(new GameStateDto(GameStateDto.State.GameLost, playerDto, bullets, aliens), getSelf());
                        getContext().become(idle());
                    }
                })
                .match(Fire.class, f -> {
                    player.tell(new Player.Fire(bulletManager), getSelf());
                })
                .match(Bullets.class, bs -> {
                    bullets = bs.bullets;
                })
                .match(Aliens.class, as -> {
                    aliens = as.aliens;
                    if (aliens.isEmpty()) {
                        guiActor.tell(new GameStateDto(GameStateDto.State.GameWon, playerDto, bullets, aliens), getSelf());
                        getContext().become(idle());
                    }
                })
                .build();
    }


    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(ActorRef guiActor) {
        return Props.create(Game.class, () -> new Game(guiActor));
    }

    public static class Tick {

    }

    public static class Start {
        public void foo() {

        }
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
        return idle();
    }

}