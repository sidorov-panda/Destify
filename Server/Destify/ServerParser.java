package Destify;

import java.io.*;
import java.net.*;
import java.util.*;

import com.kylinworks.IPngConverter;

public class ServerParser {
	private static final String HTML_START = "<html><head><title>DesktopNotify by iLendSoft</title></head><body>";
	private static final String HTML_END = "</body></html>";
	private final String PATH = Utils.defaultDirectory() +"/Destify/";

	private Socket client = null;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private NController controller = null;
	private String currentLine = null;
	private String responseString;
	private IPngConverter converter;
	private TrayPlugin trayplugin;
	private int delay;
	private String cipher;
	
	public ServerParser(Socket client, NController controller, IPngConverter converter, TrayPlugin trayplugin, int delay, String cipher) {
		this.client = client;
		this.controller = controller;
		this.converter = converter;
		this.trayplugin = trayplugin;
		this.delay = delay;
		this.cipher = cipher;
		
		responseString = ServerParser.HTML_START 
					+"<meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=http://ils.eu.gg\">"
				+ServerParser.HTML_END;
	}

	public void run() {		
		try {
			inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outToClient = new DataOutputStream(client.getOutputStream());

			currentLine = inFromClient.readLine();
			String headerLine = currentLine;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			if (httpMethod.equals("GET")) {
				if (httpQueryString.equals("/")) {
					sendResponse(202, responseString);
					
				} else {
					sendResponse(404, "Hey... what are you doing here? There is nothing to see... go away!");
				}

			} else { // POST request
				if(trayplugin.shoudShowNotification()) {
					while(!currentLine.equals("--=--")) { currentLine = inFromClient.readLine(); }
					
					String map[][] = new String[6][2];
					currentLine = inFromClient.readLine();
	
					String last = "", type = "";
					int index = -1;
					boolean cipherFailed = false, shouldDecode = false;
					String ts = "";
					
					if(currentLine.indexOf("check") == 0) {
						type = "check";
						ts = new String(currentLine.substring(type.length() +1, currentLine.length()));
						
						if(!ts.equals("Destify")) {
							ts = Utils.decode(ts, cipher);
							
							if(ts.equals("Destify")) {
								shouldDecode = true;
								
							} else {
								cipherFailed = true;
							}
						}
						
						currentLine = inFromClient.readLine();
					} 
					
					if(!cipherFailed) {
						while(!currentLine.equals("--=--")) {
							if(currentLine.indexOf("title") == 0) {
								last = ""; type = "title";
								ts = new String(currentLine.substring(type.length() +1, currentLine.length()));
								if(shouldDecode) { last += Utils.decode(ts, cipher); } else { last += ts; }
								index ++;
								
							} else if(currentLine.indexOf("subtitle") == 0) {
								last = ""; type = "subtitle";
								ts = new String(currentLine.substring(type.length() +1, currentLine.length()));
								if(shouldDecode) { last += Utils.decode(ts, cipher); } else { last += ts; }
								index ++;
								
							} else if(currentLine.indexOf("message") == 0) {
								last = "";  type = "message";
								ts = new String(currentLine.substring(type.length() +1, currentLine.length()));
								if(shouldDecode) {last += Utils.decode(ts, cipher); } else { last += ts; }
								index ++;
								
							} else if(currentLine.indexOf("topic") == 0) {
								last = ""; type = "topic";
								ts = new String(currentLine.substring(type.length() +1, currentLine.length()));
								if(shouldDecode) { last += Utils.decode(ts, cipher); } else { last += ts; }
								index ++;
								
							} else if(currentLine.indexOf("time") == 0) {
								last = ""; type = "time";
								last += new String(currentLine.substring(type.length() +1, currentLine.length()));
								index ++;
								
							} else if(currentLine.indexOf("icon") == 0) {
								last = ""; type = "icon";
								last += new String(currentLine.substring(type.length() +1, currentLine.length()));
								index ++;
								
							} else { 
								ts = new String(currentLine);
								if(shouldDecode) { last += "<br/>" +Utils.decode(ts, cipher, 5); } else { last += "<br/>" +ts; }
							}
														
							map[index][0] = type.trim().equals("(null)") ? "": type.trim();
							map[index][1] = last.trim().equals("(null)") ? "": new String(last.getBytes(), "UTF-8").trim();

							currentLine = inFromClient.readLine();
						}
						
						if(map[3][0].equals("topic") && map[3][1].equals("iMessage")) {
							map[0][1] = "iMessage: " +map[0][1];
						}
						
						if(map[5][0].equals("icon")) {
							String fname = Utils.MD5(map.toString());
							byte[] bytes = Utils.hexStringToByteArray(map[5][1]);
							
							FileOutputStream fos = new FileOutputStream(PATH +fname +".png");
						    fos.write(bytes);
						    fos.close();
						    fos = null;
						    bytes = null;
						    
						    map[5][1] = PATH +fname +".png";
						    
						    File imagefile = new File(map[5][1]);
						    converter.convert(imagefile);
						    imagefile.delete();
						    imagefile = null;
						    
						    map[5][1] = PATH +fname +"-new.png";
						}
						
					    controller.addNotification(map, trayplugin, delay);
					    
						if(map[5][0].equals("icon")) {
						    File imagefile = new File(map[5][1]);
						    imagefile.delete();
						    imagefile = null;
						}
					    
					    map = null;
					    
						System.gc();
						Runtime.getRuntime().gc();
						
					} else {
						trayplugin.displayMessage("Decoding failed! Please check your Cipher Key!");
					}
				}
				
				sendResponse(200, "Destify... Roger that!");
			}
		} catch (Exception e) { }
	}
	
	public void sendResponse(int statusCode, String responseString) throws Exception {
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer for Destify";
		String contentLengthLine = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";

		if (statusCode == 200) {
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		} else {
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		}

		responseString = ServerParser.HTML_START +responseString +ServerParser.HTML_END;
		contentLengthLine = "Content-Length: " +responseString.length() +"\r\n";

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");
		outToClient.writeBytes(responseString);
		
		inFromClient.close();
		outToClient.close();
		
		inFromClient = null;
	    currentLine = null;
	}
}
