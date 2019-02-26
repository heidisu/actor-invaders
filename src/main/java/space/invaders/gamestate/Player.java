package space.invaders.gamestate;

import akka.actor.AbstractActor;
import akka.actor.Props;
import space.invaders.dto.GameStateDto;
import space.invaders.dto.Image;
import space.invaders.dto.PlayerDto;

import java.util.function.Function;

public class Player extends AbstractActor {
    private int lives = 3;
    private int posX;
    private int posY;
    private final int width = 50;
    private final int height = 50 * 140/280;
    private final Image image = new Image(width, height, "img/cannon.png");
    private final int sceneWidth = GameStateDto.screenSize.width;
    private final int sceneHeight = GameStateDto.screenSize.height;

    static Props props() {
        return Props.create(Player.class, Player::new);
    }

    private Player(){
        posX = sceneWidth/2 - width/2;
        posY = sceneHeight - height;
        getContext().getParent().tell(getPlayerDto(), getSelf());
    }

    private void updatePosition(Function<Integer, Integer> move){
        int newPos = move.apply(posX);
        posX = newPos < 0 || newPos > sceneWidth - width ? posX : newPos;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Game.MoveLeft.class, ml -> {
                    updatePosition(pos -> pos - 5);
                    getContext().getParent().tell(getPlayerDto(), getSelf());
                })
                .match(Game.MoveRight.class, mr -> {
                    updatePosition(pos -> pos + 5);
                    getContext().getParent().tell(getPlayerDto(), getSelf());
                })
                .build();
    }

    private PlayerDto getPlayerDto() {
        return new PlayerDto(posX, posY, lives, image);
    }
}
