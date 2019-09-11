package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.AlienDto;
import space.invaders.dto.Image;

public class Alien extends AbstractActor {
    private final int id;
    private int posX;
    private final int posY;
    private final AlienImageSet imageSet;
    private static final int ticksBetweenMove = 10;
    private static final int movesBetweenDirectionChange = 15;
    private int countTicks = 0;
    private int countMoves = 0;
    private Image currentImage;
    private static final int moveRight = 5, moveLeft = -5;
    private int move = moveRight;

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
        this.posX = posX;
        this.posY = posY;
        this.imageSet = imageSet;
        this.currentImage = imageSet.getFirst();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    move();
                    changeDirection();
                    getContext().getParent().tell(new AlienDto(id, posX, posY, currentImage), getSelf());
                } )
                .match(Fire.class, fire -> fire.bulletManager.tell(new BulletManager.CreateBullet(posX + currentImage.width/2, posY + currentImage.height), getSelf()))
                .match(Events.PlayerBulletMoved.class, bm -> {
                    if(isHit(bm.bulletDto.posX, bm.bulletDto.posY)){
                        getContext().stop(bm.bulletActor);
                        getContext().stop(self());
                    }
                })
                .build();
    }

    private void changeDirection() {
        if (countMoves == movesBetweenDirectionChange){
            countMoves = -movesBetweenDirectionChange;
            move = move == moveLeft ? moveRight : moveLeft;
        }
    }

    private void move() {
        if(countTicks == ticksBetweenMove) {
            currentImage = imageSet.getOther(currentImage);
            posX += move;
            countTicks = 0;
            countMoves++;
        }
        else {
            countTicks++;
        }
    }

    private boolean isHit(int x, int y){
        return x >= posX && x <= posX + imageSet.getWidth() && y >= posY && y <= posY + imageSet.getHeight();
    }
}
