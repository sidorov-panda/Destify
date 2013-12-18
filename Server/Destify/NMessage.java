package Destify;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;

public class NMessage extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JLabel titleLabel, timeLabel, messageLabel, stickyLabel;
	private JPanel container, padding, titlePane, messageContainer, timeStickyPane;
	private JScrollPane messageScroll;
	private Font titleFont, messageFont;
	private NController controller;
	private NMessage nmessage;
	
    private float alpha = 0, elapsed = 0, step = 0.025f, maxAlpha = 0.9f;
    private String dir = "IN";
    private Timer t;
    private int speed = 1, delay = 10000;
    private boolean pinned = false;
	
    private Color background = Color.white, 
    		fontColor = Color.black, 
    		borderColor = fontColor;

	NMessage(String title, String subtitle, String message, String time, ImageIcon logo, final ImageIcon pin, final ImageIcon unpin, NController controller, final int delay) {
		this.controller = controller;
		this.nmessage = this;
		
		this.delay = delay *1000;
		
	    ImagePanel ip = new ImagePanel(logo);
	    background = new Color(ip.getAverageColor(), true);
	    fontColor = ip.getInvertedColor(background);
	    borderColor = fontColor;

		logo = Utils.roundedBorderLineWithFixedBackground(logo, 25, borderColor.brighter());
		JLabel iconLabel = new JLabel(logo);
		iconLabel.setVerticalAlignment(JLabel.TOP);
		
		titleFont = new Font("Tahoma", Font.BOLD, 13);
		messageFont = new Font("Tahoma", Font.PLAIN, 12);

		container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(borderColor));
		container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		padding = new JPanel();
		padding.setLayout(new BorderLayout());
		padding.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		padding.setOpaque(false);

		titleLabel = new JLabel();
		titleLabel.setText("<html>" +title +"</html>");
		titleLabel.setFont(titleFont);
		titleLabel.setForeground(fontColor);

		timeLabel = new JLabel();
		timeLabel.setText(time);
		timeLabel.setFont(messageFont);
	
		timeLabel.setForeground(fontColor);
		
		stickyLabel = new JLabel(pin);
		stickyLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		stickyLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) { }

			@Override
			public void mouseEntered(MouseEvent arg0) { 
				stickyLabel.setForeground(Color.red);
				nmessage.repaint();
			}

			@Override
			public void mouseExited(MouseEvent arg0) { }

			@Override
			public void mousePressed(MouseEvent arg0) {
				if(!pinned) {
					stickyLabel.setIcon(unpin);
					pinned = true;
					t.stop();
					t = null;
					
				} else {
					stickyLabel.setIcon(pin);
					pinned = false;

		            t = new Timer(speed, nmessage);
		            t.setInitialDelay(delay);
		            t.start();
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) { }
		});

		timeStickyPane = new JPanel();
		timeStickyPane.setLayout(new BorderLayout());
		timeStickyPane.setOpaque(false);
		timeStickyPane.add(timeLabel, BorderLayout.WEST);
		timeStickyPane.add(stickyLabel, BorderLayout.EAST);

		titlePane = new JPanel();
		titlePane.setLayout(new BorderLayout());
		titlePane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
		titlePane.setOpaque(false);

		titlePane.add(titleLabel, BorderLayout.WEST);
		titlePane.add(timeStickyPane, BorderLayout.EAST);

		if(!subtitle.trim().equals("")) { subtitle += "<br/>"; }
		
		messageLabel = new JLabel();
		String messageText = String.format("<html><div style='width:%dpx;overflow-x:hidden;overflow-y:scroll;'>%s</div><html>", 230, subtitle +message);
		messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		messageLabel.setOpaque(false);
		messageLabel.setText("<html>" +messageText +"</html>");
		messageLabel.setFont(messageFont);
		messageLabel.setForeground(fontColor);

		messageScroll = new JScrollPane(messageLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messageScroll.setBorder(BorderFactory.createEmptyBorder());
		messageScroll.getViewport().setOpaque(false);
		messageScroll.setOpaque(false);
		
		@SuppressWarnings("unused")
		int preferredWidth = 320, minimumheight = 250;
		
		if(messageLabel.getPreferredSize().height > 150) {
			messageScroll.setPreferredSize(new Dimension(preferredWidth, 150));
		} else {
			messageScroll.setPreferredSize(new Dimension(preferredWidth, messageLabel.getPreferredSize().height));
		}		
		
		messageContainer = new JPanel();
		messageContainer.setLayout(new BorderLayout());
		messageContainer.setOpaque(false);
		messageContainer.add(messageScroll, BorderLayout.NORTH);
	
		padding.add(titlePane, BorderLayout.NORTH);
		padding.add(messageContainer, BorderLayout.CENTER);
		
		container.setLayout(new BorderLayout());
		container.add(padding, BorderLayout.EAST);
		
		JPanel iconHolder = new JPanel(new BorderLayout());
		iconHolder.setOpaque(false);
		iconHolder.add(iconLabel, BorderLayout.NORTH);
		
		container.add(iconHolder, BorderLayout.WEST);
		container.setOpaque(false);

		AbstractBorder borderLine = new Utils.RoundedBorderLine(borderColor.brighter(), background, 15);
		setBorder(borderLine);
        setLayout(new BorderLayout());
		setOpaque(false);
		add(container);

		setPreferredSize(new Dimension(controller.getWidth() -10, getPreferredSize().height));
		
        t = new Timer(speed, this);
        t.start();
	}

    @Override
    public void actionPerformed(ActionEvent e) {
    	if(dir.equals("IN") || pinned) {
            elapsed += step;
            
    	} else if(dir.equals("OUT")){
            elapsed -= step;
    	}
    	
        alpha = 0 +elapsed;        
        if (alpha >= maxAlpha) {
            alpha = maxAlpha;
            t.stop();
            dir = "OUT";

            if(!pinned) {
	            t = new Timer(speed, this);
	            t.setInitialDelay(delay);
	            t.start();
            }
            
        } else if(alpha <= 0) {
        	elapsed = 0.0f;
            alpha = 0;
            try { t.stop(); } catch (Exception ex) {}
            t = null;

            System.gc();
            controller.removeNotification(this);
            System.gc();
        }
        
        repaint();
    }
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color nc = new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), 0);
        
        g.setColor(nc);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
