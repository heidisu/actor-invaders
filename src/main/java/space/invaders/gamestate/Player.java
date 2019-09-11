package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.Image;
import space.invaders.dto.PlayerDto;

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
        posX = sceneWidth/2 - width/2;
        posY = sceneHeight - height;
        getContext().getParent().tell(getPlayerDto(), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.MoveLeft.class, ml -> {
                    posX = Math.max(0, posX -5);
                    getContext().getParent().tell(getPlayerDto(), getSelf());
                })
                .match(Game.MoveRight.class, mr -> {
                    posX = Math.min(sceneWidth - width, posX + 5);
                    getContext().getParent().tell(getPlayerDto(), getSelf());
                })
                .build();
    }

    private PlayerDto getPlayerDto() {
        return new PlayerDto(posX, posY, lives, image);
    }
}
