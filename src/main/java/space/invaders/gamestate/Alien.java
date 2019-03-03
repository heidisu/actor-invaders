package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.AlienDto;
import space.invaders.dto.BulletDto;
import space.invaders.dto.Image;

public class Alien extends AbstractActor {

    private final int id;
    private int posX;
    private int posY;
    private AlienImageSet imageSet;
    private int time = 0;
    private int moveBy = 2;

    public static Props props(int id, int posX, int posY, AlienImageSet imageSet) {
        return Props.create(Alien.class, id, posX, posY, imageSet);
    }

    public static class Fire {
        public final ActorRef manager;

        public Fire(ActorRef manager) {
            this.manager = manager;
        }
    }

    public Alien(int id, int posX, int posY, AlienImageSet imageSet) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.imageSet = imageSet;
        context().system().eventStream().subscribe(getSelf(), Events.PlayerBulletMoved.class);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.Tick.class, t -> {
                    time = time + 1;
                    if (time == 50) {
                        time = 0;
                        moveBy = -moveBy;
                    }
                    posX = posX + moveBy;
                    String imagePath = time < 10 ? imageSet.imagePath1 : imageSet.imagePath2;
                    getContext().getParent().tell(new AlienDto(id, posX, posY, new Image(imageSet.width, imageSet.height, imagePath)), getSelf());
                })
                .match(Fire.class, f -> {
                    f.manager.tell(new BulletManager.CreateBullet(posX + (imageSet.width / 2), posY + imageSet.height, Side.Alien), getSelf());
                })
                .match(Events.PlayerBulletMoved.class, b -> {
                    BulletDto dto = b.bulletDto;
                    if (dto.posX > posX && dto.posX < (posX + imageSet.width) && dto.posY > posY && dto.posY < (posY + imageSet.height)) {
                        getContext().stop(getSelf());
                        getContext().stop(b.bulletActor);
                    }
                })
                .build();
    }
}
