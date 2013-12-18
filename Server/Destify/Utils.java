package Destify;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.border.AbstractBorder;

public class Utils {
	public static String defaultDirectory() {
	    String OS = System.getProperty("os.name").toUpperCase();
	    if (OS.contains("WIN")) return System.getenv("APPDATA");
	    else if (OS.contains("MAC")) return System.getProperty("user.home") + "/Library/Application Support";
	    else if (OS.contains("NUX")) return System.getProperty("user.home");
	    return System.getProperty("user.dir");
	}
	
	public static byte[] hexStringToByteArray(String s) {
		s = s.substring(1, s.length() -1).replace(" ", "");
		
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +Character.digit(s.charAt(i+1), 16));
	    }
	    
	    return data;
	}
	
	public static String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			md = null;
			array = null;
			
			String toReturn = new String(sb.toString());
			sb = null;
			System.gc();
			
			return toReturn;
		} catch (Exception e) { }
		return null;
	}
	
	public static String getAddr() throws IOException {
		Enumeration <?> en = NetworkInterface.getNetworkInterfaces();
        while(en.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) en.nextElement();
            Enumeration <?> ee = n.getInetAddresses();
            
            while(ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                
                String ta = i.getHostAddress();
                if(ta.contains(".") && !ta.equals("127.0.0.1") /*&& i.isReachable(10)*/) {
                	return ta;
                }
            }
        }
		return "127.0.0.1";
	}
	
	public static ImageIcon getImage(String icon, int i, int j, boolean r) {
		ImageIcon imageIcon = null;
		
		if(r) {
			imageIcon = new ImageIcon(Utils.class.getResource(icon));
		} else {
			imageIcon = new ImageIcon(icon);
		}

		Image image = imageIcon.getImage();
	    Image newimg = image.getScaledInstance(i, j, Image.SCALE_SMOOTH);
	    imageIcon = new ImageIcon(newimg);
	    
	    return imageIcon;
	}
	
	public static String decode(String NText, String code) {
		StringBuilder ctext = new StringBuilder();

		int a = 0;
		for (int i = 0; i < NText.length(); i++) {
			char key = code.toUpperCase().charAt(a % code.length());
			char aCh = NText.charAt(i);
			char nCh = ' ';

			if (aCh >= 65 && aCh <= 90) {
				aCh = NText.charAt(i);
				nCh = (char) (aCh +65 -key);
				if ((int) nCh < 65) { nCh += 26; }
				if (NText.charAt(i) >= 97) { nCh += 32; }
				ctext.append(nCh);
				a++;
			} else if (aCh >= 97 && aCh <= 122) {
				aCh = NText.charAt(i);
				nCh = (char) (aCh +65 -key);
				if ((int) nCh < 97) { nCh += 26; 
				}
				ctext.append(nCh);
				a++;
			} else {
				nCh = (char) (aCh);
				ctext.append(NText.charAt(i));
			}
		}
		return ctext.toString();
	}
	
	public static String decode(String NText, String code, int withoffset) {
		StringBuilder ctext = new StringBuilder();

		int a = withoffset;
		for (int i = 0; i < NText.length(); i++) {
			char key = code.toUpperCase().charAt(a % code.length());
			char aCh = NText.charAt(i);
			char nCh = ' ';

			if (aCh >= 65 && aCh <= 90) {
				aCh = NText.charAt(i);
				nCh = (char) (aCh +65 -key);
				if ((int) nCh < 65) { nCh += 26; }
				if (NText.charAt(i) >= 97) { nCh += 32; }
				ctext.append(nCh);
				a++;
			} else if (aCh >= 97 && aCh <= 122) {
				aCh = NText.charAt(i);
				nCh = (char) (aCh +65 -key);
				if ((int) nCh < 97) { nCh += 26; 
				}
				ctext.append(nCh);
				a++;
			} else {
				nCh = (char) (aCh);
				ctext.append(NText.charAt(i));
			}
		}
		return ctext.toString();
	}
	
	public static ImageIcon roundedBorderLineWithFixedBackground(ImageIcon image, int cornerRadius, Color color) {
		BufferedImage buImg = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB); 
		buImg.getGraphics().drawImage(image.getImage(), 0,0, image.getImageObserver());
		
	    int w = buImg.getWidth();
	    int h = buImg.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();

	    // This is what we want, but it only does hard-clipping, i.e. aliasing
	    // g2.setClip(new RoundRectangle2D ...)

	    // so instead fake soft-clipping by first drawing the desired clip shape
	    // in fully opaque white with antialiasing enabled...
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

	    // ... then compositing the image on top,
	    // using the white shape from above as alpha source
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(buImg, 0, 0, null);

        g2.setColor(color);
        g2.drawRoundRect(0, 0, w -1, h -1, cornerRadius, cornerRadius);
	    
	    g2.dispose();

	    return new ImageIcon(output);
	}
	
	public static class RoundedBorderLine extends AbstractBorder {
	    private static final long serialVersionUID = 1L;
	    private int RADIUS = 15;
	    private Color color, background;

	    RoundedBorderLine(Color color, Color background, int radius) {
	    	this.color = color;
	    	this.background = background;
	    	this.RADIUS = radius;
	    }
	    
	    @Override
	    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
	        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        g.setColor(background);
	        g.fillRoundRect(x, y, width -1, height, RADIUS, RADIUS);
	        
	        g.setColor(color);
	        g.drawRoundRect(x, y, width -1, height -1, RADIUS, RADIUS);
	    }
	}
}
