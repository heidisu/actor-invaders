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
    private Map<ActorRef, AlienDto> refToAlien = new HashMap<>();
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef[][] alienGrid = new ActorRef[rows][columns];
    private final ActorRef bulletManager;
    private Random random = new Random();
    private int fireBulletCounter;

    static Props props(ActorRef bulletManager) {
        return Props.create(AlienManager.class, () -> new AlienManager(bulletManager));
    }

    public AlienManager(ActorRef bulletManager) {
        this.bulletManager = bulletManager;
        int id = 1;
        int posY = 20;
        for (int i = 0; i < rows; i++) {
            AlienImageSet imageSet = AlienImageSet.images.get(i % AlienImageSet.images.size());
            for (int j = 0; j < columns; j++) {
                int posX = 10 + 60 * j;
                ActorRef alien = getContext().actorOf(Alien.props(id, posX, posY, imageSet), "alien-" + id);
                getContext().watch(alien);
                alienGrid[i][j] = alien;
                id++;
            }
            posY = posY + imageSet.getHeight() + 20;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, tick -> {
                    if(getContext().getChildren().iterator().hasNext()) {
                        fireRandomBullet();
                    }
                    getContext().getChildren().forEach(alien -> alien.tell(tick, getSelf()));
                    getContext().getParent().tell(new Game.Aliens(new ArrayList<>(refToAlien.values())), getSelf());
                })
                .match(AlienDto.class, alienDto -> refToAlien.put(getSender(), alienDto))
                .match(Terminated.class, terminated -> {
                    removeAlien(terminated.getActor());

                })
                .build();
    }

    private boolean columnHasAliens (int idx){
        for(int i = 0; i < rows; i++){
            if (alienGrid[i][idx] != null){
                return true;
            }
        }
        return false;
    }

    private List<Integer> getNonEmptyColumns() {
        return IntStream
                .range(0, columns)
                .filter(this::columnHasAliens)
                .boxed()
                .collect(Collectors.toList());
    }

    private void fireRandomBullet() {
        if (fireBulletCounter == 10) {
            fireBulletCounter = 0;
            List<Integer> nonEmptyColumns = getNonEmptyColumns();
            int idx = random.nextInt(nonEmptyColumns.size());
            int col = nonEmptyColumns.get(idx);
            for (int i = 3; i >= 0; i--) {
                if (alienGrid[i][col] != null) {
                    ActorRef actor = alienGrid[i][col];
                    actor.tell(new Alien.Fire(bulletManager), getSelf());
                    return;
                }
            }
        } else {
            fireBulletCounter++;
        }
    }

    private void removeAlien(ActorRef deadAlien) {
        refToAlien.remove(deadAlien);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (alienGrid[i][j] == deadAlien) {
                    alienGrid[i][j] = null;
                    return;
                }
            }
        }
    }
}
