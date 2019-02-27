package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.BulletDto;
import space.invaders.dto.GameStateDto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Bullet extends AbstractActor {
    private /*final*/ int id;
    private /*final*/ int posX;
    private int posY;
    private final String style;
    private final Function<Integer, Integer> move;

    private static Map<Type, String> typeToStyle = new HashMap<>();
    static {
        typeToStyle.put(Type.Player, "player-bullet");
        typeToStyle.put(Type.Alien, "alien-bullet");
    }

    private static Map<Type, Function<Integer, Integer>> typeToMove = new HashMap<>();
    static {
        typeToMove.put(Type.Player, i -> i - 10);
        typeToMove.put(Type.Alien, i -> i + 10);
    }


    static Props props(Type type, int id, int posX, int posY){
        return Props.create(Bullet.class, () -> new Bullet(type, id, posX, posY));
    }

    public enum Type {
        Player,
        Alien
    }

    public Bullet(Type type, int id, int posX, int posY) {
        this.style = typeToStyle.get(type);
        this.move = typeToMove.get(type);
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
                        getContext().getParent().tell(new BulletDto(id, posX, posY, style), getSelf());
                    }
                })
                .build();
    }
}

