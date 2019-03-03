package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.BulletDto;

public class Bullet extends AbstractActor {
    private final int id;
    private final int posX;
    private int posY;
    private final Side side;
    private final int moveBy;

    public static Props props(int id, int posX, int posY, Side side) {
        return Props.create(Bullet.class, id, posX, posY, side);
    }

    private Bullet(int id, int posX, int posY, Side side) {
        this. id = id;
        this.posX = posX;
        this.posY = posY;
        this.side = side;
        moveBy = side == Side.Player ? -5 : 5;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, t -> {
                    posY = posY + moveBy;
                    if (posY < 0) {
                        getContext().stop(getSelf());
                    } else {
                        BulletDto dto = new BulletDto(id, posX, posY, side);
                        getSender().tell(dto, getSelf());
                        Object event = side == Side.Player
                                ? new Events.PlayerBulletMoved(getSelf(), dto)
                                : new Events.AlienBulletMoved(getSelf(), dto);
                        context().system().eventStream().publish(event);
                    }
                })
                .build();
    }
}

