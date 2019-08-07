package space.invaders.gamestate;

import akka.actor.ActorRef;
import space.invaders.dto.BulletDto;

public class Events {

    static abstract class BulletMoved {
        final ActorRef bulletActor;
        final BulletDto bulletDto;

        BulletMoved(ActorRef bulletActor, BulletDto bulletDto) {
            this.bulletActor = bulletActor;
            this.bulletDto = bulletDto;
        }
    }

    static class AlienBulletMoved extends BulletMoved implements Player.PlayerMessage {

        AlienBulletMoved(ActorRef bulletActor, BulletDto bulletDto) {
            super(bulletActor, bulletDto);
        }
    }

    static class PlayerBulletMoved extends BulletMoved {

        PlayerBulletMoved(ActorRef bulletActor, BulletDto bulletDto) {
            super(bulletActor, bulletDto);
        }
    }
}
