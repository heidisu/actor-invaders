package space.invaders.dto;

import java.io.Serializable;
import java.util.Objects;

public class AlienDto implements Serializable {
    public final int id;
    public final int posX;
    public final int posY;
    public final Image image;

    public AlienDto(int id, int posX, int posY, Image image) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlienDto alienDto = (AlienDto) o;
        return id == alienDto.id &&
                posX == alienDto.posX &&
                posY == alienDto.posY &&
                image.equals(alienDto.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, posX, posY, image);
    }
}