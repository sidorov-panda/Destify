package Destify;

import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

public class TrayPlugin {	
	private final TrayIcon trayIcon;
	private final PopupMenu popup;
	private final SystemTray tray;
	private String info = "No informations yet.";
	private boolean mutestate = true, ignorestate = true;
	
	TrayPlugin(final String addr) {		
		popup = new PopupMenu();
		tray = SystemTray.getSystemTray();
		Image trayImage = Utils.getImage("/Resources/Logos/Destify@2x.png", tray.getTrayIconSize().width, -1, true).getImage();
		trayIcon = new TrayIcon(trayImage, "Destify");
        
        MenuItem aboutItem = new MenuItem("Destify by iLendSoft \u00a9 2013");
        CheckboxMenuItem muteUnmute = new CheckboxMenuItem("Mute sounds");
        CheckboxMenuItem ignoreMessages = new CheckboxMenuItem("Ignore incoming messages");
        MenuItem protocollItem = new MenuItem("Show protocol");
        MenuItem optionsItem = new MenuItem("Options");
        MenuItem gb = new MenuItem("Run Garbage Collector");
        MenuItem exitItem = new MenuItem("Exit");
        
        aboutItem.setEnabled(false);
        protocollItem.setEnabled(false);
        //optionsItem.setEnabled(false);

        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(muteUnmute);
        popup.add(ignoreMessages);
        popup.add(protocollItem);
        popup.add(optionsItem);
        popup.add(gb);
        popup.addSeparator();
        popup.add(exitItem);   
        
        trayIcon.setPopupMenu(popup);
        try { tray.add(trayIcon);  } catch (Exception e) { System.out.println("TrayIcon could not be added."); }
        
        protocollItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.gc();
            }
        });
        
        gb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		System.gc();
        		Runtime.getRuntime().gc();
            }
        });
        
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	displayMessage(info);
            }
        });
         
        muteUnmute.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                
                if (cb1Id == ItemEvent.SELECTED){
                	mutestate = false;
                } else {
                	mutestate = true;
                }
            }
        });
        
        ignoreMessages.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                
                if (cb1Id == ItemEvent.SELECTED){
                	ignorestate = false;
                } else {
                	ignorestate = true;
                }
            }
        });
         
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//dbc.closeConnection();
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
        
        optionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try { new ProcessBuilder("java", "-jar", Utils.defaultDirectory() +"/Destify/Settings.jar", addr).start(); } catch (IOException e1) { }
            }
        });
	}
	
	public void displayMessage(String s) {
		this.info = s;
		trayIcon.displayMessage("Destify Message", info, MessageType.INFO);
	}

	public boolean shoudPlaySound() {
		return this.mutestate;
	}
	
	public boolean shoudShowNotification() {
		return this.ignorestate;
	}

	public void setInfo(String s) {
		this.info = s;
	}
}
