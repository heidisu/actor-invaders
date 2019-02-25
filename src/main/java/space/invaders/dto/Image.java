package space.invaders.dto;

import java.io.Serializable;
import java.util.Objects;

public class Image implements Serializable {
    public final int width;
    public final int height;
    public final String imageUrl;

    public Image(int width, int height, String imageUrl) {
        this.width = width;
        this.height = height;
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return width == image.width &&
                height == image.height &&
                imageUrl.equals(image.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, imageUrl);
    }
}
