package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Bullet extends AbstractActor {
    private /*final*/ int id;
    private /*final*/ int posX;
    private int posY;
    private final String style;
    private final Function<Integer, Integer> move;
    private final BiFunction<ActorRef, BulletDto, Events.BulletMoved> createEvent;

    static Props props(Type type, int id, int posX, int posY){
        return Props.create(Bullet.class, () -> new Bullet(type, id, posX, posY));
    }

    public enum Type {
        Player,
        Alien
    }

    public Bullet(Type type, int id, int posX, int posY) {
        style = type == Type.Player ? "player-bullet" : "alien-bullet";
        move = type == Type.Player ? i -> i - 10 :  i -> i + 10;
        createEvent = type == Type.Player ? Events.PlayerBulletMoved::new : Events.AlienBulletMoved::new;
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
                        BulletDto bulletDto = new BulletDto(id, posX, posY, style);
                        getContext().getParent().tell(bulletDto, getSelf());
                        getContext().getSystem().getEventStream().publish(createEvent.apply(getSelf(), bulletDto));
                    }
                })
                .build();
    }
}

