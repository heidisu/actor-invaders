package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.BulletDto;

public class Bullet extends AbstractActor {
    private /*final*/ int id;
    private /*final*/ int posX;
    private int posY;

    static Props props(int id, int posX, int posY){
        return Props.create(Bullet.class, () -> new Bullet(id, posX, posY));
    }

    public Bullet(int id, int posX, int posY) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    posY = posY - 10;
                    if (posY < 0) {
                        getContext().stop(getSelf());
                    }
                    else {
                        getContext().getParent().tell(new BulletDto(id, posX, posY, "player-bullet"), getSelf());
                    }
                })
                .build();
    }
}

