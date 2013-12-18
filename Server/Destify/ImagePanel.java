package Destify;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigInteger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = -3747955137230037528L;
	private static final int bufferedImageType = BufferedImage.TYPE_INT_RGB;
	private static final Color defaultColor = Color.lightGray;
	private static Object[][] defaultRenderingHints = { 
			{ RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY },
			{ RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON },
			{ RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC },
			{ RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE },
			{ RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY } };

	private int width, height;
	private BufferedImage bufferedImage;
	private Graphics2D g2d;

	public ImagePanel(ImageIcon imageIcon) {
		init(-1, -1);
		this.bufferedImage = getBufferedImage(imageIcon);
	}

	private void init(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0)
			return Printable.NO_SUCH_PAGE;

		Graphics2D g = (Graphics2D) graphics;
		Dimension d = new Dimension(width, height);

		Dimension dprev = d;
		setSize(dprev);

		g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		double scale = Math.min(pageFormat.getImageableWidth() /dprev.width, pageFormat.getImageableHeight() /dprev.height);
		g.scale(scale, scale);

		paint(g);
		return Printable.PAGE_EXISTS;
	}

	public static RenderingHints getRenderingHints() {
		RenderingHints renderingHints = new RenderingHints((RenderingHints.Key) defaultRenderingHints[0][0], defaultRenderingHints[0][1]);
		for (int i = 1; i < defaultRenderingHints.length; i++)
			renderingHints.put((RenderingHints.Key) defaultRenderingHints[i][0], defaultRenderingHints[i][1]);
		return renderingHints;
	}

	public BufferedImage getBufferedImage(ImageIcon imageIcon) {
		if (bufferedImage == null) {
			
			if (this.width <= 0)
				this.width = imageIcon.getIconWidth();
			if (this.height <= 0)
				this.height = imageIcon.getIconHeight();

			bufferedImage = new BufferedImage(width, height, bufferedImageType);
			g2d = bufferedImage.createGraphics();
			g2d.setRenderingHints(getRenderingHints());
			g2d.setColor(defaultColor);
			
			if (imageIcon != null)
				g2d.drawImage(imageIcon.getImage(), 0, 0, width, height, this);
			else
				g2d.drawRect(0, 0, width, height);
		}
		return bufferedImage;
	}

	public Graphics2D getGraphics2D() {
		return g2d;
	}

	public int getImageWidth() {
		return width;
	}

	public int getImageHeight() {
		return height;
	}

	public void repaintImage(Rectangle rect) {
		Dimension d = getSize();
		repaint(rect.x *d.width /width, rect.y *d.height /height,
				rect.width *d.width /width, rect.height *d.height /height);
	}

	public int getColor() {
		return g2d.getColor().getRGB();
	}

	public void setColor(int color) {
		g2d.setColor(new Color(color));
	}

	public void clear() {
		g2d.fillRect(0, 0, width, height);
	}

	public void fillRectangle(Rectangle rect) {
		g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	public void setRGB(int x, int y, int rgb) {
		bufferedImage.setRGB(x, y, rgb);
	}

	public int getRGB(int x, int y) {
		return bufferedImage.getRGB(x, y);
	}

	protected void paintComponent(Graphics graphic) {
		Graphics2D g = (Graphics2D) graphic;
		g.setRenderingHints(g2d.getRenderingHints());
		g.drawImage(bufferedImage, 0, 0, this.getWidth(), this.getHeight(), this);
	}

	public Dimension getPreferredSize() {
		return bufferedImage != null ? new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()) : new Dimension(700, 450);
	}
	
	public Color getInvertedColor(Color c) {
		double Y = 0.2126 *c.getRed() +0.7152 *c.getGreen() +0.0722 *c.getBlue();
		
		if(Y > 128) {
			return Color.black;
		} else {
			return Color.white;
		}		
	}
	
	public int getAverageColor() {
	    BigInteger[] color = getBigIntegers(3);
	    for (int y = 0; y < this.getImageHeight(); y++) {
	        for (int x = 0; x < this.getImageWidth(); x++) {
	            int c = this.getRGB(x, y);
	            color[0] = add(color[0], (c >> 16) & 0xFF);
	            color[1] = add(color[1], (c >> 8) & 0xFF);
	            color[2] = add(color[2], c & 0xFF);
	        }
	    }
	    BigInteger size = new BigInteger(String.valueOf(this.getImageHeight()));
	    size = size.multiply(new BigInteger(String.valueOf(this.getImageWidth())));
	    for (int i = 0; i < color.length; i++) { color[i] = color[i].divide(size); }
	    return (0xFF << 24) | (color[0].intValue() << 16) | (color[1].intValue() << 8) | color[2].intValue();
	}
	 
	private BigInteger add(BigInteger bi, long l) {
	    return bi.add(new BigInteger(String.valueOf(l)));
	}
	 
	private BigInteger[] getBigIntegers(int size) {
	    BigInteger[] bigIntegers = new BigInteger[size];
	    for (int i = 0; i < bigIntegers.length; i++) {
	        bigIntegers[i] = new BigInteger("0");
	    }
	    return bigIntegers;
	}
}