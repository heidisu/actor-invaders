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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game extends AbstractActor {
    private final ActorRef guiActor;
    private final int width = GameStateDto.screenSize.width;
    private final int height = GameStateDto.screenSize.height;
    private ActorRef player;
    private ActorRef bulletMananger;
    private ActorRef alienManager;
    private PlayerDto playerDto;
    private List<BulletDto> bullets = new ArrayList<>();
    private List<AlienDto> aliens = new ArrayList<>();

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(ActorRef guiActor) {
        return Props.create(Game.class, () -> new Game(guiActor));
    }

    public static class Tick {

    }

    public static class Start implements Serializable {

    }

    public static class Fire implements Serializable{

    }

    public static class MoveLeft implements Serializable{

    }

    public static class MoveRight implements Serializable{

    }

    public static class Bullets {
        final List<BulletDto> bullets;

        public Bullets(List<BulletDto> bullets) {
            this.bullets = bullets;
        }
    }

    public static class Aliens {
        final List<AlienDto> aliens;

        public Aliens(List<AlienDto> aliens) {
            this.aliens = aliens;
        }
    }

    private Game(ActorRef guiActor) {
        this.guiActor = guiActor;
    }


    private Receive getIdle() {
        return receiveBuilder()
                .match(Start.class, start -> {
                        player = getContext().actorOf(Player.props(), "player");
                        getContext().getSystem().getEventStream().subscribe(player, Events.AlienBulletMoved.class);
                        bulletMananger = getContext().actorOf(BulletManager.props(), "bulletmanager");
                        alienManager = getContext().actorOf(AlienManager.props(bulletMananger), "alienmanager");
                        log.info("Game started!");
                        getContext().become(getPlaying());
                })
                .build();
    }

    private Receive getPlaying() {
        return receiveBuilder()
                .match(Tick.class, tick -> {
                    guiActor.tell(new GameStateDto(GameStateDto.State.Playing, playerDto, bullets, aliens), getSelf());
                    bulletMananger.tell(tick, getSelf());
                    alienManager.tell(tick, getSelf());
                })
                .match(MoveLeft.class, ml -> player.tell(ml, getSelf()))
                .match(MoveRight.class, mr -> player.tell(mr, getSelf()))
                .match(PlayerDto.class, playerDto -> {
                    this.playerDto = playerDto;
                    if(playerDto.lives == 0){
                        guiActor.tell(new GameStateDto(GameStateDto.State.GameLost, playerDto, bullets, aliens), getSelf());
                        getContext().become(getGameOver());
                    }
                })
                .match(Fire.class, fire -> player.tell(new Player.Fire(bulletMananger), getSelf()))
                .match(Bullets.class, bullets -> this.bullets = bullets.bullets)
                .match(Aliens.class, aliens -> {
                    boolean noMoreAliens = !this.aliens.isEmpty() && aliens.aliens.isEmpty();
                    this.aliens = aliens.aliens;
                    if(noMoreAliens){
                        guiActor.tell(new GameStateDto(GameStateDto.State.GameWon, playerDto, bullets, this.aliens), getSelf());
                        getContext().become(getGameOver());
                    }
                })
                .build();
    }

    private Receive getGameOver() {
        return receiveBuilder().build();
    }

    @Override
    public Receive createReceive() {
        return getIdle();
    }

}