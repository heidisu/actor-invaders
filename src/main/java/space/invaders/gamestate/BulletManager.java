package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import com.sun.javafx.collections.UnmodifiableListSet;
import space.invaders.dto.BulletDto;

import java.util.*;

public class BulletManager extends AbstractActor {
    private int nextId = 0;
    private Set<ActorRef> bulletRefs = new HashSet<>();
    private Map<ActorRef, BulletDto> refToBullet = new HashMap<>();

    public static Props props() {
        return Props.create(BulletManager.class);
    }

    public static class CreateBullet {
        public final int x;
        public final int y;
        public final Side side;

        public CreateBullet(int x, int y, Side side) {
            this.x = x;
            this.y = y;
            this.side = side;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateBullet.class, cb -> {
                    nextId = nextId + 1;
                    ActorRef bullet = getContext().actorOf(Bullet.props(nextId, cb.x, cb.y, cb.side), "bullet-" + nextId);
                    getContext().watch(bullet);
                    bulletRefs.add(bullet);
                })
                .match(Game.Tick.class, t -> {
                    getSender().tell(new Game.Bullets(new ArrayList(refToBullet.values())), getSelf());
                    for (ActorRef b : bulletRefs) {
                        b.tell(t, getSelf());
                    }
                })
                .match(BulletDto.class,  dto -> {
                    refToBullet.put(getSender(), dto);
                })
                .match(Terminated.class, t -> {
                    bulletRefs.remove(t.getActor());
                    refToBullet.remove(t.getActor());
                })
                .build();
    }
}