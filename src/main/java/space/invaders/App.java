package space.invaders;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import space.invaders.gui.GUI;

public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("space-invaders");
        ActorRef gui = system.actorOf(GUI.props().withDispatcher("javafx-dispatcher"), "gui");
        //ActorRef gameInitializer = system.actorOf(GameInitializer.props(), "game-initializer");
        ActorSelection gameInitializer = system.actorSelection("akka.tcp://space-invaders@127.0.0.1:2552/user/game-initializer");
        gameInitializer.tell(new GameInitializer.Initialize(gui, "player1"), ActorRef.noSender());
    }
}