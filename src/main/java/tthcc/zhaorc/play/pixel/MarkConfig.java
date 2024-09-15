package tthcc.zhaorc.play.pixel;

import lombok.Getter;
import lombok.Setter;

/**
 * created by zhaoricheng@jd.com at 2024/05/17 15:43
 */
@Setter
@Getter
public class MarkConfig extends PixelConfig {

    //图纸上方块的大小（像素）
    private int pageBlockSize = 10;
    //图纸每行每列的方块数量
    private int paperBlocks = 20;
    //小于这个数量的颜色不使用
    private int minColorNum = 0;
    //零件盒的格子数
    private int partsBoxX = 0;
    //零件盒每行个数
    private int partsRowNum = 0;
    //颜色表
    private String colorFilename = "/color_list_mark.txt";

    private String pixelTdTpl = "<td class=\"block mark_td mark_%s\"></td>";
}
