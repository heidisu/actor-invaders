package space.invaders.gamestate;

import java.util.List;

class AlienImage {
    final int width;
    final int height;
    final String imagePath1;
    final String imagePath2;

    private AlienImage(int width, int height, String imagePath1, String imagePath2) {
        this.width = width;
        this.height = height;
        this.imagePath1 = imagePath1;
        this.imagePath2 = imagePath2;
    }

    private static AlienImage alien1 = new AlienImage(40, 40* 224/308, "img/alien1-closed.png", "img/alien1-open.png");
    private static AlienImage alien2 = new AlienImage(40, 40* 224/336, "img/alien2-closed.png", "img/alien2-open.png");
    private static AlienImage alien3 = new AlienImage(40, 40* 224/252, "img/alien3-closed.png", "img/alien3-open.png");

    static List<AlienImage> images = List.of(alien1, alien2, alien3);
}
