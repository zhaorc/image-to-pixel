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
     * @param pixelConfig
     */
    public void convertPixel(PixelConfig pixelConfig) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(pixelConfig.getFilename())) {
            BufferedImage bufferedImage = ImageIO.read(fileInputStream);
            int width = (bufferedImage.getWidth() - pixelConfig.getOffsetLeft() - pixelConfig.getOffsetRight()) / pixelConfig.getMergeX() * pixelConfig.getMergeX();
            int height = (bufferedImage.getHeight() - pixelConfig.getOffsetTop() - pixelConfig.getOffsetBottom()) / pixelConfig.getMergeY() * pixelConfig.getMergeY();
            BufferedImage originImageStream = bufferedImage.getSubimage(pixelConfig.getOffsetLeft(), pixelConfig.getOffsetTop(), width, height);
            //合并像素
            List<Color> colorList = this.mergePixel(pixelConfig, originImageStream);
            this.makePixelFile(pixelConfig, colorList);
            this.makePartsFile(pixelConfig, colorList);
            this.makePaperFile(pixelConfig, colorList);
            this.makeIndexFile(pixelConfig);
            //XXX
            System.out.println("colors=" + colorList.stream().distinct().count());
        }
    }

    /**
     * 合并像素，排除掉数量较少的颜色
     *
     * @param pixelConfig
     * @param originImage
     * @throws Exception
     */
    private List<Color> mergePixel(PixelConfig pixelConfig, BufferedImage originImage) throws Exception {
        List<Color> colorList = Lists.newArrayList();
        int blocksX = originImage.getWidth() / pixelConfig.getMergeX();
        int blocksY = originImage.getHeight() / pixelConfig.getMergeY();
        int mergeNum = pixelConfig.getMergeX() * pixelConfig.getMergeY();
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
                    for (int i = 0; i < pixelConfig.getMergeX(); i++) {
                        for (int j = 0; j < pixelConfig.getMergeY(); j++) {
                            rgb = originImage.getRGB(i + x * pixelConfig.getMergeX(), j + y * pixelConfig.getMergeY());
                            b = (rgb & 0xff);
                            g = ((rgb & 0xff00) >> 8);
                            r = ((rgb & 0xff0000) >> 16);
                            Color color = this.getColor(pixelConfig, r, g, b);
                            blockB += color.getB();
                            blockG += color.getG();
                            blockR += color.getR();
                        }
                    }
                    blockB /= mergeNum;
                    blockG /= mergeNum;
                    blockR /= mergeNum;
                    Color color = this.getColor(pixelConfig, blockR, blockG, blockB);
                    colorList.add(color);
                    int num = colorNumMap.getOrDefault(color.getName(), 0);
                    colorNumMap.put(color.getName(), num + 1);
                    rgb = ((color.getR() << 16) & 0xff0000) + ((color.getG() << 8) & 0xff00) + (color.getB() & 0xff);
                    for (int i = 0; i < pixelConfig.getMergeX(); i++) {
                        for (int j = 0; j < pixelConfig.getMergeY(); j++) {
                            pixelImage.setRGB(x * pixelConfig.getMergeX() + i, y * pixelConfig.getMergeY() + j, rgb);
                        }
                    }
                }
            }
            //排除数量少的颜色
            int checkNum = pixelConfig.getMinColorNum();
            List<String> removeList = colorNumMap.entrySet().stream().filter(o -> o.getValue() < checkNum).map(Map.Entry::getKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(removeList)) {
                break;
            }
            pixelConfig.getColorList().removeIf(o -> removeList.contains(o.getName()));
        }
        //保存
        int idx = pixelConfig.getFilename().lastIndexOf(".");
        String filetype = pixelConfig.getFilename().substring(idx);
        String originImageFileName = pixelConfig.getFilename().replace(filetype, "_01_pixel") + filetype;
        String pixelImageFileName = pixelConfig.getFilename().replace(filetype, "_99_pixel") + filetype;
        try (FileOutputStream originImageOutputStream = new FileOutputStream(originImageFileName);
             FileOutputStream pixelImageOutputStream = new FileOutputStream(pixelImageFileName)) {
            ImageIO.write(originImage, "png", originImageOutputStream);
            ImageIO.write(pixelImage, "png", pixelImageOutputStream);
        }
        pixelConfig.setBlocksX(blocksX);
        pixelConfig.setBlocksY(blocksY);
        return colorList;
    }

    /**
     * @param pixelConfig
     * @param r
     * @param g
     * @param b
     * @return
     */
    private Color getColor(PixelConfig pixelConfig, int r, int g, int b) {
        Color result = null;
        double distance = 3 * 255 * 255;
        for (Color color : pixelConfig.getColorList()) {
            double d = (r - color.getR()) * (r - color.getR()) + (g - color.getG()) * (g - color.getG()) + (b - color.getB()) * (b - color.getB());
            if (d < distance) {
                distance = d;
                result = color;
            }
        }
        return result;
    }

    /**
     * @param pixelConfig
     * @param colorList
     * @throws Exception
     */
    private void makePixelFile(PixelConfig pixelConfig, List<Color> colorList) throws Exception {
        int idx = pixelConfig.getFilename().lastIndexOf("/");
        String filename = pixelConfig.getFilename().substring(idx);
        filename = pixelConfig.getFilename().replace(filename, "/web" + filename);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        String pixelFilename = filename.replace(filetype, "_pixel.html");
        List<String> lineList = Lists.newArrayList();
        lineList.addAll(pixelConfig.getHeaderLineList());
        lineList.add("<body>");
        lineList.add(String.format(pixelConfig.getPixelTableTpl(), pixelConfig.getBlocksX() > pixelConfig.getBlocksY() ? "pixel_table_w" : "pixel_table_h"));
        lineList.add("<tr>");
        for (int i = 0; i < colorList.size(); i++) {
            if (i > 0 && i % pixelConfig.getBlocksX() == 0) {
                lineList.add("</tr>");
                lineList.add("<tr>");
            }
            lineList.add(String.format(pixelConfig.getPixelTdTpl(), colorList.get(i).getName()));
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
     * @param pixelConfig
     * @param colorList
     * @throws Exception
     */
    private void makePaperFile(PixelConfig pixelConfig, List<Color> colorList) throws Exception {
        int idx = pixelConfig.getFilename().lastIndexOf("/");
        String filename = pixelConfig.getFilename().substring(idx);
        filename = pixelConfig.getFilename().replace(filename, "/web" + filename);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        int cols = pixelConfig.getBlocksX() / pixelConfig.getPaperBlocks();
        int rows = pixelConfig.getBlocksY() / pixelConfig.getPaperBlocks();
        cols += pixelConfig.getBlocksX() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += pixelConfig.getBlocksY() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;
        List<String> paperLineList = Lists.newArrayList();
        List<Color> paperColorList = Lists.newArrayList();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                paperLineList.clear();
                paperColorList.clear();
                String paperFilename = filename.replace(filetype, String.format("_paper_%02d%02d.html", row, col));
                paperLineList.add(pixelConfig.getPaperTableTpl());
                //序号行
                paperLineList.add("<tr>");
                paperLineList.add(String.format(pixelConfig.getPageCornerTpl(), ""));
                for (int x = 0; x < pixelConfig.getPaperBlocks(); x++) {
                    if (col * pixelConfig.getPaperBlocks() + x >= pixelConfig.getBlocksX()) {
                        continue;
                    }
                    paperLineList.add(String.format(pixelConfig.getPageColTpl(), col * pixelConfig.getPaperBlocks() + x));
                }
                paperLineList.add("</tr>");
                for (int y = 0; y < pixelConfig.getPaperBlocks(); y++) {
                    if (row * pixelConfig.getPaperBlocks() + y >= pixelConfig.getBlocksY()) {
                        continue;
                    }
                    if (y > 0) {
                        paperLineList.add("</tr>");
                    }
                    paperLineList.add("<tr>");
                    paperLineList.add(String.format(pixelConfig.getPageRowTpl(), row * pixelConfig.getPaperBlocks() + y));
                    for (int x = 0; x < pixelConfig.getPaperBlocks(); x++) {
                        if (col * pixelConfig.getPaperBlocks() + x >= pixelConfig.getBlocksX()) {
                            continue;
                        }
                        int offset = (row * pixelConfig.getPaperBlocks() + y) * pixelConfig.getBlocksX() + col * pixelConfig.getPaperBlocks() + x;
                        paperColorList.add(colorList.get(offset));
                        paperLineList.add(String.format(pixelConfig.getBlockTdTpl(), colorList.get(offset).getName()));
                    }
                }
                paperLineList.add("</tr>");
                paperLineList.add("</table>");
                paperLineList.add("</body>");

                List<String> lineList = Lists.newArrayList();
                this.addHeaderLinesToFile(pixelConfig, lineList);
                lineList.add(this.makePaperPartsBoxLine(pixelConfig, paperColorList));
                lineList.add("</head>");
                lineList.add("<body>");
                lineList.add(String.format(pixelConfig.getTitleTpl(), String.format("%02d%02d", row, col)));
                lineList.add(pixelConfig.getPartsButtonTpl());
                lineList.addAll(this.makePartsFileLineList(pixelConfig, paperColorList));
                lineList.add(this.makeActionTableLine(pixelConfig, row, col));
                lineList.addAll(paperLineList);
                this.writeTextFile(lineList, paperFilename);
            }
        }
    }

    /**
     * 图纸零件盒
     *
     * @param pixelConfig
     * @param paperColorList
     * @return
     */
    private String makePaperPartsBoxLine(PixelConfig pixelConfig, List<Color> paperColorList) {
        List<String> colorNameList = paperColorList.stream().collect(Collectors.groupingBy(Color::getName)).keySet().stream().sorted().collect(Collectors.toList());
        List<String> partsBox = Lists.newArrayList();
        for (int i = 0; i < colorNameList.size(); i++) {
            int row = i / pixelConfig.getPartsBoxX();
            int col = i % pixelConfig.getPartsBoxX();
            partsBox.add(String.format("\"%s\":\"%s-%s\"", colorNameList.get(i), row + 1, col + 1));
        }
        return "<script>var partsBoxMap = {" + Joiner.on(",").join(partsBox) + "};</script>";
    }

    /**
     * @param pixelConfig
     * @param row
     * @param col
     * @return
     */
    private String makeActionTableLine(PixelConfig pixelConfig, int row, int col) {
        int idx = pixelConfig.getFilename().lastIndexOf("/");
        String filename = pixelConfig.getFilename().substring(idx + 1);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);

        int cols = pixelConfig.getBlocksX() / pixelConfig.getPaperBlocks();
        int rows = pixelConfig.getBlocksY() / pixelConfig.getPaperBlocks();
        cols += pixelConfig.getBlocksX() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += pixelConfig.getBlocksY() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;

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
        return String.format(pixelConfig.getActionTableTpl(), indexFilename, prePaperFilename, nextPaperFilename);
    }

    /**
     * @param pixelConfig
     */
    private void makeIndexFile(PixelConfig pixelConfig) throws Exception {
        int idx = pixelConfig.getFilename().lastIndexOf("/");
        String filename = pixelConfig.getFilename().substring(idx + 1);
        idx = filename.indexOf(".");
        String filetype = filename.substring(idx);
        String indexFilename = pixelConfig.getFilename().replace(filename, "/web/index.html");
        int cols = pixelConfig.getBlocksX() / pixelConfig.getPaperBlocks();
        int rows = pixelConfig.getBlocksY() / pixelConfig.getPaperBlocks();
        cols += pixelConfig.getBlocksX() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;
        rows += pixelConfig.getBlocksY() % pixelConfig.getPaperBlocks() == 0 ? 0 : 1;

        List<String> lineList = Lists.newArrayList();
        this.addHeaderLinesToFile(pixelConfig, lineList);
        lineList.add("</head>");
        lineList.add(pixelConfig.getIndexBodyTpl());
        lineList.add("<table>");
        lineList.add(String.format(pixelConfig.getTitleTpl(), "目录"));
        lineList.add(String.format(pixelConfig.getIndexTdTpl(), filename.replace(filetype, "_pixel.html"), "像素图"));
        lineList.add(String.format(pixelConfig.getIndexTdTpl(), filename.replace(filetype, "_parts_00.html"), "零件表"));
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                lineList.add(String.format(pixelConfig.getIndexTdTpl(), filename.replace(filetype, String.format("_paper_%02d%02d.html", row, col)), String.format("图纸-%02d%02d", row, col)));
            }
        }
        lineList.add("</table>");
        lineList.add("</body>");
        this.writeTextFile(lineList, indexFilename);
    }

    /**
     * @param pixelConfig
     * @param colorList
     * @throws Exception
     */
    private void makePartsFile(PixelConfig pixelConfig, List<Color> colorList) throws Exception {
        int idx = pixelConfig.getFilename().indexOf(".");
        String filetype = pixelConfig.getFilename().substring(idx);
        idx = pixelConfig.getFilename().lastIndexOf("/");
        String filename = pixelConfig.getFilename().substring(idx);
        String indexFilename = "index.html";
        filename = pixelConfig.getFilename().replace(filename, "/web" + filename);
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        int total = 0;
        for (Map.Entry<String, List<Color>> entry : colorMap.entrySet()) {
            total += entry.getValue().size();
        }
        List<String> partsLineList = Lists.newArrayList();
        this.addHeaderLinesToFile(pixelConfig, partsLineList);
        partsLineList.add("</head>");
        partsLineList.add("<body>");
        partsLineList.add(String.format(String.format(pixelConfig.getIndexButtonTpl(), indexFilename)));
        partsLineList.add(String.format(pixelConfig.getTitleTpl(), "零件总数：" + total));
        partsLineList.addAll(this.addToPackage(pixelConfig, colorList));
        partsLineList.addAll(this.makePartsFileLineList(pixelConfig, colorList));
        partsLineList.add("</body>");

        String partsFilename = filename.replace(filetype, "_parts_00.html");
        this.writeTextFile(partsLineList, partsFilename);
    }

    /**
     * @param pixelConfig
     * @param colorList
     * @return
     */
    private List<String> makePartsFileLineList(PixelConfig pixelConfig, List<Color> colorList) {
        List<String> lineList = Lists.newArrayList();
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        List<String> nameList = colorMap.keySet().stream().sorted().collect(Collectors.toList());
        int rows = nameList.size() / pixelConfig.getPartsRowNum();
        rows += nameList.size() % pixelConfig.getPartsRowNum() == 0 ? 0 : 1;
        lineList.add(pixelConfig.getPartsTableTpl());
        for (int i = 0; i < rows; i++) {
            lineList.add("<tr>");
            for (int j = 0; j < pixelConfig.getPartsRowNum(); j++) {
                String colorName = "";
                int index = i * pixelConfig.getPartsRowNum() + j;
                if (index < nameList.size()) {
                    colorName = nameList.get(index);
                }
                lineList.add(String.format(pixelConfig.getPartsColTpl(), colorName));
            }
            lineList.add("</tr>");
            lineList.add("<tr>");
            for (int j = 0; j < pixelConfig.getPartsRowNum(); j++) {
                int colorNum = 0;
                int index = i * pixelConfig.getPartsRowNum() + j;
                if (index < nameList.size()) {
                    colorNum = colorMap.get(nameList.get(index)).size();
                }
                lineList.add(String.format(pixelConfig.getPartsRowTpl(), colorNum));
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
     * @param pixelConfig
     * @param lineList
     */
    private void addHeaderLinesToFile(PixelConfig pixelConfig, List<String> lineList) {
        lineList.addAll(pixelConfig.getHeaderLineList());
    }

    /**
     * @param pixelConfig
     * @param colorList
     * @return
     */
    private List<String> addToPackage(PixelConfig pixelConfig, List<Color> colorList) {
        Map<String, List<String>> packageMap = Maps.newHashMap();
        Map<String, List<Color>> colorMap = colorList.stream().collect(Collectors.groupingBy(Color::getName));
        for (Map.Entry<String, List<Color>> entry : colorMap.entrySet()) {
            this.addToPackage(pixelConfig, entry.getKey(), entry.getValue().size(), packageMap);
        }
        int price = 0;
        for (int i = 0; i < pixelConfig.getPackageName().length; i++) {
            price += pixelConfig.getPackagePrice()[i] * packageMap.get(String.valueOf(pixelConfig.getPackageName()[i])).size();
        }
        List<String> lineList = Lists.newArrayList();
        lineList.add(String.format(pixelConfig.getTitleTpl(), "价格：" + BigDecimal.valueOf(price).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
        lineList.add(pixelConfig.getPartsTableTpl());
        for (Map.Entry<String, List<String>> entry : packageMap.entrySet()) {
            String line = entry.getKey() + "粒：" + Joiner.on(" ").join(entry.getValue());
            lineList.add("<tr>");
            lineList.add(String.format(pixelConfig.getPackageRowTpl(), line));
            lineList.add("</tr>");
        }
        lineList.add("</table>");
        return lineList;
    }

    /**
     * @param pixelConfig
     * @param colorName
     * @param colorNum
     * @param packageMap
     */
    private void addToPackage(PixelConfig pixelConfig, String colorName, int colorNum, Map<String, List<String>> packageMap) {
        colorName = String.valueOf(Integer.parseInt(colorName));
        int biggest = pixelConfig.getPackageName()[pixelConfig.getPackageName().length - 1];
        for (int i = 0; i < pixelConfig.getPackagePrice().length; i++) {
            if (colorNum <= pixelConfig.getPackageName()[i]) {
                List<String> colorList = packageMap.computeIfAbsent(String.valueOf(pixelConfig.getPackageName()[i]), k -> Lists.newArrayList());
                colorList.add(colorName);
                break;
            }
        }
        if (colorNum > biggest) {
            List<String> colorList = packageMap.computeIfAbsent(String.valueOf(biggest), k -> Lists.newArrayList());
            colorList.add(colorName);
            this.addToPackage(pixelConfig, colorName, colorNum - biggest, packageMap);
        }
    }
}
