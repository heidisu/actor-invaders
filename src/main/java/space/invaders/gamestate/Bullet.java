package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.BulletDto;

import space.invaders.dto.GameStateDto;

public class Bullet extends AbstractActor {
    private final int id;
    private final int posX;
    private int posY;
    private final BulletDto.Sender sender;
    private final int move;

    static Props props(BulletDto.Sender sender, int id, int posX, int posY){
        return Props.create(Bullet.class, () -> new Bullet(sender, id, posX, posY));
    }

    public Bullet(BulletDto.Sender sender, int id, int posX, int posY) {
        this.sender = sender;
        this.move = sender == BulletDto.Sender.Player ? -10 : 10;
        this.id = id;
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    posY += move;
                    if (posY < 0 || posY > GameStateDto.screenSize.height) {
                        getContext().stop(getSelf());
                    }
                    else {
                        BulletDto bulletDto = new BulletDto(id, posX, posY, sender);
                        getContext().getParent().tell(bulletDto, getSelf());
                    }
                })
                .build();
    }
}

