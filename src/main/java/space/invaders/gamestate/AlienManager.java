package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import space.invaders.dto.AlienDto;

import java.util.HashMap;
import java.util.Map;

public class AlienManager extends AbstractActor {
    private Map<ActorRef, AlienDto> refToAlien = new HashMap<>();
    private final int columns = 10;
    private final int rows = 4;
    private ActorRef [][] alienGrid = new ActorRef [rows][columns];

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
