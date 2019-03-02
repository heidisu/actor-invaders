package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import space.invaders.dto.BulletDto;

import java.util.HashMap;
import java.util.Map;

public class BulletManager extends AbstractActor {
    private int nextId = 1;
    private Map<ActorRef, BulletDto> refToBullet = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}