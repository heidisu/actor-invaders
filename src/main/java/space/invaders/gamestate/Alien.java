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
    private final AlienImageSet imageSet;
    private int countDown = 10;
    private String currentImage;
    private final Function<Integer, Integer> moveRight = i -> i + 5;
    private final Function<Integer, Integer> moveLeft = i -> i - 5;
    private Function<Integer, Integer> move = moveRight;
    private int countMoves;

    static Props props(int id, int posX, int posY, AlienImageSet imageSet){
        return Props.create(Alien.class, () -> new Alien(id, posX, posY, imageSet));
    }

    static class Fire {
        private final ActorRef bulletManager;

        Fire(ActorRef bulletManager) {
            this.bulletManager = bulletManager;
        }
    }

    public Alien(int id, int posX, int posY, AlienImageSet imageSet) {
        this.id = id;
        this.currentImage = imageSet.imagePath1;
        this.posX = posX;
        this.posY = posY;
        this.imageSet = imageSet;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    if(countDown == 0) {
                        currentImage = currentImage.equals(imageSet.imagePath1) ? imageSet.imagePath2 : imageSet.imagePath1;
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
                    getContext().getParent().tell(new AlienDto(id, posX, posY, new Image(imageSet.width, imageSet.height, currentImage)), getSelf());
                } )
                .match(Fire.class, fire -> fire.bulletManager.tell(new BulletManager.CreateBullet(posX + imageSet.width/2, posY + imageSet.height), getSelf()))
                .match(Events.PlayerBulletMoved.class, bm -> {
                    if(isHit(bm.bulletDto.posX, bm.bulletDto.posY)){
                        getContext().stop(bm.bulletActor);
                        getContext().stop(self());
                    }
                })
                .build();
    }

    private boolean isHit(int x, int y){
        return x >= posX && x <= posX + imageSet.width && y >= posY && y <= posY + imageSet.height;
    }
}
