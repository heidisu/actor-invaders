package space.invaders.gamestate;

import java.util.List;

class AlienImageSet {
    final int width;
    final int height;
    final String imagePath1;
    final String imagePath2;

    private AlienImageSet(int width, int height, String imagePath1, String imagePath2) {
        this.width = width;
        this.height = height;
        this.imagePath1 = imagePath1;
        this.imagePath2 = imagePath2;
    }

    private static AlienImageSet alien1 = new AlienImageSet(40, 40* 224/308, "img/alien1-closed.png", "img/alien1-open.png");
    private static AlienImageSet alien2 = new AlienImageSet(40, 40* 224/336, "img/alien2-closed.png", "img/alien2-open.png");
    private static AlienImageSet alien3 = new AlienImageSet(40, 40* 224/252, "img/alien3-closed.png", "img/alien3-open.png");

    static List<AlienImageSet> images = List.of(alien1, alien2, alien3);
}
