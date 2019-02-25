package space.invaders;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import space.invaders.dto.GameStateDto;
import space.invaders.gamestate.Game;
import space.invaders.gui.GUI;

import java.io.Serializable;
import java.time.Duration;

public class GameInitializer extends AbstractActor {

    public static class Initialize implements Serializable {
        public final ActorRef gui;
        public final String clientid;

        public Initialize(ActorRef gui, String clientId) {
            this.gui = gui;
            this.clientid = clientId;
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
                                    "game-"+ initialize.clientid);
                    getContext().getSystem().scheduler().schedule(
                            Duration.ZERO,
                            Duration.ofMillis(1000/ GameStateDto.speed.value), game, new Game.Tick(),
                            getContext().getSystem().dispatcher(), ActorRef.noSender());
                    initialize.gui.tell(new GUI.GameInitialized(game), self());
                })
                .build();
    }
}
