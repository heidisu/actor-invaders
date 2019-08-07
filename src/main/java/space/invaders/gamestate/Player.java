package space.invaders.gamestate;

import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.Image;
import space.invaders.dto.PlayerDto;

import java.util.function.Function;

public class Player {
    private static final int width = 50;
    private static final int height = 50 * 140/280;
    private static final Image image = new Image(width, height, "img/cannon.png");
    private static final int sceneWidth = GameStateDto.screenSize.width;
    private static final int sceneHeight = GameStateDto.screenSize.height;

    interface PlayerMessage {}

    static class Fire implements PlayerMessage {
        final ActorRef bulletManager;

        Fire(ActorRef bulletManager) {
            this.bulletManager = bulletManager;
        }
    }

    public static final class Start implements PlayerMessage {
        final ActorRef parent;

        public Start(ActorRef parent) {
            this.parent = parent;
        }
    }

    static Behavior<PlayerMessage> startPlayer() {
        return Behaviors.receive(PlayerMessage.class)
                .onMessage(
                Start.class,
                (context, message) -> {
                    int posX = sceneWidth / 2 - width / 2;
                    int posY = sceneHeight - height;
                    int lives = 3;
                    message.parent.tell(getPlayerDto(posX, posY, lives), Adapter.toUntyped(context.getSelf()));
                    return playing(message.parent, posX, posY, lives);
                }
        ).build();
    }

    private static Behavior<PlayerMessage> playing(ActorRef parent, int posX, int posY, int lives) {
        return Behaviors.receive(PlayerMessage.class)
                .onMessage(
                        Game.MoveLeft.class,
                        (context, moveLeft) -> {
                           int newPosX = getPosition(posX, pos -> pos - 5);
                           parent.tell(getPlayerDto(newPosX, posY, lives), Adapter.toUntyped(context.getSelf()));
                           return playing(parent, newPosX, posY, lives);
                        })
                .onMessage(
                        Game.MoveRight.class,
                        (context, moveRight) -> {
                            int newPosX = getPosition(posX, pos -> pos + 5);
                            parent.tell(getPlayerDto(newPosX, posY, lives), Adapter.toUntyped(context.getSelf()));
                            return playing(parent, newPosX, posY, lives);
                        })
                .onMessage(
                        Fire.class,
                        (context, fire) -> {
                            fire.bulletManager.tell(new BulletManager.CreateBullet(posX + width/2, posY), Adapter.toUntyped(context.getSelf()));
                            return playing(parent, posX, posY, lives);
                        }
                )
                .onMessage(
                        Events.AlienBulletMoved.class,
                        (context, bulletMoved) -> {
                            int newLives = lives;
                            if (isHit(posX, posY, bulletMoved.bulletDto)){
                                bulletMoved.bulletActor.tell(new Bullet.Stop(), Adapter.toUntyped(context.getSelf()));
                                newLives --;
                                parent.tell(getPlayerDto(posX, posY, newLives), Adapter.toUntyped(context.getSelf()));
                            }
                            return playing(parent, posX, posY, newLives);
                        }
                )
                .build();
    }

    private static int getPosition(int posX, Function<Integer, Integer> move){
        int newPos = move.apply(posX);
        return newPos < 0 || newPos > sceneWidth - width ? posX : newPos;
    }

    private static boolean isHit(int posX, int posY, BulletDto bd) {
        return bd.posX >= posX && bd.posX <= posX + width && bd.posY >= posY + height / 3 && bd.posY <= posY + height;
    }

    private static PlayerDto getPlayerDto(int posX, int posY, int lives) {
        return new PlayerDto(posX, posY, lives, image);
    }
}
