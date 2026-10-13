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
      var image = new BufferedImage(18, 30, BufferedImage.TYPE_INT_ARGB);
      var graphics = image.createGraphics();
      graphics.setColor(new Color(colors[f]));
      graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      String character = characters.substring(i, i + 1);
      graphics.drawString(character, (18 - graphics.getFontMetrics().stringWidth(character)) / 2, 25);
      graphics.dispose();
      ImageIO.write(image, "png", new File(folder, families[f] + "-" + i + ".png"));
    }
    System.out.println("PASS: 56 transparent numeric glyphs generated for large model-owned values.");
  }
}
