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
    private final String filename = "c:/d_pan/my/pixel/002/origin_b.jpg";

    public static void main(String[] args) {
        Application application = new Application();
        application.init();
    }

    private void init() {
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
        pixelConfig.setOffsetLeft(99);
        pixelConfig.setOffsetRight(99);
        pixelConfig.setOffsetTop(2);
        pixelConfig.setOffsetBottom(3);
        pixelConfig.setMergeX(25);
        pixelConfig.setMergeY(25);

        pixelConfig.setPageBlockSize(18);
        pixelConfig.setPaperBlocks(10);
        pixelConfig.setPartsRowNum(7);
        pixelConfig.setMinColorNum(10);
        pixelConfig.setPartsBoxX(5);
        loadColorList(pixelConfig);
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
            Image2Pixel image2Pixel = new Image2Pixel();
            image2Pixel.convertPixel(pixelConfig);
        } catch (Exception exp) {
            log.error(exp.getMessage(), exp);
        }
    }

}
