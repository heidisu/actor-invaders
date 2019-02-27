package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.AlienDto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlienManager extends AbstractActor {
    private Set<ActorRef> alienRefs = new HashSet<>();
    private Map<ActorRef, AlienDto> refToAlien = new HashMap<>();
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef [][] alienGrid = new ActorRef [columns][rows];
    private final ActorRef bulletManager;
    private int fireBulletCounter = 0;
    private Random random = new Random();

    static Props props(ActorRef bulletManager){
        return Props.create(AlienManager.class, () -> new AlienManager(bulletManager));
    }

    public AlienManager(ActorRef bulletManager) {
        this.bulletManager = bulletManager;
        int id = 1;
        var posY = 20;
        for (int j = 0; j < rows; j++) {
            var image = AlienImage.images.get(j % AlienImage.images.size());
            for (int i = 0; i < columns; i++) {
                var posX = 10 + 60 * i;
                var alien = context().actorOf(Alien.props(id, posX, posY, image), "alien-" + id);
                context().watch(alien);
                alienRefs.add(alien);
                alienGrid[i][j] = alien;
                id++;
            }
            posY = posY + image.height + 20;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    fireRandomBullet();
                    alienRefs.parallelStream().forEach(alien -> alien.tell(tick, getSelf()));
                    getContext().getParent().tell(new Game.Aliens(List.copyOf(refToAlien.values())), getSelf());
                })
                .match(AlienDto.class, alienDto -> refToAlien.put(getSender(), alienDto))
                .match(Terminated.class, terminated -> {
                    removeAlien(terminated.getActor());

                })
                .build();
    }

    private void fireRandomBullet() {
        if(fireBulletCounter != 10 ){
            fireBulletCounter++;
        }
        else {
            fireBulletCounter = 0;
            var nonEmptyColumns =
                    IntStream
                            .range(0, columns)
                            .filter(i -> alienGrid[i][rows - 1] != null)
                            .boxed()
                            .collect(Collectors.toList());
            var idx = random.nextInt(nonEmptyColumns.size());
            var col = nonEmptyColumns.get(idx);
            var maxRow = 0;
            for (int j = 3; j >= 0; j--) {
                if (alienGrid[col][j] != null) {
                    maxRow = j;
                    break;
                }
            }
            var actor = alienGrid[col][maxRow];
            actor.tell(new Alien.Fire(bulletManager), self());
        }
    }

    private void removeAlien(ActorRef deadAlien){
        refToAlien.remove(deadAlien);
        for(int i = 0; i < columns; i++){
            for(int j = 0; j < rows; j++){
                if(alienGrid[i][j] == deadAlien){
                    alienGrid[i][j] = null;
                    return;
                }
            }
        }
    }
}
