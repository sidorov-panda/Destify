package Destify;

import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.kylinworks.IPngConverter;

public class Starter {	
	public static void main(String args[]) throws Exception {
		try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); } catch (Exception e) {}

		String fileDir = Utils.defaultDirectory() +"/Destify/settings.ini";
		IniFile f = new IniFile(fileDir);
		
		TrayPlugin tp = null;
		String addr = null;
		
		int timeout = 10000;
		int PORT = f.getInt("Destify", "port", 3128);
		
		ServerSocket serverSocket = null;
		
		try {
			addr = Utils.getAddr();
			tp = new TrayPlugin(addr);

			serverSocket = new ServerSocket(PORT, 10, InetAddress.getByName(addr));
			serverSocket.setSoTimeout(timeout);
			
			NController controller = new NController();
			tp.displayMessage("Please set following IP address and Portnumber on your iDevice: " +addr +":" +PORT); 
			tp.setInfo(addr +":" +PORT);
			
		    IPngConverter converter = new IPngConverter();
		    Socket connection = null;
		    
			while(true) {
				//System.out.println("Waiting on " +addr +":" +serverSocket.getLocalPort());
				ServerParser sp = null;
				
				f = new IniFile(fileDir);
				int delay = f.getInt("Destify", "delay", 10);
				String cipher = f.getString("Destify", "cipher", "");
				
				try {
					connection = serverSocket.accept();
					sp = new ServerParser(connection, controller, converter, tp, delay, cipher);
					sp.run();
					sp = null;
					
				} catch (Exception e) { }
				
				String taddr = Utils.getAddr();
				PORT = f.getInt("Destify", "port", 3128);
				
				if(!addr.equals(taddr) || PORT != serverSocket.getLocalPort()) {
					serverSocket.close();
					serverSocket = new ServerSocket(PORT, 10, InetAddress.getByName(taddr));
					serverSocket.setSoTimeout(timeout);
					
					tp.displayMessage("IP address and/or Portnumber changed, please use following " +taddr +":" +PORT);
					tp.setInfo(taddr +":" +PORT);
					addr = taddr;
				}
				taddr = null;
				f = null;

				System.gc();
				Runtime.getRuntime().gc();
			}
			
		} catch (BindException e) {
			JOptionPane.showMessageDialog(null,
					"There is already an instance running!",
					"Destify",
					JOptionPane.WARNING_MESSAGE);
			
			System.exit(0);
		}
	}
}
