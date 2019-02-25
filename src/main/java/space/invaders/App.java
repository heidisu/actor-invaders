package space.invaders;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import space.invaders.gui.GUI;

public class App {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("space-invaders");
        final ActorRef gui = system.actorOf(GUI.props().withDispatcher("javafx-dispatcher"), "gui");
        final ActorRef gameInitializer = system.actorOf(GameInitializer.props(), "game-initializer");
        gameInitializer.tell(new GameInitializer.Initialize(gui, "clientid"), ActorRef.noSender());
    }
}