package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;

import java.util.function.Function;

public class Bullet extends AbstractActor {
    private /*final*/ int id;
    private /*final*/ int posX;
    private int posY;
    private final BulletDto.Sender sender;
    private final Function<Integer, Integer> move;

    static Props props(BulletDto.Sender sender, int id, int posX, int posY){
        return Props.create(Bullet.class, () -> new Bullet(sender, id, posX, posY));
    }

    public Bullet(BulletDto.Sender sender, int id, int posX, int posY) {
        this.sender = sender;
        this.move = sender == BulletDto.Sender.Player ? i -> i - 10 : i -> i + 10;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    posY = move.apply(posY);
                    if (posY < 0 | posY > GameStateDto.screenSize.height) {
                        getContext().stop(getSelf());
                    }
                    else {
                        getContext().getParent().tell(new BulletDto(id, posX, posY, sender), getSelf());
                    }
                })
                .build();
    }
}

