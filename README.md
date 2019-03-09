# Space invaders actor workshop

![](img/game.gif)

In this workshop we will complete a simplified version of the classic game Space Invaders, as shown above. While we are making the game work, you will get experience with how Akka actors work, their lifecycle, how to change their behaviour, and different ways to send messages to other actors.

The GUI part is already there, but it isn't doing anything yet, our task will be to finish the game logic which uses a hierarchy of actors to manage updates and keep track of the game state.

![The actor hierarchy](img/actor-hierarchy.png "The actor hierarchy")

The game loop is driven by a scheduled message `Tick` which is sent to the main `Game`actor 20 times pr second, and `Game`'s main task is to send a `GameStateDto`to the `GUI`.

There are probably many ways to organize the actors and still get a working game. We have chosen to have a quite clean communcation interface between the `GUI` and the `Game`; `GUI` can send `Start`, `MoveLeft`, `MoveRight` and `Fire` to `Game`, and `Game` only sends `GameStateDto` back to the `GUI`.

`GameStateDto` is an object that contains a complete view of the current state of the game, and the different parts of our actor hierarchy is responsible for providing different parts of it to the `Game` actor, which collects the parts and sends the total picture to the `GUI`actor.

Communication between other actors mainly occurs between a parent and its children. This approach gives only one entry point between the gui and the game logic, which makes it easy to have full control over when the game is active, or ended and all the moving parts have to stop. The global `Tick`makes it easy handle speed, and also the aliens move in a synchronized way so they can act entirely on their own. 

![The message flow](img/message-flow.png "The flow of messages")

Below are detailed instruction that gradually will make the game work. You can either follow them, or if you want, you can go more "free style" and make the actor system as you like, as long as the `GUI` actor receives the game state as specified in `GameStateDto` it should still work.

## A very tiny quick guide to Akka
Here is a very short summary of the Akka and actor basics you might need for the workshop. You can skip it for now and get back to it if you don't find the answer in the task desciption. And also, a much better place to look is in the real [Akka documentation](https://doc.akka.io/docs/akka/current/index.html)!

An **Actor** recieves messages, creates other actors, have its internal state. and lives in a hierarchy of actors in an actor system. An actor inherits the class `AbstractActor`, and contains the following important methods
*  `getSelf()` - `ActorRef` to itself                         
* `getSender()` - `ActorRef`to the currently processed message 
* `getContext()` - the actor context

The **Context** gives contextual information for the actor, like `getChildren()`, `getParent()` and `getSystem()`.  Actors are often created from another actor's context with `getContext().actorOf()`, and the new actor will then be a child of that actor. The constructor of an actor is not used directly, instead each actor class should have a static method returning [`Props`](https://doc.akka.io/docs/akka/current/actors.html#recommended-practices) for its own instantiation. `Props` are created with `Props.create(<MyClass>.class, () -> new <MyClass>(args))`. 

When an actor is created you get hold of an `ActorRef`, which is your reference to the actor and everything you need to send messages to that actor. The `ActorRef` can safely be contained in messages sent to other actors, and it is a common way to introduce an actor to another.

A **Message** can be sent to an actor by invoking the `ActorRef`'s `tell` method. The first argument is the message and the second is the `ActorRef` of the sender, often `getSelf()`. If you just want to forward a message you can istead use `forward`, which will keep the original sender. It is a good thing to create a message as static inner classes of the actor that will receive those messages. 

The **receiving** of messages in an actor is defined in the `createReceive()` method that all actors have to implement. The `Receive` object is created with a `receiveBuilder()`, which is build by adding a `match` for each message type the actor should respond to, like `receiveBuilder().match(<MyMessage>.class, msg -> {}).build()`. 

The actor can behave like a state machine, by defining different states in terms of `Receive` objects, where different `Recieve`s can respond to different messages, or respond differently to the same message. The transition from one `Receive` to another is called **become**, and within one `Receive` one can move to the next state by invoking `getContext().become(<NextReceive>)`. The `createReceive` method should return the state the actor should start with.

