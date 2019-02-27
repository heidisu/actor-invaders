package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.BulletDto;

import java.util.*;

public class BulletManager extends AbstractActor {
    private int nextId = 1;
    private Set<ActorRef> bulletRefs = new HashSet<>();
    private Map<ActorRef, BulletDto> refToBullet = new HashMap<>();

    public static Props props(){
        return Props.create(BulletManager.class, BulletManager::new);
    }


    static class CreateBullet{
        final int posX;
        final int posY;

        public CreateBullet(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateBullet.class, cb -> {
                    ActorRef bullet =  getContext().actorOf(Bullet.props(nextId, cb.posX, cb.posY), "bullet-" + nextId);
                    getContext().watch(bullet);
                    bulletRefs.add(bullet);
                    nextId ++;
                })
                .match(Game.Tick.class, tick -> {
                    bulletRefs.stream().parallel().forEach(br -> br.tell(tick, getSelf()));
                    getContext().getParent().tell(new Game.Bullets(List.copyOf(refToBullet.values())), getSelf());
                })
                .match(BulletDto.class, bd -> refToBullet.put(getSender(), bd))
                .match(Terminated.class, t -> {
                    bulletRefs.remove(t.getActor());
                    refToBullet.remove(t.getActor());
                })
                .build();
    }
}