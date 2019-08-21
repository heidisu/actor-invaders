package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.AlienDto;
import space.invaders.dto.Image;

import java.util.function.Function;

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
    private static final Function<Integer, Integer> moveRight = i -> i + 5;
    private static final Function<Integer, Integer> moveLeft = i -> i - 5;
    private Function<Integer, Integer> move = moveRight;

    static Props props(int id, int posX, int posY, AlienImageSet imageSet){
        return Props.create(Alien.class, () -> new Alien(id, posX, posY, imageSet));
    }

    public Alien(int id, int posX, int posY, AlienImageSet imageSet) {
        this.id = id;
        this.currentImage = imageSet.getFirst();
        this.posX = posX;
        this.posY = posY;
        this.imageSet = imageSet;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    move();
                    changeDirection();
                    getContext().getParent().tell(new AlienDto(id, posX, posY, currentImage), getSelf());
                } )
                .build();
    }

    private void changeDirection() {
        if (countMoves == movesBetweenDirectionChange){
            countMoves = -movesBetweenDirectionChange;
            move = move.equals(moveLeft) ? moveRight : moveLeft;
        }
    }

    private void move() {
        if(countTicks == ticksBetweenMove) {
            currentImage = imageSet.getOther(currentImage);
            posX = move.apply(posX);
            countTicks = 0;
            countMoves++;
        }
        else {
            countTicks++;
        }
    }
}
