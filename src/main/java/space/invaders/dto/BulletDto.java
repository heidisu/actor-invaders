package space.invaders.dto;

import space.invaders.gamestate.Side;

import java.io.Serializable;
import java.util.Objects;

public class BulletDto implements Serializable {
    public final int id;
    public final int posX;
    public final int posY;
    public final Side side;


    public BulletDto(int id, int posX, int posY, Side side) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.side = side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulletDto bulletDto = (BulletDto) o;
        return id == bulletDto.id &&
                posX == bulletDto.posX &&
                posY == bulletDto.posY &&
                side.equals(bulletDto.side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, posX, posY, side);
    }
}
