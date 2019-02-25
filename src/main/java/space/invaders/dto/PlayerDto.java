package space.invaders.dto;

import java.io.Serializable;
import java.util.Objects;

public class PlayerDto implements Serializable {
    public final int posX;
    public final int posY;
    public final int lives;
    public final Image image;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerDto playerDto = (PlayerDto) o;
        return posX == playerDto.posX &&
                posY == playerDto.posY &&
                lives == playerDto.lives &&
                image.equals(playerDto.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY, lives, image);
    }

    public PlayerDto(int posX, int posY, int lives, Image image) {
        this.posX = posX;
        this.posY = posY;
        this.lives = lives;
        this.image = image;
    }
}
