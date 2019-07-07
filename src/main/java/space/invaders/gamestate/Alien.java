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
    private Image currentImage;
    private final Function<Integer, Integer> moveRight = i -> i + 5;
    private final Function<Integer, Integer> moveLeft = i -> i - 5;
    private Function<Integer, Integer> move = moveRight;
    private int countMoves;

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
                    if(countDown == 0) {
                        currentImage = imageSet.getOther(currentImage);
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
                    getContext().getParent().tell(new AlienDto(id, posX, posY, currentImage), getSelf());
                } )
                .build();
    }
}
