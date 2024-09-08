package tthcc.zhaorc.play.pixel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by zhaoricheng@jd.com at 2024/05/17 15:41
 */
@Slf4j
public class Image2Pixel {

    /**
     * @param blockConfig
     */
    public void convertPixel(BlockConfig blockConfig) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(blockConfig.getFilename())) {
            BufferedImage bufferedImage = ImageIO.read(fileInputStream);
            int width = (bufferedImage.getWidth() - blockConfig.getOffsetLeft() - blockConfig.getOffsetRight()) / blockConfig.getMergeX() * blockConfig.getMergeX();
            int height = (bufferedImage.getHeight() - blockConfig.getOffsetTop() - blockConfig.getOffsetBottom()) / blockConfig.getMergeY() * blockConfig.getMergeY();
            BufferedImage originImageStream = bufferedImage.getSubimage(blockConfig.getOffsetLeft(), blockConfig.getOffsetTop(), width, height);
            //合并像素
            List<Color> colorList = this.mergePixel(blockConfig, originImageStream);
            this.makePixelFile(blockConfig, colorList);
            this.makePartsFile(blockConfig, colorList);
            this.makePaperFile(blockConfig, colorList);
            this.makeIndexFile(blockConfig);
            //XXX
            System.out.println("colors=" + colorList.stream().distinct().count());
        }
    }

    /**
     * 合并像素，排除掉数量较少的颜色
     *
     * @param blockConfig
     * @param originImage
     * @throws Exception
     */
    private List<Color> mergePixel(BlockConfig blockConfig, BufferedImage originImage) throws Exception {
        List<Color> colorList = Lists.newArrayList();
        int blocksX = originImage.getWidth() / blockConfig.getMergeX();
        int blocksY = originImage.getHeight() / blockConfig.getMergeY();
        int mergeNum = blockConfig.getMergeX() * blockConfig.getMergeY();
        Map<String, Integer> colorNumMap = Maps.newHashMap();
        BufferedImage pixelImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        int rgb = 0;
        int r = 0, g = 0, b = 0;
        int blockR = 0, blockG = 0, blockB = 0;
        while (true) {
            colorList.clear();
            colorNumMap.clear();
            //合并
            for (int y = 0; y < blocksY; y++) {
                for (int x = 0; x < blocksX; x++) {
                    //均值
                    blockR = blockG = blockB = 0;
                    for (int i = 0; i < blockConfig.getMergeX(); i++) {
                        for (int j = 0; j < blockConfig.getMergeY(); j++) {
                            rgb = originImage.getRGB(i + x * blockConfig.getMergeX(), j + y * blockConfig.getMergeY());
                            b = (rgb & 0xff);
                            g = ((rgb & 0xff00) >> 8);
                            r = ((rgb & 0xff0000) >> 16);
                            Color color = this.getColor(blockConfig, r, g, b);
                            blockB += color.getB();
                            blockG += color.getG();
                            blockR += color.getR();
                        }
                    }
                    blockB /= mergeNum;
                    blockG /= mergeNum;
                    blockR /= mergeNum;
                    Color color = this.getColor(blockConfig, blockR, blockG, blockB);
                    colorList.add(color);
                    int num = colorNumMap.getOrDefault(color.getName(), 0);
                    colorNumMap.put(color.getName(), num + 1);
                    rgb = ((color.getR() << 16) & 0xff0000) + ((color.getG() << 8) & 0xff00) + (color.getB() & 0xff);
                    for (int i = 0; i < blockConfig.getMergeX(); i++) {
                        for (int j = 0; j < blockConfig.getMergeY(); j++) {
                            pixelImage.setRGB(x * blockConfig.getMergeX() + i, y * blockConfig.getMergeY() + j, rgb);
                        }
                    }
                }
            }
            //排除数量少的颜色
            int checkNum = blockConfig.getMinColorNum();
            List<String> removeList = colorNumMap.entrySet().stream().filter(o -> o.getValue() < checkNum).map(Map.Entry::getKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(removeList)) {
                break;
            }
            blockConfig.getColorList().removeIf(o -> removeList.contains(o.getName()));
        }
        //保存
        int idx = blockConfig.getFilename().lastIndexOf(".");
        String filetype = blockConfig.getFilename().substring(idx);
        String originImageFileName = blockConfig.getFilename().replace(filetype, "_01_pixel") + filetype;
        String pixelImageFileName = blockConfig.getFilename().replace(filetype, "_99_pixel") + filetype;
        try (FileOutputStream originImageOutputStream = new FileOutputStream(originImageFileName);
             FileOutputStream pixelImageOutputStream = new FileOutputStream(pixelImageFileName)) {
            ImageIO.write(originImage, "png", originImageOutputStream);
            ImageIO.write(pixelImage, "png", pixelImageOutputStream);
        }
        blockConfig.setBlocksX(blocksX);
        blockConfig.setBlocksY(blocksY);
        return colorList;
    }

    /**
     * @param blockConfig
     * @param r
     * @param g
     * @param b
     * @return
     */
    private Color getColor(BlockConfig blockConfig, int r, int g, int b) {
        Color result = null;
        double distance = 3 * 255 * 255;
        for (Color color : blockConfig.getColorList()) {
            double d = (r - color.getR()) * (r - color.getR()) + (g - color.getG()) * (g - color.getG()) + (b - color.getB()) * (b - color.getB());
            if (d < distance) {
                distance = d;
                result = color;
            }
        }
        return result;
    }

    /**
     * @param blockConfig
     * @param colorList
     * @throws Exception
     */
    private void makePixelFile(BlockConfig blockConfig, List<Color> colorList) throws Exception {
        int idx = blockConfig.getFilename().lastIndexOf("/");
        String filename = blockConfig.getFilename().substring(idx);
        filename = blockConfig.getFilename().replace(filename, "/web" + filename);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        String pixelFilename = filename.replace(filetype, "_pixel.html");
        List<String> lineList = Lists.newArrayList();
        lineList.addAll(blockConfig.getHeaderLineList());
        lineList.add("<body>");
        lineList.add(String.format(blockConfig.getPixelTableTpl(), blockConfig.getBlocksX() > blockConfig.getBlocksY() ? "pixel_table_w" : "pixel_table_h"));
        lineList.add("<tr>");
        for (int i = 0; i < colorList.size(); i++) {
            if (i > 0 && i % blockConfig.getBlocksX() == 0) {
                lineList.add("</tr>");
                lineList.add("<tr>");
            }
            lineList.add(String.format(blockConfig.getPixelTdTpl(), colorList.get(i).getName()));
        }
        lineList.add("</tr>");
        lineList.add("</table>");
        lineList.add("</body>");
        lineList.remove(5); //viewport
        this.writeTextFile(lineList, pixelFilename);
    }

    /**
     * 图纸
     *
     * @param blockConfig
     * @param colorList
     * @throws Exception
     */
    private void makePaperFile(BlockConfig blockConfig, List<Color> colorList) throws Exception {
        int idx = blockConfig.getFilename().lastIndexOf("/");
        String filename = blockConfig.getFilename().substring(idx);
        filename = blockConfig.getFilename().replace(filename, "/web" + filename);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        int cols = blockConfig.getBlocksX() / blockConfig.getPaperBlocks();
        int rows = blockConfig.getBlocksY() / blockConfig.getPaperBlocks();
        cols += blockConfig.getBlocksX() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += blockConfig.getBlocksY() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;
        List<String> paperLineList = Lists.newArrayList();
        List<Color> paperColorList = Lists.newArrayList();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                paperLineList.clear();
                paperColorList.clear();
                String paperFilename = filename.replace(filetype, String.format("_paper_%02d%02d.html", row, col));
                paperLineList.add(blockConfig.getPaperTableTpl());
                //序号行
                paperLineList.add("<tr>");
                paperLineList.add(String.format(blockConfig.getPageCornerTpl(), ""));
                for (int x = 0; x < blockConfig.getPaperBlocks(); x++) {
                    if (col * blockConfig.getPaperBlocks() + x >= blockConfig.getBlocksX()) {
                        continue;
                    }
                    paperLineList.add(String.format(blockConfig.getPageColTpl(), col * blockConfig.getPaperBlocks() + x));
                }
                paperLineList.add("</tr>");
                for (int y = 0; y < blockConfig.getPaperBlocks(); y++) {
                    if (row * blockConfig.getPaperBlocks() + y >= blockConfig.getBlocksY()) {
                        continue;
                    }
                    if (y > 0) {
                        paperLineList.add("</tr>");
                    }
                    paperLineList.add("<tr>");
                    paperLineList.add(String.format(blockConfig.getPageRowTpl(), row * blockConfig.getPaperBlocks() + y));
                    for (int x = 0; x < blockConfig.getPaperBlocks(); x++) {
                        if (col * blockConfig.getPaperBlocks() + x >= blockConfig.getBlocksX()) {
                            continue;
                        }
                        int offset = (row * blockConfig.getPaperBlocks() + y) * blockConfig.getBlocksX() + col * blockConfig.getPaperBlocks() + x;
                        paperColorList.add(colorList.get(offset));
                        paperLineList.add(String.format(blockConfig.getBlockTdTpl(), colorList.get(offset).getName()));
                    }
                }
                paperLineList.add("</tr>");
                paperLineList.add("</table>");
                paperLineList.add("</body>");

                List<String> lineList = Lists.newArrayList();
                this.addHeaderLinesToFile(blockConfig, lineList);
                lineList.add(this.makePaperPartsBoxLine(blockConfig, paperColorList));
                lineList.add("</head>");
                lineList.add("<body>");
                lineList.add(String.format(blockConfig.getTitleTpl(), String.format("%02d%02d", row, col)));
                lineList.add(blockConfig.getPartsButtonTpl());
                lineList.addAll(this.makePartsFileLineList(blockConfig, paperColorList));
                lineList.add(this.makeActionTableLine(blockConfig, row, col));
                lineList.addAll(paperLineList);
                this.writeTextFile(lineList, paperFilename);
            }
        }
    }

    /**
     * 图纸零件盒
     *
     * @param blockConfig
     * @param paperColorList
     * @return
     */
    private String makePaperPartsBoxLine(BlockConfig blockConfig, List<Color> paperColorList) {
        List<String> colorNameList = paperColorList.stream().collect(Collectors.groupingBy(Color::getName)).keySet().stream().sorted().collect(Collectors.toList());
        List<String> partsBox = Lists.newArrayList();
        for (int i = 0; i < colorNameList.size(); i++) {
            int row = i / blockConfig.getPartsBoxX();
            int col = i % blockConfig.getPartsBoxX();
            partsBox.add(String.format("\"%s\":\"%s-%s\"", colorNameList.get(i), row + 1, col + 1));
        }
        return "<script>var partsBoxMap = {" + Joiner.on(",").join(partsBox) + "};</script>";
    }

    /**
     * @param blockConfig
     * @param row
     * @param col
     * @return
     */
    private String makeActionTableLine(BlockConfig blockConfig, int row, int col) {
        int idx = blockConfig.getFilename().lastIndexOf("/");
        String filename = blockConfig.getFilename().substring(idx + 1);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);

        int cols = blockConfig.getBlocksX() / blockConfig.getPaperBlocks();
        int rows = blockConfig.getBlocksY() / blockConfig.getPaperBlocks();
        cols += blockConfig.getBlocksX() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += blockConfig.getBlocksY() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;

        //上一页
        int row_0 = row, col_0 = col - 1;
        if (col_0 < 0) {
            col_0 = row == 0 ? 0 : cols - 1;
            row_0 = row - 1;
        }
        row_0 = row_0 < 0 ? 0 : row_0;
        col_0 = col_0 < 0 ? 0 : col_0;
        //下一页
        int row_1 = row, col_1 = col + 1;
        if (col_1 >= cols) {
            col_1 = row == rows - 1 ? col : 0;
            row_1 = row_1 + 1;
        }
        row_1 = row_1 >= rows ? rows - 1 : row_1;
        col_1 = col_1 >= cols ? cols - 1 : col_1;
        String indexFilename = "index.html";
        String prePaperFilename = filename.replace(filetype, String.format("_paper_%02d%02d.html", row_0, col_0));
        String nextPaperFilename = filename.replace(filetype, String.format("_paper_%02d%02d.html", row_1, col_1));
        return String.format(blockConfig.getActionTableTpl(), indexFilename, prePaperFilename, nextPaperFilename);
    }

    /**
     * @param blockConfig
     */
    private void makeIndexFile(BlockConfig blockConfig) throws Exception {
        int idx = blockConfig.getFilename().lastIndexOf("/");
        String filename = blockConfig.getFilename().substring(idx + 1);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        String indexFilename = blockConfig.getFilename().replace(filename, "/web/index.html");
        int cols = blockConfig.getBlocksX() / blockConfig.getPaperBlocks();
        int rows = blockConfig.getBlocksY() / blockConfig.getPaperBlocks();
        cols += blockConfig.getBlocksX() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += blockConfig.getBlocksY() % blockConfig.getPaperBlocks() == 0 ? 0 : 1;

        List<String> lineList = Lists.newArrayList();
        this.addHeaderLinesToFile(blockConfig, lineList);
        lineList.add("</head>");
        lineList.add(blockConfig.getIndexBodyTpl());
        lineList.add("<table>");
        lineList.add(String.format(blockConfig.getTitleTpl(), "目录"));
        lineList.add(String.format(blockConfig.getIndexTdTpl(), filename.replace(filetype, "_pixel.html"), "像素图"));
        lineList.add(String.format(blockConfig.getIndexTdTpl(), filename.replace(filetype, "_parts_00.html"), "零件表"));
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                lineList.add(String.format(blockConfig.getIndexTdTpl(), filename.replace(filetype, String.format("_paper_%02d%02d.html", row, col)), String.format("图纸-%02d%02d", row, col)));
            }
        }
        lineList.add("</table>");
        lineList.add("</body>");
        this.writeTextFile(lineList, indexFilename);
    }

    /**
     * @param blockConfig
     * @param colorList
     * @throws Exception
     */
    private void makePartsFile(BlockConfig blockConfig, List<Color> colorList) throws Exception {
        int idx = blockConfig.getFilename().indexOf(".");
        String filetype = blockConfig.getFilename().substring(idx);
        idx = blockConfig.getFilename().lastIndexOf("/");
        String filename = blockConfig.getFilename().substring(idx);
        String indexFilename = "index.html";
        filename = blockConfig.getFilename().replace(filename, "/web" + filename);
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        int total = 0;
        for (Map.Entry<String, List<Color>> entry : colorMap.entrySet()) {
            total += entry.getValue().size();
        }
        List<String> partsLineList = Lists.newArrayList();
        this.addHeaderLinesToFile(blockConfig, partsLineList);
        partsLineList.add("</head>");
        partsLineList.add("<body>");
        partsLineList.add(String.format(String.format(blockConfig.getIndexButtonTpl(), indexFilename)));
        partsLineList.add(String.format(blockConfig.getTitleTpl(), "零件总数：" + total));
        partsLineList.addAll(this.addToPackage(blockConfig, colorList));
        partsLineList.addAll(this.makePartsFileLineList(blockConfig, colorList));
        partsLineList.add("</body>");

        String partsFilename = filename.replace(filetype, "_parts_00.html");
        this.writeTextFile(partsLineList, partsFilename);
    }

    /**
     * @param blockConfig
     * @param colorList
     * @return
     */
    private List<String> makePartsFileLineList(BlockConfig blockConfig, List<Color> colorList) {
        List<String> lineList = Lists.newArrayList();
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        List<String> nameList = colorMap.keySet().stream().sorted().collect(Collectors.toList());
        int rows = nameList.size() / blockConfig.getPartsRowNum();
        rows += nameList.size() % blockConfig.getPartsRowNum() == 0 ? 0 : 1;
        lineList.add(blockConfig.getPartsTableTpl());
        for (int i = 0; i < rows; i++) {
            lineList.add("<tr>");
            for (int j = 0; j < blockConfig.getPartsRowNum(); j++) {
                String colorName = "";
                int index = i * blockConfig.getPartsRowNum() + j;
                if (index < nameList.size()) {
                    colorName = nameList.get(index);
                }
                lineList.add(String.format(blockConfig.getPartsColTpl(), colorName));
            }
            lineList.add("</tr>");
            lineList.add("<tr>");
            for (int j = 0; j < blockConfig.getPartsRowNum(); j++) {
                int colorNum = 0;
                int index = i * blockConfig.getPartsRowNum() + j;
                if (index < nameList.size()) {
                    colorNum = colorMap.get(nameList.get(index)).size();
                }
                lineList.add(String.format(blockConfig.getPartsRowTpl(), colorNum));
            }
            lineList.add("</tr>");
        }
        lineList.add("</table>");
        return lineList;
    }

    /**
     * @param lineList
     * @param filename
     * @throws Exception
     */
    private void writeTextFile(List<String> lineList, String filename) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(filename))))) {
            for (String line : lineList) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * @param blockConfig
     * @param lineList
     */
    private void addHeaderLinesToFile(BlockConfig blockConfig, List<String> lineList) {
        lineList.addAll(blockConfig.getHeaderLineList());
    }

    /**
     * @param blockConfig
     * @param colorList
     * @return
     */
    private List<String> addToPackage(BlockConfig blockConfig, List<Color> colorList) {
        Map<String, List<String>> packageMap = Maps.newHashMap();
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        for (Map.Entry<String, List<Color>> entry : colorMap.entrySet()) {
            this.addToPackage(blockConfig, entry.getKey(), entry.getValue().size(), packageMap);
        }
        int price = 0;
        for (int i = 0; i < blockConfig.getPackageName().length; i++) {
            price += blockConfig.getPackagePrice()[i] * packageMap.get(String.valueOf(blockConfig.getPackageName()[i])).size();
        }
        List<String> lineList = Lists.newArrayList();
        lineList.add(String.format(blockConfig.getTitleTpl(), "价格：" + BigDecimal.valueOf(price).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        lineList.add(blockConfig.getPartsTableTpl());
        for (Map.Entry<String, List<String>> entry : packageMap.entrySet()) {
            String line = entry.getKey() + "粒（" + entry.getValue().size() + "）：" + Joiner.on(" ").join(entry.getValue());
            lineList.add("<tr>");
            lineList.add(String.format(blockConfig.getPackageRowTpl(), line));
            lineList.add("</tr>");
        }
        lineList.add("</table>");
        return lineList;
    }

    /**
     * @param blockConfig
     * @param colorName
     * @param colorNum
     * @param packageMap
     */
    private void addToPackage(BlockConfig blockConfig, String colorName, int colorNum, Map<String, List<String>> packageMap) {
        colorName = String.valueOf(Integer.parseInt(colorName));
        int biggest = blockConfig.getPackageName()[blockConfig.getPackageName().length - 1];
        for (int i = 0; i < blockConfig.getPackagePrice().length; i++) {
            if (colorNum <= blockConfig.getPackageName()[i]) {
                List<String> colorList = packageMap.computeIfAbsent(String.valueOf(blockConfig.getPackageName()[i]), k -> Lists.newArrayList());
                colorList.add(colorName);
                break;
            }
        }
        if (colorNum > biggest) {
            List<String> colorList = packageMap.computeIfAbsent(String.valueOf(biggest), k -> Lists.newArrayList());
            colorList.add(colorName);
            this.addToPackage(blockConfig, colorName, colorNum - biggest, packageMap);
        }
    }
}
