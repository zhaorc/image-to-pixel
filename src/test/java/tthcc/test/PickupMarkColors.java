package tthcc.test;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * created by zhaoricheng@jd.com at 2024/08/21 08:23
 */
@Slf4j
public class PickupMarkColors {

    private List<MarkColorFile> markColorFileList = Lists.newArrayList(
            MarkColorFile.builder()
                    .filename("mark_color_01.png")
                    .rows(18)
                    .cols(14)
                    .offsetLeft(47)
                    .offsetTop(262)
                    .radius(40)
                    .distanceX0(50)
                    .distanceX1(49)
                    .distanceY0(53)
                    .distanceY1(52)
                    .offsetCenter(-12)
                    .nameList(Lists.newArrayList("120", "0", "WG9", "WG8", "WG7", "WG6", "WG5", "WG4", "WG3", "WG2", "WG1", "WG05", "109", "F37", "CG9", "CG8", "CG7", "CG6", "CG5", "CG4", "CG3", "CG2", "CG1", "CG05", "138", "131", "133", "135", "F", "GG9", "GG7", "GG5", "GG3", "GG1", "BG9", "BG7", "BG5", "BG3", "BG1", "99", "F48", "F36", "373", "415", "153", "365", "370", "218", "95", "96", "92", "F41", "F47", "411", "412", "102", "413", "134", "F3", "F20", "F19", "F32", "F22", "F16", "45", "41", "104", "169", "101", "100", "F11", "F30", "F31", "F34", "F29", "F33", "F28", "162", "410", "417", "107", "103", "F23", "98", "F18", "407", "F17", "F27", "F43", "F15", "416", "F38", "F40", "F24", "154", "F25", "F45", "97", "75", "77", "178", "32", "145", "73", "82", "84", "85", "86", "87", "81", "82", "69", "F52", "68", "179", "144", "143", "182", "147", "185", "183", "76", "70", "74", "71", "72", "166", "171", "67", "66", "63", "58", "65", "54", "57", "53", "50", "61", "64", "62", "16", "124", "59", "48", "47", "46", "172", "167", "55", "56", "52", "51", "42", "43", "38", "37", "49", "36", "34", "35", "49", "44", "F7", "F8", "F13", "173", "174", "175", "F151", "123", "124", "164", "141", "142", "F10", "F21", "F50", "F14", "F5", "32", "31", "33", "414", "152", "20", "F42", "F49", "F9", "F39", "375", "F26", "21", "122", "22", "23", "24", "374", "139", "209", "140", "F44", "F70", "F54", "94", "376", "93", "F56", "155", "F46", "91", "196", "137", "26", "136", "360", "29", "25", "360", "132", "366", "367", "369", "371", "361", "198", "378", "379", "27", "F73", "28", "356", "357", "F53", "18", "F68", "F69", "F74", "147", "130", "146", "9", "7", "13", "14", "16", "6", "126", "125", "88", "F71", "17", "89"))
                    .build(),
            MarkColorFile.builder()
                    .filename("mark_color_02.png")
                    .rows(1)
                    .cols(11)
                    .offsetLeft(74)
                    .offsetTop(15)
                    .radius(60)
                    .distanceX0(76)
                    .distanceX1(76)
                    .distanceY0(55)
                    .distanceY1(55)
                    .offsetCenter(-18)
                    .nameList(Lists.newArrayList("8", "121", "10", "15", "12", "11", "5", "4", "3", "2", "1"))
                    .build()
    );

    @Test
    public void doTest() throws Exception {
        for (MarkColorFile markColorFile : markColorFileList) {
            try (InputStream inputStream = this.getClass().getResourceAsStream("/" + markColorFile.getFilename());
                 FileOutputStream fileOutputStream = new FileOutputStream("d:/tmp/2/_" + markColorFile.getFilename())) {
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                int samples = 5;
                int cX = 0;
                int cY = markColorFile.getOffsetTop() + markColorFile.getRadius() / 2;
//                int centerColor = 0;
                int blockColor = 255 << 16;
                for (int row = 0; row < markColorFile.getRows(); row++) {
                    cX = markColorFile.getOffsetLeft() + markColorFile.getRadius() / 2;
                    for (int col = 0; col < markColorFile.getCols(); col++) {
//                        for (int x = -2; x < 3; x++) {
//                            for (int y = -2; y < 3; y++) {
//                                bufferedImage.setRGB(cX + x, cY + y, centerColor);
//                            }
//                        }
                        int rgb = 0;
                        int r = 0, g = 0, b = 0;
                        for (int x = -samples / 2; x <= samples / 2; x++) {
                            for (int y = -samples / 2; y <= samples / 2; y++) {
                                rgb = bufferedImage.getRGB(cX + x, markColorFile.getOffsetCenter() + cY + y);
                                r += ((rgb >> 16) & 0xff);
                                g += ((rgb >> 8) & 0xff);
                                b += (rgb & 0xff);
                                bufferedImage.setRGB(cX + x, markColorFile.getOffsetCenter() + cY + y, blockColor);
                            }
                        }
                        r = r / samples / samples;
                        g = g / samples / samples;
                        b = b / samples / samples;
                        //XXX
                        System.out.println(String.format("%5s=#%02x%02x%02x", markColorFile.getNameList().get(row * markColorFile.getCols() + col), r, g, b));
                        cX += ((col % 2 == 0) ? markColorFile.getDistanceX0() : markColorFile.getDistanceX1());
                    }
                    cY += ((row % 2 == 0 ? markColorFile.getDistanceY0() : markColorFile.getDistanceY1()));
                }
                ImageIO.write(bufferedImage, "png", fileOutputStream);
            }
        }
    }

    @Setter
    @Getter
    @SuperBuilder
    @NoArgsConstructor
    static class MarkColorFile {
        private String filename;
        private int rows;
        private int cols;
        private int offsetLeft;
        private int offsetTop;
        private int radius;
        private int distanceX0;
        private int distanceX1;
        private int distanceY0;
        private int distanceY1;
        private int offsetCenter;
        private List<String> nameList;
    }

}
