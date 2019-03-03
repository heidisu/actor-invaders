package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.Image;
import space.invaders.dto.PlayerDto;

public class Player extends AbstractActor {
    private int lives = 3;
    private int posX;
    private int posY;
    private final int width = 50;
    private final int height = 50 * 140/280;
    private final Image image = new Image(width, height, "img/cannon.png");
    private final int sceneWidth = GameStateDto.screenSize.width;
    private final int sceneHeight = GameStateDto.screenSize.height;

    static Props props() {
        return Props.create(Player.class, Player::new);
    }

    public static class Fire {
        public final ActorRef manager;

        public Fire(ActorRef manager) {
            this.manager = manager;
        }
    }

    private Player() {
        posX = 300;
        posY = 350;
        getContext().getParent().tell(getDto(), getSelf());
        context().system().eventStream().subscribe(getSelf(), Events.AlienBulletMoved.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.MoveLeft.class, ml -> {
                    posX = posX - 5;
                    getContext().getParent().tell(getDto(), getSelf());
                })
                .match(Game.MoveRight.class, mr -> {
                    posX = posX + 5;
                    getContext().getParent().tell(getDto(), getSelf());
                })
                .match(Fire.class, f -> {
                    f.manager.tell(new BulletManager.CreateBullet(posX + (width / 2), posY, Side.Player), getSelf());
                })
                .match(Events.AlienBulletMoved.class, b -> {
                    BulletDto dto = b.bulletDto;
                    if (dto.posX > posX && dto.posX < (posX + width) && dto.posY > posY && dto.posY < (posY + height)) {
                        lives = lives - 1;
                        getContext().stop(b.bulletActor);
                    }
                })
                .build();
    }

    private PlayerDto getDto() {
        return new PlayerDto(posX, posY, lives, image);
    }
}
