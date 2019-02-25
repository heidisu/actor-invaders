package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import space.invaders.dto.AlienDto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlienManager extends AbstractActor {
    private Set<ActorRef> alienRefs = new HashSet<>();
    private Map<ActorRef, AlienDto> refToAlien = new HashMap<>();
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef [][] alienGrid = new ActorRef [columns][rows];

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
