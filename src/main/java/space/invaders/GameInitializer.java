package space.invaders;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.GameStateDto;
import space.invaders.gamestate.Game;
import space.invaders.gui.GUI;

import java.io.Serializable;
import java.time.Duration;

public class GameInitializer extends AbstractActor implements Serializable{

    public static class Initialize implements Serializable {
        public final ActorRef gui;
        public final String playerId;

        public Initialize(ActorRef gui, String playerId) {
            this.gui = gui;
            this.playerId = playerId;
        }
    }

    static public Props props() {
        return Props.create(GameInitializer.class, GameInitializer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Initialize.class, initialize -> {
                    ActorRef game =
                            getContext().getSystem().actorOf(
                                    Game.props(initialize.gui),
                                    "game-"+ initialize.playerId);
                    getContext().getSystem().scheduler().schedule(
                            Duration.ZERO,
                            Duration.ofMillis(1000/ GameStateDto.speed.value), game, new Game.Tick(),
                            getContext().getSystem().dispatcher(), ActorRef.noSender());
                    initialize.gui.tell(new GUI.GameInitialized(game), self());
                })
                .build();
    }
}
