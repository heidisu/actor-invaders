package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.AlienDto;

import java.util.*;

public class AlienManager extends AbstractActor {
    private Map<ActorRef, AlienDto> refToAlien = new HashMap<>();
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef [][] alienGrid = new ActorRef [rows][columns];
    private final ActorRef bulletManager;
    private Random random = new Random();

    static Props props(ActorRef bulletManager){
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
                    getContext().getChildren().forEach(alien -> alien.tell(tick, getSelf()));
                    getContext().getParent().tell(new Game.Aliens(Collections.unmodifiableList(new ArrayList<>(refToAlien.values()))), getSelf());
                })
                .match(AlienDto.class, alienDto -> refToAlien.put(getSender(), alienDto))
                .match(Terminated.class, terminated -> {
                    removeAlien(terminated.getActor());

                })
                .build();
    }

    private void removeAlien(ActorRef deadAlien){
        refToAlien.remove(deadAlien);
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                if(alienGrid[i][j] == deadAlien){
                    alienGrid[i][j] = null;
                    return;
                }
            }
        }
    }
}
