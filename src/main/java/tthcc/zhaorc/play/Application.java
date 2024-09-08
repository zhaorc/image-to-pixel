package tthcc.zhaorc.play;

import lombok.extern.slf4j.Slf4j;
import tthcc.zhaorc.play.pixel.BlockConfig;
import tthcc.zhaorc.play.pixel.Color;
import tthcc.zhaorc.play.pixel.Image2Pixel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * created by zhaoricheng@jd.com at 2024/05/17 15:40
 */
@Slf4j
public class Application {

    //    private final String filename = "c:/d_pan/my/pixel/001/origin_a.png";
//    private final String filename = "c:/d_pan/my/pixel/002/tuye_a.jpg";
//    private final String filename = "c:/d_pan/my/pixel/004/houzi.png";
    private final String filename = "d:/my/pixel/006/longmao_a.jpg";
//    private final String filename = "d:/my/pixel/009/bicycle_a.jpg";

    public static void main(String[] args) throws Exception {
        Application application = new Application();
        BlockConfig blockConfig = application.init();
        Image2Pixel image2Pixel = new Image2Pixel();
        image2Pixel.convertPixel(blockConfig);
    }

    private BlockConfig init() {
        //XXX
        log.info("start....,filename={}", filename);
        BlockConfig blockConfig = new BlockConfig();
        blockConfig.setFilename(filename);

//        // 001
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(24);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

//        // 002
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(0);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

//        // 004
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(0);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

        // 006
        blockConfig.setOffsetLeft(100);
        blockConfig.setOffsetRight(40);
        blockConfig.setOffsetTop(0);
        blockConfig.setOffsetBottom(0);
        blockConfig.setMergeX(3);
        blockConfig.setMergeY(3);

//        // 007
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(0);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

        blockConfig.setPageBlockSize(18);
        blockConfig.setPaperBlocks(10);
        blockConfig.setPartsRowNum(7);
        blockConfig.setMinColorNum(10);
        blockConfig.setPartsBoxX(5);

        loadColorList(blockConfig);
        blockConfig.setPackagePrice("200:4.20; 400:6.80; 1000:12.80");

        return blockConfig;
    }

    /**
     * @param pixelConfig
     */
    private void loadColorList(BlockConfig pixelConfig) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream("/color_list.txt"))))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.charAt(0) == '#') {
                    continue;
                }
                String[] v = line.split(" ");
                pixelConfig.addColor(new Color(v[0], v[1]));
            }
        } catch (Exception exp) {
            log.error(exp.getMessage(), exp);
        }
    }

}