## Getting started
To do this workshop you should have the following installed on your computer:
* [Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) version >= 9
  * If you use OpenJDK of version >= 11, the gui library JavaFX is no longer included, and a separate dependency must be added to the `pom.xml`, take a look at the [required change](https://github.com/heidisu/actor-invaders/commit/6f3cba41e00c7143de940a40cd4c4d82a35da514) in the `java-11` branch.
* [Maven](https://maven.apache.org/)
* A nice editor, like [IntelliJ](https://www.jetbrains.com/idea/)

Clone (or download) this repo, and open the project in your editor. The editor probably knows how to run the project from the main class `App.java`. The code can also be build and run from command line with
```
mvn clean install
java -jar target/actor-invaders-1.0-SNAPSHOT-uber.jar
```

If you see a black screen with a start button when running the application, you are good to go!

## Task 1: Let the game begin
The actor `Game` is the main actor. It will receive messages from the `GUI` actor and from the message scehduler, and create and organize actors for handling the player, the aliens, and the bullets, and send new game state back to the `GUI`.
Initially `Game` has five message types; `Tick`, `Start`, `Fire`, `MoveLeft` and `MoveRight`. (We will add more later, and it will receive DTO objects as messages) 

The first message is the one that gets the game going. At every `Tick` the current game state is sent to the `GUI` actor. `Start` is received when the user clicks the "Start game" button, and should move `Game` into an active state. When the `Game`is in active state it should response to the commands `Fire`, `MoveLeft` and `MoveRight` from the player, and `Tick`from the scheduler.

![The different states of the Game](img/gamestate.png "The different states of the game")

Instead of having conditionals and flags to decide whether the actor should react to the the different messages or not, it will be better to keep the states clean and separate from each other. For that we will use the [`become`](https://doc.akka.io/docs/akka/2.5/actors.html) functionality to move between the states.
* In the `Game` actor make two methods that both return`Receive` objects, one for when the game has not yet started, and one for when the `Game` is playing, you can for instance call them `getIdle` and `getPlaying`.
  * `Receive` objects is made with `receiveBuilder()`. Add a `.match()` for each message that should be received, and finally `.build()` to get the `Receive`.
* The idle `Recieve` should only react to `Start` messages, and when it receive such a message it should create the `Player` actor, and then become the playing `Receive`.
  * The player actor can be created by`getContext().actorOf(Player.props(), "player")` You are in the context on the `Game` actor, so the player actor will be a child of the `Game` actor, with the name "player". 
  * You might want to save the player actorref in an instance variable so you have it for later
  * Maybe you also want to log that the game has started, so you really know. There is a `log` instance member use can use for that.
  * Finally, the `Game` should move on to playing state. That is acheived with `getContext().become()`.
* The second, the playing `Receive`, should *not* react to `Start`, but the other messages. 
  * When it recieves `Tick`it should tell the `GameStateDto` to the gui actor. The way to tell something to an actor is to use the `tell` method on an `actorRef` like `guiActor.tell(new GameStateDto(...), getSelf()))`. The bullets and aliens can just be empty lists for the time being. The `playerDto`is available as an instance variable, but it is immutable and thus safe to give away, the state can be `GameStateDto.State.Playing`.
  * The messages `MoveLeft` and `MoveRight` can be told further to the `Player`, and we will work with the player in the next session. (We will come back to `Fire`later).
* Make sure that the `createReceive()` method returns the receiver for the idle state.
* Start the application and see that you can click the start button and then is showed an empty, black screen. (Yeah, really exciting!)

## Task 2: Make the player move
Let us get the player into action! Both the player and the aliens are represented by images on the game screen. The screen has fixed size of `600 x 400` px, where `(0, 0)` is the upper left corner, and the position of an image is given by the coordinates of the image's upper left corner.

![The coordinate system](img/screen-coordinates.png "The coordinate system of the screen")

The `Player`is the actor for the state of actual players's cannon in the game, and it keeps its current position (`posX`, `posY`) and the remaining number of `lives` as its state variables. Every time the player move it will send a `PlayerDto` to its parent actor `Game` (and the `Game` will include that dto as a part of a `GameStateDto` at every `Tick`). 

* We will need to be able to make a `PlayerDto` quite often, so we can probably just create a method that makes one, and returns it. There is already a constant `image` that can be used as argument to the `PlayerDto`s constructor.
* Somewhere in the `Player` we should set the inital position for the cannon, it can be done in the constructor or just set the values where the instance variables are declared. A normal position would be in the middle, at the bottom of the screen. There are constants in the `Player` containing the screen size, which can be used in the calculation. We should also immediately send a `PlayerDto`back to the `Game`. The `Player` actor is a child of `Game`, so we can use `getContext().parent()` to get hold of the `Game` actorRef.
* In the `Player`'s `createReceive` add matches in the builder for the `Game.MoveLeft` and `Game.MoveRight` messages. In the action function in the match we should update `posX`. Experiment with what number you feel is a good speed, it can be 5. Maybe you also want to stop the player from moving outside the screen? A `PlayerDto` should also be sendt back to the `Game`.
* In `Game` we should receive the `PlayerDto` and update the instance variable.
* Start the game and see that you can move the player with the left and right arrows.

## Task 3: Firing bullets
There are many ways to think about the bullets and how they should be modelled in the system. Are they owned by the object firing them, or do they live separately from the object creating them? Here we will let the bullets live separate from the object triggering the creation of them. But we need some bookkeeping to be able to deliver a list of current bullets and their positions to the `GUI`actor. We will terminate bullets when they move outside the screen or when they hit another object. Thus, we will also make a `BulletManager` to keep track of the total set of bullets in the game. The bullets themselves are tiny actors that keep track of their own position, and stop themselves if they move outside the screen.

### The Bullet
The `Bullet`actor has three private field; an `id`, and the position `posX` and `posY`. To have an `id`  is useful for more effective updates of the `GUI`, and also the actor name has to be unqiue, so the id will be appended to the name when the bullets are created. The `posX`is never updated, the bullets move in straight lines. We will have two types of bullets, those fired by the player, and those fired by the aliens, but for now we will only think of bullets as fired from the player and moving upwards.
* Make a constructor in the `Bullet` that takes the in the three fields `id`, `posX` and `posY`, and sets the matching private fields. Add a static `props` method, that takes in the same three fields, and returns the props with `Props.create`.
* In the receive builder we will match for the `Tick`message.
  * Update the `posY`of the `Bullet`. Again, pick a suitable number, 10 might be the number.
  * If `posY`is outside the screen, we will stop the bullet. There are [several ways of stopping](https://doc.akka.io/docs/akka/2.5/actors.html#stopping-actors) an actor, depending on the what needs to be done when an actor stops. We can just go for the simple `getContext().stop(getSelf())`.
  * Otherwise, if the bullet it not outside the screen, we should create a `BulletDto` and tell that back to the sender (which should be the same as the parent). The sender for this bullet is `BulletDto.Sender.Player`.

### The BulletManager
The `BulletManager` will create new bullets and keep track of the `BulletDto` it receives. On every tick it will send the tick further down to each bullet, and send the current list of `BulletDto`s back to `Game`. Note that the manager will not need to wait for the bullets to update their position before it sends the list of dtos back to the `Game`. It just happily send the current situation, so the updates from the `Bullet`actors will take effect in a later tick.
* Create a static props method for the `BulletManager`. It does not need any arguments, and we don't need to make a constructor either.
* The manager needs a new message type for telling it to make new bullets. Make an inner static class for this message, it might be called `CreateBullet`, and it should take two arguments, the x- and y-coordinate of the position where the bullet should be created.
* We need some structure to keep order in the set of bullets. The manager already got a `refToBullet` for that, which can be used to store the `BulletDto`s it receives from the `Bullet`actors. You can of course use other structures for the bookkeeping if you'd like.
* The `receiveBuilder`should have matches for `Tick`, `CreateBullet`, `BulletDto`and `Terminated`. The last message is a special one that comes from the lifecycle monitoring in Akka. Any actor can `watch` any other actor, and if the watched actor stops, a `Terminated` message is sent to the watcher. 
  * When the manager receives a `CreateBullet`it should create a `Bullet` actor, start watching it by calling `getContext().watch(bullet)`. There is an instance variabel `nextId`that can be used for setting the id and incremented afterwards. The id should also be a part of the actor name when bullet is created.
  * When the `Tick` message is received the manager should send the `Tick`further to all the bullets, and send the bulletDtos of `refToBullet.values()` back to `Game`. The `BulletManager` can get hold of all the bullets by using `getContext().getChildren()`. Create a new message type in`Game` for receiving the list of `bulletDto`s, it could be called `Bullets`. To make sure that we do not share mutable state out, the list of bullets should be put in a new list, it should even be in a `Collections.unmodifiableList`.
  * On `BulletDto` messages it should just put the sender of the message and the dto in the `refToBullet`.
  * When a `Bullet`stops, the manager receives a `Terminated` message. The message has a `getActor()`that will give the `ActorRef`of the stopped actor. The manager should remove this stopped bullet from the `refToBullet`.

### The Player
The `Player`needs to responsible for firing its own bullets, also because the start position of the bullet depends on the current position of the `Player`. The `Player` doesn't know about the `BulletManager`, but it can get to know it by including the manager in messages sent to the `Player`.
* Make a new message type in the `Player` class, for instance called `Fire`, which has an `ActorRef` as an instance member.
* Add a new match in the receiveBuilder that maches on this new `Fire` message. When such a message is receive the `Player` should tell the actorRef in the message to `CreateBullet`.


### Putting the pieces together
Now we have most of the pieces ready to fire bullets, we only need to put the pieces together in the `Game` actor.
* Create a `BulletManager`, and keep a reference for it. It can for instance be created when the `Start` message is received.
* In the `playing` state of the actor, add a match for the `Fire` message. When this message is received, the actor should tell the `Player` the fire message we made in the `Player` class, where an reference to the `BulletManager` is added.
* In the same state it should also add a match for its `Bullets` message. When it receives this it should keep the list of bullets inside the message. 
* In the `Tick` match where the `GameStateDto` is sent to the `GUI`, the list of bullets in the previous step should be added. The `Tick`should also be sent down to the `BulletManager`.
* Start the game and see that the player now can fire bullets by pushing space. Well done!

## Task 4: Organize the aliens
You can of course organize the aliens and have as many of them as you like, below are instructions to make the aliens appear as shown in the gif.

The aliens are organized in a grid of 4 x 10 aliens, where bullets are fired randomly from one of the columns where there still are aliens left. The bullet should then be fired from the lowermost alien in that column. The aliens all has a width of 40 px, and can be evenly distributed on the a screen of width 600 with 20 pixels between the aliens, in all directions.

![The grid of aliens](img/alien-grid.png "The grid of aliens")

### The Alien
The `Alien` actor keep track of its current position, and its current image, since the aliens alternate between two images. It also need some logic for moving to the right for some time, and then move to the left, and the back again, and for alternating the images.
* Add a constructor and a static `props` method that takes integers `id`, `posX`, `posY`, and `imageSet` of type `AlienImageSet`, and make and set corresponding private fields.
* Make a new message type similar to the one we made for the `Player`. It can be called `Fire` and should take a `BulletManager`actorRef as constructor argument.
* Add matches in the receiveBulder for `Tick` and `Fire`.
  * On `Tick` the alien should move, and the send  an `AlienDto` message to its parent. Some logic is needed for moving first right, then left, and then to the right again. And to alternate between the two images. An easy approach can be to just keep some internal counters which are incremented until some limit is reached and the direction and the image are changed, respectively, and the counter is reset.
  * On `Fire` the `Alien` should tell `CreateBullet` to the bulletManager. The `AlienImageSet` has fields for height and width that might be useful for centering the position of the bullet.


### The AlienManager
The `AlienManager` has some similarities with the `BulletManager`, it creates all the `Alien` actors, and watch them so that it can remove dead aliens. The manager receives `AlienDto` messages from aliens, and sends a current list of `AlienDto`back to `Game` at each `Tick`. The aliens are organized in a grid, and the manager is responsible for making a random alien fire a bullet now and then.
* Make a constructor and static `props` method. Both should take an `ActorRef` for the `BulletManager` as argument.
* The grid of aliens can be initialized in the constructor
  * Use for instance a double for loop, and add actorRefs to the manager's grid variable.
  * Use the three different image sets defined in `AlienImageSet` so that aliens on same row has same image, and the rows alternates between different images.
  * The aliens should be watched by the manager
* The manager should respond to messages of type `Tick`, `AlienDto`and `Terminated`
  * On `Tick`it should decide if it want to fire a random bullet. Perhaps nice to have a separate method for firing the bullet, and the method should randomly choose one of the lowermost aliens from each column (if the column still has aliens left), and tell the selected `Alien` to `Fire`. You probably don't want to fire a bullet at every `Tick`, then it feels like it's raining bullets. 
  * On `Tick`the manager should also tell all the aliens to tick, and send the `AlienDto`s back to the game, in `Game` we should add a corresponding match which save the aliens in `Game`'s instance variable `aliens`.
  * When `AlienDto`is received, the manager should update the `refToAlien` map.
  * When `Terminated`is recieved, the dead alien should be removed from all the places it is kept in instance variables.

### The Bullet and the BulletManager
Now the `BulletManager` will receive `CreateBullet`messages from two different senders; from the `Player`and from the `AlienManager`. It therefore needs to create two different kinds of bullets, one of with sender player which are moving upwards, and one with sender alien which is mowing downwards. 
* Decide what you want to do with the `Bullet`actor in order to create bullets of these two kinds. Maybe you want to make it into an abstract base class with two sub classes, one for each bullet type, or maybe just separate the different logic inside the same class by using the existing enum in `BulletDto`, or something else.
* The `BulletManager` should then be responsible for creating a `Bullet` with the right properties. But how can it know which type of `Bullet`it should make? Again there are choices. The manager can use the name of the sender of the `CreateBullet` message to deduce what `Bullet`it should make, or we can extend the `CreateBullet` message to contain information that can be used to decide. In the first case the `BulletManager`is in control of what kind of bullets it want to make, in the latter, the sender of the message controls the decision.

### The Game
In `Game`create the `AlienManager`, and send `Tick`also to the `AlienManager`.

## Task 5: it's a war!
We are actually pretty close to something that behaves like a game! 

The main remaining part is to detect when the player or the aliens are hit by bullets. If the player is hit it should loose a life, and if there is no lives left, the game is lost. When an alien is hit it should be removed, and if there are no aliens left the game is won. When a bullet hits something it should disappear from the screen.

Obviously we need a way for the player and aliens to figure out when they are hit by bullets. The player and the aliens know their own widths and heights, so it is practical to let those entities decide if they are hit by a bullet or not. But we might not want the entities to keep a list of references to the bullets, which also are continously created and removed, or for a bullet to have a list of the entities, since these objects are not directly related in our actor hierarchy. But there is another way; the [Event Bus](https://doc.akka.io/docs/akka/current/event-bus.html). We will let the entities subscribe to bullets, and take the right action if they are hit. One can make a dedicated event bus, but we will just use the main bus for the actor system, the Event Stream.

In the class `Events` there are two events, one for when an bullet fired from an alien, and one for when a bullet is fired from the player. We will let the player subscribe to `AlienBulletMoved` and aliens subscribe to `PlayerBulletMoved`

* Start by making bullets publish the right message on the event stream each time they move. It is done my invoking `context().system().eventStream().publish()`. Messages published on the event stream does not have its original sender, that is why the actorRef for the bullet is included in the messsage.
* When the `Player` is created, it should also start to subscribe for `AlienBulletMoved` messages, and have a match clause for such messages in its receive builder. If the position of the `BulletDto` is within the area occupied by the player, the player should loose one life, and also stop the bullet actor. 
* Similar with the aliens. They should subscribe to the `PlayerBulletMoved` message, and stop both itself and the bullet when it is hit by the bullet. 
* Now we only have to update the state in `Game`. We should update the gui with state gameWon if there are noe aliens left, and similar gameLost if `Game`receives a `PlayerDto`with no lives left. You can choose if you want the logic for this in `Game`or if you want the `Player` and `AlienManager` tell the `Game` when those things happen. To make everthing stop when the game is either won or lost, we can make a new reiceve method for game over, and then let the `Game` become game over.

<p align="center">
  :tada: <b>Congratulations! You did it!</b> :tada:
 </p>

## Bonus task

Do you want to see how easy it is for actors to communicate with remote actors?

Let us split our actor system in two parts. If you look at the actor hierarchy diagram in the introduction you will see our three top level actors, the `GUI`, the `Game` and the `GameIntializer`. We will run the `GUI`actor in one application and keep the `GameIntializer`and the `Game`, with all its child actors, in another, and still be able to play the game. The applications can both be run on your computer, or team up with someone and run one application each.

### Serialization

Everything that will be sent between the applications have to be serializable and implement the `Serializable` interface. The `GameStateDto` and the things related to game initialization are already serializable, the things left are the messages sent to `Game` from the `GUI` actor. Go to the `Game` class and make sure that all the messages `Start`, `Fire`, `MoveLeft` and `MoveRight` (and others if you have made any yourself) implements `Serializable`.

### The Game application

We will now make a jar for the `Game` part. We will have to update the `application.conf` file to enable remoting, and to specify ip and port for the application. This is done by adding the follwing config to the file.
```
akka {
    actor {
        provider = remote
    }

    remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "127.0.0.1"
          port = 2552
        }
     }
}
```

The things to notice here are the values for `hostname` and `port`. Update the host name with a reachable ip if you will run the applications on different computers, otherwise the configuration can be kept as it is.

We then have to change our `App.java`. The game application will just create an actor system, and the game initializer actor, comment out the line where the gui actor is created, and the last line where the `Initialize` message is sent to the gameIntializer.

Build the application with maven, and get hold of the `target/actor-invaders-1.0-SNAPSHOT-uber.jar`. Copy it somewhere else, and rename it so you know it is the application with the game part.


### The GUI application

The gui application need the similar addition in `application.conf`, but change the port to something else, and update the hostname if it will communicate with a different computer.

In `App.java` we will create the actor system as before, but comment in the creation of the `GUI` actor, and comment out the creation of the `GameInitializer`. The application needs to get hold of the `GameInitializer` who lives in the remote system. It can do that by using actor selection in the following way:
```
ActorSelection gameInitializer = system.actorSelection("akka.tcp://space-invaders@127.0.0.1:2552/user/game-initializer");
```
Note that the host and port in the selection path must match what you configured for the game application. Then one can send the `Initialize` message to the gameInitializer, as before in the last line.

### Run the applications

Start the game application first, and then the gui, and, hey, everything works as before!
