package space.invaders.gamestate;

import space.invaders.dto.Image;

import java.util.List;

class AlienImageSet {
    private final int width, height;
    private final Image first;
    private final Image second;

    private AlienImageSet(int width, int height, String imagePath1, String imagePath2) {
        this.width = width;
        this.height = height;
        this.first = new Image(width, height, imagePath1);
        this.second = new Image(width, height, imagePath2);
    }

    Image getFirst(){
        return first;
    }

    Image getOther (Image currentImage){
        return first.equals(currentImage) ? second : first;
    }

    int getWidth(){
        return width;
    }

    int getHeight(){
        return height;
    }


    private static AlienImageSet alien1 = new AlienImageSet(40, 40* 224/308, "img/alien1-closed.png", "img/alien1-open.png");
    private static AlienImageSet alien2 = new AlienImageSet(40, 40* 224/336, "img/alien2-closed.png", "img/alien2-open.png");
    private static AlienImageSet alien3 = new AlienImageSet(40, 40* 224/252, "img/alien3-closed.png", "img/alien3-open.png");

    static List<AlienImageSet> images = List.of(alien1, alien2, alien3);
}
