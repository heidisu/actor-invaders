package space.invaders.gui;

import akka.dispatch.DispatcherPrerequisites;
import akka.dispatch.ExecutorServiceConfigurator;
import akka.dispatch.ExecutorServiceFactory;
import com.typesafe.config.Config;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class JavaFXExecutorServiceConfigurator extends ExecutorServiceConfigurator {
    private ExecutorServiceFactory factory = JavaFXExecutorService::new;

    public JavaFXExecutorServiceConfigurator(Config config, DispatcherPrerequisites prerequisites) {
        super(config, prerequisites);
        Platform.startup(() -> {});
    }


    @Override
    public ExecutorServiceFactory createExecutorServiceFactory(String id, ThreadFactory threadFactory) {
        return factory;
    }

    class JavaFXExecutorService extends AbstractExecutorService {

        @Override
        public void shutdown() {

        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            Platform.runLater(command);
        }
    }

}
