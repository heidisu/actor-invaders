package space.invaders.gamestate;

import akka.actor.AbstractActor;

public class Alien extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
