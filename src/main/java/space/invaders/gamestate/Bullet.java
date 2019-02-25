package space.invaders.gamestate;

import akka.actor.AbstractActor;

public class Bullet extends AbstractActor {
    private /*final*/ int id;
    private /*final*/ int posX;
    private int posY;

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}

