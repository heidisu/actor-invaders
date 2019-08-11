package space.invaders;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import space.invaders.dto.GameStateDto;
import space.invaders.gamestate.Game;
import space.invaders.gui.GUI;

import java.io.Serializable;
import java.time.Duration;

public class GameInitializer extends AbstractActor implements Serializable{
private final ActorRef gameMonitor;

    public static class Initialize implements Serializable {
        public final ActorRef gui;
        public final String playerId;

        public Initialize(ActorRef gui, String playerId) {
            this.gui = gui;
            this.playerId = playerId;
        }
    }

    static public Props props(ActorRef gameMonitor) {
        return Props.create(GameInitializer.class, () -> new GameInitializer(gameMonitor));
    }

    public GameInitializer(ActorRef gameMonitor) {
        this.gameMonitor = gameMonitor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Initialize.class, initialize -> {
                    ActorRef game =
                            getContext().getSystem().actorOf(
                                    Game.props(initialize.gui, gameMonitor),
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
