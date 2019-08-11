package space.invaders;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("space-invaders");
        //ActorRef gui = system.actorOf(GUI.props().withDispatcher("javafx-dispatcher"), "gui");
        ActorRef gameInitializer = system.actorOf(GameInitializer.props(), "game-initializer");
        //gameInitializer.tell(new GameInitializer.Initialize(gui, "player1"), ActorRef.noSender());
    }
}