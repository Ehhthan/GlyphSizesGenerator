import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class GlyphSizesGenerator {


    public static void main(String[] args) throws IOException {
        final String template = "unicode_page_%s";
        final int rows = 16, columns = 16, tileHeight = 16, tileWidth = 16;

        File file = new File("glyph_sizes.bin");
        if (file.exists());
            file.delete();

        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        for (int i = 0x0; i <= 0x00FF; i++) {
            // The hex string of the first byte used to identify texture
            String code = Integer.toHexString(i);

            // Pads with a zero
            if (code.length() < 2)
                code = "0" + code;


            InputStream stream = GlyphSizesGenerator.class.getClassLoader().getResourceAsStream("textures/" + String.format(template, code) + ".png");

            BufferedImage image = (stream != null) ? ImageIO.read(stream) : null;
            int start = ((i & 0xFF) << 8);
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < columns; k++) {
                    int codepoint = start++;
                    int b = 0;
                    if (image != null) {
                        BufferedImage sub = image.getSubimage(tileWidth * k, tileHeight * j, tileWidth, tileHeight);
                        int minX = -1;
                        int maxX = -1;
                        for (int x = 0; x < sub.getWidth(); x++) {
                            for (int y = 0; y < sub.getHeight(); y++) {
                                int color = sub.getRGB(x, y);
                                int alpha = (color >> 24) & 0xFF;
                                if (alpha != 0) {
                                    // This sets the edges to the current coordinate
                                    // if this pixel lies outside the current boundaries
                                    if (minX == -1 || x < minX)
                                        minX = x;

                                    if (maxX == -1 || x > maxX)
                                        maxX = x;
                                }
                            }
                        }
                        if (minX == -1)
                            minX = 0;
                        if (maxX == -1)
                            maxX = 0;
                        b = (minX << 4) + (maxX & 0x0F);
                    }

                    accessFile.seek(codepoint);
                    accessFile.writeByte(b);
                }
            }
        }
        accessFile.close();
    }
}
