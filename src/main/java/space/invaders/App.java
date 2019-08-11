package space.invaders;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import space.invaders.monitoring.GameMonitor;

public class App {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("space-invaders");
        //ActorRef gui = system.actorOf(GUI.props().withDispatcher("javafx-dispatcher"), "gui");
        ActorRef gameMonitor = system.actorOf(GameMonitor.props().withDispatcher("javafx-dispatcher"), "game-monitor");
        ActorRef gameInitializer = system.actorOf(GameInitializer.props(gameMonitor), "game-initializer");
        //ActorSelection gameInitializer = system.actorSelection("akka.tcp://space-invaders@192.168.1.41:2552/user/game-initializer");
        //gameInitializer.tell(new GameInitializer.Initialize(gui, "player2"), ActorRef.noSender());
    }
}