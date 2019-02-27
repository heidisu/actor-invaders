package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.AlienDto;
import space.invaders.dto.Image;

import java.util.function.Function;

public class Alien extends AbstractActor {
    private final int id;
    private int posX;
    private final int posY;
    private final AlienImage image;
    private int countDown = 10;
    private String currentImage;
    private final Function<Integer, Integer> moveRight = i -> i + 5;
    private final Function<Integer, Integer> moveLeft = i -> i - 5;
    private Function<Integer, Integer> move = moveRight;
    private int countMoves;

    static Props props(int id, int posX, int posY, AlienImage image){
        return Props.create(Alien.class, () -> new Alien(id, posX, posY, image));
    }

    static class Fire {
        private final ActorRef bulletManager;

        Fire(ActorRef bulletManager) {
            this.bulletManager = bulletManager;
        }
    }

    public Alien(int id, int posX, int posY, AlienImage image) {
        this.id = id;
        this.currentImage = image.imagePath1;
        this.posX = posX;
        this.posY = posY;
        this.image = image;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    if(countDown == 0) {
                        currentImage = currentImage.equals(image.imagePath1) ? image.imagePath2 : image.imagePath1;
                        posX = move.apply(posX);
                        countDown = 10;
                        countMoves++;
                    }
                    else {
                        countDown--;
                    }
                    if (countMoves == 15){
                        countMoves = -15;
                        move = move.equals(moveLeft) ? moveRight : moveLeft;
                    }
                    context().parent().tell(new AlienDto(id, posX, posY, new Image(image.width, image.height, currentImage)), self());
                } )
                .match(Fire.class, fire -> fire.bulletManager.tell(new BulletManager.CreateBullet(posX + image.width/2, posY + image.height), getSelf()))
                .build();
    }
}
