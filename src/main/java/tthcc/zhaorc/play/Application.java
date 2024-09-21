package tthcc.zhaorc.play;

import lombok.extern.slf4j.Slf4j;
import tthcc.zhaorc.play.pixel.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * created by zhaoricheng@jd.com at 2024/05/17 15:40
 */
@Slf4j
public class Application {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Application application = new Application();

//        BlockConfig pixelConfig = application.configBlock();

        MarkConfig pixelConfig = application.configMark();

        Image2Pixel image2Pixel = new Image2Pixel();
        image2Pixel.convertPixel(pixelConfig);
    }

    /**
     * @return
     */
    private MarkConfig configMark() {
        // 580 x 420

//        String filename = "c:/d_pan/my/pixel/008/longmao.png";
        String filename = "c:/d_pan/my/pixel/009/dl250_a.jpg";

        //XXX
        log.info("start....,filename={}", filename);
        MarkConfig markConfig = new MarkConfig();
        markConfig.setFilename(filename);

//        //008
//        markConfig.setOffsetLeft(235);
//        markConfig.setOffsetRight(236);
//        markConfig.setOffsetTop(15);
//        markConfig.setOffsetBottom(15);
//        markConfig.setMergeX(5);
//        markConfig.setMergeY(5);

        //009
        markConfig.setOffsetLeft(0);
        markConfig.setOffsetRight(0);
        markConfig.setOffsetTop(18);
        markConfig.setOffsetBottom(0);
        markConfig.setMergeX(3);
        markConfig.setMergeY(3);

        markConfig.setPageBlockSize(18);
        markConfig.setPaperBlocks(10);
        loadColorList(markConfig);

        return markConfig;
    }

    /**
     * @return
     */
    private BlockConfig configBlock() {
//     String filename = "c:/d_pan/my/pixel/001/origin_a.png";
//    String filename = "c:/d_pan/my/pixel/002/tuye_a.jpg";
        String filename = "c:/d_pan/my/pixel/004/houzi.png";
//    String filename = "d:/my/pixel/006/longmao_a.jpg";
//    String filename = "d:/my/pixel/009/bicycle_a.jpg";


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
    private void loadColorList(PixelConfig pixelConfig) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream(pixelConfig.getColorFilename()))))) {
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
