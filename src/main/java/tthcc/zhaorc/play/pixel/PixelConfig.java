package tthcc.zhaorc.play.pixel;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class PixelConfig {

    private String filename;
    private String colorFilename;
    //合并几个像素
    private int mergeX;
    private int mergeY;
    //合并后的方块数量
    private int blocksX;
    private int blocksY;
    //图纸上方块的大小（像素）
    private int pageBlockSize = 10;
    //零件盒每行个数
    private int partsRowNum = 10;
    //图纸每行每列的方块数量
    private int paperBlocks = 20;
    //小于这个数量的颜色不使用
    private int minColorNum = 10;
    //零件盒的格子数
    private int partsBoxX = 6;

    private int offsetLeft;
    private int offsetRight;
    private int offsetTop;
    private int offsetBottom;

    private int[] packageName;
    private int[] packagePrice;

    private List<Color> colorList = Lists.newArrayList();

    public void addColor(Color color) {
        colorList.add(color);
    }

    /**
     * @param packageNamePrice
     */
    public void setPackagePrice(String packageNamePrice) {
        String[] v = packageNamePrice.split(";");
        packageName = new int[v.length];
        packagePrice = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            String[] pair = v[i].split(":");
            packageName[i] = Integer.parseInt(pair[0].trim());
            packagePrice[i] = Integer.parseInt(pair[1].trim().replace(".", ""));
        }
    }

    private List<String> headerLineList = Lists.newArrayList(
            "<!doctype html>",
            "<html xmlns:th=\"http://www.w3.org/1999/xhtml\">",
            "<head>",
            "<title>啊哈哈</title>",
            "<meta charset=\"utf-8\">",
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">",
            "<link rel=\"stylesheet\" href=\"../color2.css?v=0020\"/>",
            "<script src=\"../jquery-3.7.1.min.js\"></script>",
            "<script src=\"../pixel-app.js?v=0020\"></script>"
//            "<link rel=\"stylesheet\" href=\"/pixel/color2.css?v=0016\"/>",
//            "<script src=\"/pixel/jquery-3.7.1.min.js\"></script>",
//            "<script src=\"/pixel/pixel-app.js?v=0016\"></script>"
    );

    private String actionTableTpl =
            "<table class=\"action_table\">\n" +
                    "<tr>\n" +
                    "<td class=\"action_td_blank\"></td>\n" +
                    "<td class=\"action_td\"><button name=\"page\" type=\"button\" class=\"ui-button ui-button-bg\" _page=\"%s\">目录</button></td>\n" +
                    "<td class=\"action_td\"><button name=\"page\" type=\"button\" class=\"ui-button ui-button-bg\" _page=\"%s\">上页</button></td>\n" +
                    "<td class=\"action_td\"><button name=\"page\" type=\"button\" class=\"ui-button ui-button-bg\" _page=\"%s\">下页</button></td>\n" +
                    "<td class=\"action_td\"><button name=\"stepPre\" type=\"button\" class=\"ui-button ui-button-lg\">-</button></td>\n" +
                    "<td class=\"action_td\"><button name=\"stepNext\" type=\"button\" class=\"ui-button ui-button-lg\">+</button></td>\n" +
                    "</tr>\n" +
                    "</tr>\n" +
                    "</table>";

    private String partsButtonTpl = "<div class=\"parts-action\"><button name=\"nextBox\" type=\"button\" class=\"ui-button ui-button-sm\">+</button></div>";
    private String indexButtonTpl = "<button name=\"page\" type=\"button\" class=\"ui-button index_button\" _page=\"%s\">目录</button>";
    private String indexBodyTpl = "<body class=\"index_body\">";
    private String indexTdTpl = "<tr><td class=\"index_td\"><a href=\"%s\">%s</a></td></tr>";
    private String pixelTableTpl = "<table class=\"%s\">";
    private String pixelTdTpl = "<td class=\"color_%s\"></td>";
    private String pageTdTpl = "<td class=\"paper_td\">%s</td>";
    private String pageRowTpl = "<td class=\"row_td\">%s</td>";
    private String pageColTpl = "<td class=\"col_td\">%s</td>";
    private String pageCornerTpl = "<td class=\"corner_td\">%s</td>";
    private String partsTableTpl = "<table class=\"parts_table\">";
    private String paperTableTpl = "<table class=\"paper_table\">";
    private String partsRowTpl = "<td class=\"parts_row_td\">%s</td>";
    private String partsColTpl = "<td class=\"parts_col_td\">%s</td>";
    private String titleTpl = "<h3 class=\"title\">%s</h3>";
    private String packageRowTpl = "<td class=\"package_row_td\">%s</td>";

}
