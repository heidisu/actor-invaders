package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import space.invaders.dto.BulletDto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BulletManager extends AbstractActor {
    private int nextId = 1;
    private Set<ActorRef> bulletRefs = new HashSet<>();
    private Map<ActorRef, BulletDto> refToBullet = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}