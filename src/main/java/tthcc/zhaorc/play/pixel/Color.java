package tthcc.zhaorc.play.pixel;

import lombok.Getter;

import java.util.Objects;

/**
 * created by zhaoricheng@jd.com at 2024/05/18 20:48
 */
@Getter
public class Color {

    private String name;
    private String hex;
    private int r = 0, g = 0, b = 0;

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color(String name, String hex) {
        this.name = name;
        hex = hex.replace("#", "");
        this.hex = hex;
        this.r = Integer.parseInt(hex.substring(0, 2), 16);
        this.g = Integer.parseInt(hex.substring(2, 4), 16);
        this.b = Integer.parseInt(hex.substring(4, 6), 16);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return Objects.equals(name, color.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
