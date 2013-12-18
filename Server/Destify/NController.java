package Destify;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

public class NController extends JDialog {
	private static final long serialVersionUID = 1L;
	private Rectangle bounds;
	private NController nc;
	private AudioInputStream stream;
	private AudioFormat format;
	private DataLine.Info info;
	private Clip clip;
    private ImageIcon logo, pin, unpin;
	
	public NController() {
		nc = this;
		bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setLayout(new FlowLayout());
		
		URL yourFile = NController.class.getResource("/Resources/1.wav");
		
		try {
		    stream = AudioSystem.getAudioInputStream(yourFile);
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    clip = (Clip) AudioSystem.getLine(info);
			clip.open(stream);
		} catch (Exception e) { }

		pin = Utils.getImage("/Resources/pin.png", 15, -1, true);
		unpin = Utils.getImage("/Resources/unpin.png", 15, -1, true);
		
		setTitle("Destify");
		setSize(new Dimension(400, 5));
		setLocation(bounds.width -getWidth(), bounds.height -getHeight());
		
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 0));
		setResizable(false);
		
		setAlwaysOnTop(true);
		setVisible(true);
		setModal(true);
		
		toFront();
	}
	
	public synchronized void playSound() {
		clip.setFramePosition(0);
		clip.start();

		System.gc();
		Runtime.getRuntime().gc();
	}
	
	
	public void addNotification(String[][] map, TrayPlugin trayplugin, int delay) {
		String title = "", subtitle = "", message = "", time = "", icon = "";
		
		for(String[] values: map) {
			if(values[0].equals("title")) {
				title = values[1];
			} else if(values[0].equals("subtitle")) {
				subtitle = values[1];
			} else if(values[0].equals("message")) {
				message = values[1];
			} else if(values[0].equals("time")) {
				time = values[1];
			} else if(values[0].equals("icon")) {
				icon = values[1];
			}
	    }
		
		Pattern p = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"); // RegEx Bedinung
		Matcher m = p.matcher(message);
		while (m.find()) { message = message.replace(m.group(0), "<span style='color:darkred'>[LINK]</span>"); }
		
		logo = Utils.getImage(icon, 50, 50, false);
		
		NMessage td = new NMessage(title, subtitle, message, time, logo, pin, unpin, this, delay);
		add(td, 0);

		if(trayplugin.shoudPlaySound()) { playSound(); }
		resetTo(getHeight() +td.getPreferredSize().height +5);
		validate();
	}

	public void removeNotification(NMessage td) {
		td.setVisible(false);
		resetTo(nc.getHeight() -td.getPreferredSize().height -5);
		td.removeAll();
		remove(td);
		td = null;
		
		validate();
		
		System.gc();
		Runtime.getRuntime().gc();
	}
	
	public void resetTo(int h) {
		nc.setBounds(nc.getX(), bounds.height -h, nc.getWidth(), h);
	}
}
