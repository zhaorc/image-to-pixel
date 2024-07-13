package tthcc.zhaorc.play;

import lombok.extern.slf4j.Slf4j;
import tthcc.zhaorc.play.pixel.Color;
import tthcc.zhaorc.play.pixel.Image2Pixel;
import tthcc.zhaorc.play.pixel.PixelConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * created by zhaoricheng@jd.com at 2024/05/17 15:40
 */
@Slf4j
public class Application {

    //    private final String filename = "c:/d_pan/my/pixel/001/origin_a.png";
    private final String filename = "c:/d_pan/my/pixel/002/tuye_a.jpg";
//    private final String filename = "c:/d_pan/my/pixel/004/houzi.png";
//    private final String filename = "c:/d_pan/my/pixel/006/longmao_a1.jpg";

    public static void main(String[] args) throws Exception {
        Application application = new Application();
        PixelConfig pixelConfig = application.init();
        Image2Pixel image2Pixel = new Image2Pixel();
        image2Pixel.convertPixel(pixelConfig);
    }

    private PixelConfig init() {
        //XXX
        log.info("start....,filename={}", filename);
        PixelConfig pixelConfig = new PixelConfig();
        pixelConfig.setFilename(filename);

//        // 001
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(24);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

        // 002
        pixelConfig.setOffsetLeft(0);
        pixelConfig.setOffsetRight(0);
        pixelConfig.setOffsetTop(0);
        pixelConfig.setOffsetBottom(0);
        pixelConfig.setMergeX(5);
        pixelConfig.setMergeY(5);

//        // 004
//        pixelConfig.setOffsetLeft(0);
//        pixelConfig.setOffsetRight(0);
//        pixelConfig.setOffsetTop(0);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

//        // 006
//        pixelConfig.setOffsetLeft(100);
//        pixelConfig.setOffsetRight(40);
//        pixelConfig.setOffsetTop(0);
//        pixelConfig.setOffsetBottom(0);
//        pixelConfig.setMergeX(5);
//        pixelConfig.setMergeY(5);

        pixelConfig.setPageBlockSize(18);
        pixelConfig.setPaperBlocks(10);
        pixelConfig.setPartsRowNum(7);
        pixelConfig.setMinColorNum(10);
        pixelConfig.setPartsBoxX(5);
        loadColorList(pixelConfig);
        pixelConfig.setPackagePrice("200:4.20; 400:6.80; 1000:12.80");

        return pixelConfig;
    }

    /**
     * @param pixelConfig
     */
    private void loadColorList(PixelConfig pixelConfig) {
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
