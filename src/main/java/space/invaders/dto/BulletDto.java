package space.invaders.dto;

import java.io.Serializable;
import java.util.Objects;

public class BulletDto implements Serializable {
    public enum Sender {
        Player,
        Alien
    }

    public final int id;
    public final int posX;
    public final int posY;
    public final Sender sender;


    public BulletDto(int id, int posX, int posY, Sender sender) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.sender = sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulletDto bulletDto = (BulletDto) o;
        return id == bulletDto.id &&
                posX == bulletDto.posX &&
                posY == bulletDto.posY &&
                sender.equals(bulletDto.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, posX, posY, sender);
    }
}
