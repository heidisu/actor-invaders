package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.AlienDto;

import java.util.*;

public class AlienManager extends AbstractActor {
    private Set<ActorRef> alienRefs = new HashSet<>();
    private Map<ActorRef, AlienDto> refToAlien = null;
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef [][] alienGrid = new ActorRef [columns][rows];
    private final ActorRef bulletManager;

    private Random random = new Random();

    public static Props props(ActorRef bulletManager) {
        return Props.create(AlienManager.class, bulletManager);
    }

    private AlienManager(ActorRef bulletManager) {
        this.bulletManager = bulletManager;
        for (int column = 0; column < columns; column ++) {
            for (int row = 0; row < rows; row ++) {
                int id = (column * columns) + row;
                AlienImageSet image = AlienImageSet.images.get(row % 3);
                ActorRef alien = getContext().actorOf(Alien.props(id, 10 + (50 * column), 10 + (40 * row), image), "alien-" + id);
                getContext().watch(alien);
                alienGrid[column][row] = alien;
                alienRefs.add(alien);
            }
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, t -> {
                    if (refToAlien != null) {
                        getSender().tell(new Game.Aliens(new ArrayList(refToAlien.values())), getSelf());
                        maybeFire();
                    }
                    for (ActorRef alien : alienRefs) {
                        alien.tell(t, getSelf());
                    }
                })
                .match(AlienDto.class, dto -> {
                    if (refToAlien == null) {
                        refToAlien = new HashMap<>();
                    }
                    refToAlien.put(getSender(), dto);
                })
                .match(Terminated.class, t -> {
                    ActorRef alien = t.getActor();
                    refToAlien.remove(alien);
                    alienRefs.remove(alien);
                    for (int column = 0; column < columns; column ++) {
                        for (int row = 0; row < rows; row ++) {
                            if (alienGrid[column][row] == alien) {
                                alienGrid[column][row] = null;
                            }
                        }
                    }
                })
                .build();
    }

    private void maybeFire() {
        if (random.nextInt(10) == 5) {
            int column = random.nextInt(columns);
            for (int row = rows - 1; row > -1; row--) {
                ActorRef alien = alienGrid[column][row];
                if (alien != null) {
                    alien.tell(new Alien.Fire(bulletManager), getSelf());
                    return;
                }
            }
        }
    }
}
