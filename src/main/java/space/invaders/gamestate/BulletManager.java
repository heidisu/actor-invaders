package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.BulletDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BulletManager extends AbstractActor {
    private int nextId = 1;
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
                    BulletDto.Sender sender =  getSender().path().name().equals("player") ? BulletDto.Sender.Player : BulletDto.Sender.Alien;
                    ActorRef bullet =  getContext().actorOf(Bullet.props(sender, nextId, cb.posX, cb.posY), "bullet-" + nextId);
                    getContext().watch(bullet);
                    nextId ++;
                })
                .match(Game.Tick.class, tick -> {
                    getContext().getChildren().forEach(br -> br.tell(tick, getSelf()));
                    getContext().getParent().tell(new Game.Bullets(Collections.unmodifiableList(new ArrayList<>(refToBullet.values()))), getSelf());
                })
                .match(BulletDto.class, bd -> refToBullet.put(getSender(), bd))
                .match(Terminated.class, t -> {
                    refToBullet.remove(t.getActor());
                })
                .build();
    }
}