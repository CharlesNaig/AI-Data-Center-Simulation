import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class CreateNumberGlyphs {
  public static void main(String[] args) throws Exception {
    var folder = new File(args[0]);
    folder.mkdirs();
    String characters = "0123456789.%/-";
    String[] families = {"water", "grid", "green", "wellbeing"};
    int[] colors = {0x075DA8, 0x946000, 0x12632A, 0xA52332};
    for (int f = 0; f < families.length; f++) for (int i = 0; i < characters.length(); i++) {
      String character = characters.substring(i, i + 1);
      var font = new Font(Font.SANS_SERIF, Font.BOLD, 32);
      var measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
      measure.setFont(font);
      int width = measure.getFontMetrics().charWidth(character.charAt(0)) + 4;
      measure.dispose();
      var image = new BufferedImage(width, 40, BufferedImage.TYPE_INT_ARGB);
      var graphics = image.createGraphics();
      graphics.setColor(new Color(colors[f]));
      graphics.setFont(font);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.drawString(character, 2, 32);
      graphics.dispose();
      ImageIO.write(image, "png", new File(folder, families[f] + "-" + i + ".png"));
    }
    System.out.println("PASS: 56 transparent numeric glyphs generated for large model-owned values.");
  }
}
