package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.Image;

public class Player extends AbstractActor {
    private int lives = 3;
    private int posX;
    private int posY;
    private static final int width = 50;
    private static final int height = 50 * 140/280;
    private static final Image image = new Image(width, height, "img/cannon.png");
    private static final int sceneWidth = GameStateDto.screenSize.width;
    private static final int sceneHeight = GameStateDto.screenSize.height;

    static Props props() {
        return Props.create(Player.class, Player::new);
    }

    private Player(){
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
