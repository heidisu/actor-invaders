package space.invaders.dto;

import java.io.Serializable;
import java.util.Objects;

public class BulletDto implements Serializable {
    public final int id;
    public final int posX;
    public final int posY;
    public final String styleClass;


    public BulletDto(int id, int posX, int posY, String styleClass) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.styleClass = styleClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulletDto bulletDto = (BulletDto) o;
        return id == bulletDto.id &&
                posX == bulletDto.posX &&
                posY == bulletDto.posY &&
                styleClass.equals(bulletDto.styleClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, posX, posY, styleClass);
    }
}
