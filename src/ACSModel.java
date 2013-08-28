import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.hodgeproject.mediacontroller.network.NetworkTypes;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Click;
import com.hodgeproject.mediacontroller.network.NetworkTypes.ClientPass;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Keypress;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Move;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLRequest;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLUpdate;
import com.hodgeproject.mediacontroller.network.NetworkTypes.ServerMsg;

/**
 * Model for the Anvil Control Server
 * contains logic for server communication
 * @author Bryan Hodge
 *
 */
public class ACSModel extends Observable {
	
	// Main entry point
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				ACSModel m = new ACSModel();
				ACSView v = new ACSView(m);
				new ACSController(m,v); //MCController c = new MCController(m,v);
				
				v.setVisible(true);
			}
		});
	}
	
	
	// Constants
	public static final int QUICK_SLOTS = 6;				// Number of Quick Launch Slots
	
	private static final int DEFAULT_PORT = 4498;			// Port Server is listening on (TCP/UDP)
	private static final int CLIENT_PORT = 4499;			// Port the client listens to Multicast traffic on 	
	private static final int BROADCAST_PORT = 4497;			// Port for Multicast traffic on
	private static final String GROUP_ADDR = "230.1.1.1";	// Address of Multicast group to join
	private static final String SERV_NAME = "Anvil Server";	// Name the server sends, will later grab Compy name if null
	private static final String CONFIG_NAME = "Anvil.conf";	// Name of the Config file that will be created/loaded
	private static final boolean BROADCAST = true;			// Enable/Disable publicly broadcasting server location to clients

	// Member variables
	private List<ModelChangeListener> listeners;	// Registered Listener List
	private File[] quick_launch_files;				// Files in the Quick Launch bar
	private boolean serv_enabled;					// State of running server
	private boolean bcast_enabled;
	private boolean client_connected;				// Connection state of client
	private boolean psw_enable;
	private boolean waitingOnPassword;
	private int conID;								// The ID of the accepted connection

	
	private String password;						// Current password set for client
	private String salt;
	private String serv_name;						// Current broadcasted server name
	private String client_ip;						// IP of the connected client
	private String server_ip;						// IP of running server
	private int port;								// Port to accept connections on
	
	private Broadcaster bcast;						// Broadcaster runnable that publicly gives server location
	private Server server;
	private SmartRobot rob;
	private File config;

	public ACSModel(){
		listeners = new ArrayList<ModelChangeListener>();
		
		quick_launch_files = new File[QUICK_SLOTS];
		client_connected = false;		
		psw_enable = false;
		password = ""; 		
		salt = "";
		serv_name = SERV_NAME;		
		port = DEFAULT_PORT; 		
		client_ip = "---.---.---.---"; 
		server_ip = "---.---.---.---"; 
		conID = 0;
		bcast_enabled = BROADCAST;
		waitingOnPassword = false;
		
		loadConfig();
		
		//Create the Robot for movement control
		try {
			rob = new SmartRobot();
		} catch (AWTException e1) {
			System.err.println("Error creating Robot");
			e1.printStackTrace();
		}
		
		try {
			server_ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			server_ip = "---.---.---.---";
		}
		
		serv_enabled = false;
		toggleServer();      
	}
	
	/* Example Code TODO:EVENTUALLY REMOVE THIS
	//how to update UI from another thread (Like in the [server] Listener class below)
	EventQueue.invokeLater(new Runnable() {
        public void run() {
            try {
            	client_connected = true;
            	changedStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });	
	*/
	
	/**
	 * Load the server configuration from a file
	 */
	private void loadConfig() {
		boolean exists = false;
		config = new File(CONFIG_NAME);
		//TODO Better handle config file errors
		try {
			exists = config.exists();
			
			// keep default values if no config file found
			if (!exists) return;

			FileReader fstream = new FileReader(config);
			BufferedReader in = new BufferedReader(fstream);
			String line = null;

			int ql_size = 0; // must be set before a Quick Launch file path is read
			int ql_count = 0; // ql_count will count up until the ql_size and then stop.
			psw_enable = false;

			while ((line = in.readLine()) != null) {
				if (line.trim().equals("") || line.trim().substring(0, 2).equals("//")) continue;

				int linesize = line.trim().length();
				
				if (linesize > 8 && line.trim().substring(0, 8).equals("QL_FILES")) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						ql_size = 0;
					} else {
						ql_size = Integer.parseInt(result[1].trim());
					}
				} else if (linesize > 7 && line.trim().substring(0, 7).equals("QL_PATH") && ql_count < ql_size) {
					String[] result = line.trim().split("=");

					if (result.length == 1) {
						System.err.println("Error loading QL file path from config, Skipping");
					} else {
						String f_path = result[1].trim();
						quick_launch_files[ql_count] = new File(f_path);
						System.out.println("Loading QL file from config");
						ql_count++;
					}
				} else if (linesize > 9 && line.trim().substring(0, 9).equals("SERV_NAME")) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						serv_name = SERV_NAME;
					} else {
						serv_name = result[1].trim();
					}
				} else if (linesize > 12 && line.trim().substring(0, 12).equals("PSWD_ENABLED")) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						psw_enable = false;
					} else {
						if (result[1].trim().toUpperCase().equals("TRUE")) {
							psw_enable = true;
						}
					}
				} else if (linesize > 9 && line.trim().substring(0, 9).equals("SERV_PSWD") && psw_enable) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						password = "";
					} else {
						password = result[1].trim();
					}
					
				} else if (linesize > 9 && line.trim().substring(0, 9).equals("SERV_SALT") && psw_enable) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						salt = "";
					} else {
						salt = result[1].trim();
					}
				} else if (linesize > 9 && line.trim().substring(0, 9).equals("SERV_PORT")) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						port = DEFAULT_PORT;
					} else {
						port = Integer.parseInt(result[1].trim());
					}
				} else if (linesize > 17 && line.trim().substring(0, 17).equals("BROADCAST_ENABLED")) {
					String[] result = line.trim().split("=");
					if (result.length == 1) {
						bcast_enabled = true;
					} else {
						if (result[1].trim().toUpperCase().equals("FALSE")) {
							bcast_enabled = false;
						}
					}

				}
			}
			in.close();

		} catch (NumberFormatException e) {
			System.err.println("Error converting value to number while loading config, using default values!");
		} catch (IOException e) {
			System.err.println("Error loading config, using default values!");
		}

	}
	
	/**
	 * Save the server config settings to a file
	 * @return
	 */
	public boolean saveConfig(){
		
		if(config.delete()){
			try {
				
				if(config.createNewFile()){
					FileWriter fstream = new FileWriter(config);
					BufferedWriter out = new BufferedWriter(fstream);
					out.write("// Anvil Configuration File\n\n");
					out.write("// The number of Quick Launch files to keep track of.\n");
					
					int qlnum = 0;
					for(int x = 0; x < quick_launch_files.length; x++){
						if(quick_launch_files[x] != null) qlnum++;
					}
					out.write("QL_FILES="+qlnum+"\n");
					
					for(int x = 0; x < quick_launch_files.length; x++){
						if(quick_launch_files[x] != null){
							out.write("QL_PATH="+quick_launch_files[x].getCanonicalPath()+"\n");
						}
					}
					
					
					out.write("\n// The server name that is visible to clients.\n");
					out.write("SERV_NAME=" + serv_name + "\n\n");
					out.write("// Require password on connect.\n");
					
					if(psw_enable){
						out.write("PSWD_ENABLED=true\n");
						out.write("SERV_PSWD="+password+"\n");	
						out.write("SERV_SALT="+salt+"\n\n");
					} else {
						out.write("PSWD_ENABLED=false\n");
						out.write("SERV_PSWD=\n");
						out.write("SERV_SALT=\n\n");
					}
					
					out.write("// Port the server runs on.\n");
					out.write("SERV_PORT=" + port + "\n\n");
					out.write("// Publicly broadcast server presence over LAN.\n");
					if(bcast_enabled){
						out.write("BROADCAST_ENABLED=true\n");
					}else{
						out.write("BROADCAST_ENABLED=false\n");
					}
					out.close();
					System.out.println("Successfully saved configuration!");
					return true;
				}
				
			} catch (IOException e) {
				System.err.println("Error saving to new file");
			}
			
		} 
		
		// File was not saved
		return false; 
	}
	
	
	/**
	 * Adds a listener that can be notified of changes to the model
	 * @param mcl
	 */
	public void addModelChangeListener(ModelChangeListener mcl){
		listeners.add(mcl);
	}
	
	/**
	 * Modify the QL File list for the quick launch bar
	 * @param f		- The File to add to the Quick Launch bar
	 * @param index	- The index of the QLButton
	 */
	public void setQuickLaunchFile(File f, int index){
		if(index < 0 || index >= QUICK_SLOTS) return;
		quick_launch_files[index] = f;
		
		//sendQuickLaunchFiles();
		sendQuickLaunchFile(index);
		
		changedQLFiles();
	}
	
	
	/**
	 * Enables or Disables the server's communication
	 */
	public void toggleServer(){
		if(serv_enabled){
			System.out.println("Server Disabled");
			server.stop();
			if(bcast!=null && bcast.isRunning()){
				bcast.stop();
			}
			serv_enabled = false;
		}else{
			System.out.println("Server Enabled");
			
			//Create the Kryonet server
			server = new Server();
			NetworkTypes.register(server);
			server.addListener(new MyListener());
			
			try {
				server.bind(port, port);
			} catch (IOException e) {
				System.err.println("Error starting server on DEFAULT_PORT");
				e.printStackTrace();
			}
			
			//start server in its own thread, enable the broadcaster
	    	server.start();
			if(bcast_enabled){
				bcast = new Broadcaster();
				new Thread(bcast).start();
			}
			
			serv_enabled = true;
		}
		
		changedStatus();
		changedIP();
	}
	
	/**
	 * change the broadcasted servername
	 */
	public void changeServerName(String nName){
		serv_name = nName;
		//Update View
		changedServerName();
		System.out.println("SERVER NAME CHANGED");
	}
	
	public void setBroadcastEnable(boolean enabled){
		if(enabled){
			bcast_enabled = true;
			if(bcast == null || (bcast != null && !bcast.isRunning()) ){
				bcast = new Broadcaster();
				new Thread(bcast).start();
			}
		}else{
			bcast_enabled = false;
			if(bcast!=null && bcast.isRunning()){
				bcast.stop();
			}
		}
		
		
	}
	
	/**
	 * Change the password for the server
	 * @param enable - Whether passwords should be checked
	 * @param nPassword - The password to hash and check against (Field is ignored if passwords are being disabled)
	 */
	public void changePassword(boolean enable, char[] nPassword){
		
		if(enable){
			salt = SecurityUtil.generateSalt();
			password = SecurityUtil.sha256HexString(nPassword, salt);
			
			psw_enable = true;
			System.out.println("PASSWORD Set");
		} else {
			psw_enable = false;
			System.out.println("PASSWORD Disabled");
		}
		
		changedServerPassword();
	}
	
	
	/**
	 * Sends current data to all registered listeners
	 */
	public void requestUpdate(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedStatus(serv_enabled,client_connected);
			mcl.changedClientIP(client_ip);
			mcl.changedServerIP(server_ip);
			mcl.changedQLFiles(quick_launch_files);
			mcl.changedServerName(serv_name);
			
			mcl.changedServerPswd(psw_enable);
			mcl.changedBroadcast(bcast_enabled);
		}
	}
	
	//***************************************
	//******************** Model Notification Methods
	//********************
	
	/**
	 * Notify listeners that the QL File list has been modified
	 * TODO: confirm no sync issues
	 */
	protected void changedQLFiles(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedQLFiles(quick_launch_files);
		}
	}
	
	/**
	 * Notify listeners that the servers status has changed
	 */
	protected void changedStatus(){	
		for(ModelChangeListener mcl : listeners){	
			mcl.changedStatus(serv_enabled,client_connected);
		}
	}
	
	/**
	 * Notify listeners of the clients IP
	 */
	protected void changedIP(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedClientIP(client_ip);
		}
	}
	
	protected void changedServerName(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedServerName(serv_name);
		}
	}
	
	protected void changedServerPassword(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedServerPswd(psw_enable);
		}
	}
	
	protected void changedBroadcastEnable(){
		for(ModelChangeListener mcl : listeners){
			mcl.changedBroadcast(bcast_enabled);
		}
	}
	
	//***************************************
	//******************** Server Communication Functions
	//********************
	
	private void sendQuickLaunchFiles(){
		if(client_connected){
			// Notify connected client of new files (TCP)						
			for(int x = 0; x < quick_launch_files.length; x++){
				sendQuickLaunchFile(x);
			}			
		}
	}
	
	private void sendQuickLaunchFile(int index){
		File f = quick_launch_files[index];
		if(f==null) return;
		
		QLUpdate ql = new QLUpdate();
		ql.itemIndex = index;
		ql.fileName = f.getName();
		
    	// Generate the png's to send to android
    	FileSystemView fsv = FileSystemView.getFileSystemView();
    	Icon ico = fsv.getSystemIcon(f);
    	BufferedImage bi = new BufferedImage(ico.getIconWidth(), ico.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    	Graphics big = bi.getGraphics();
    	ico.paintIcon(null, big, 0, 0);  
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	try {
    		ImageIO.write(bi, "png", baos);
		} catch (IOException e1) {
			System.err.println("Error generating icon");
			return;
		}
    	
		// just send the one file
		byte[] imageBytes = baos.toByteArray();

		ql.icon = new byte[imageBytes.length];
		ql.icon = imageBytes;

		if (client_connected) {
			System.out.println("Sending QLUpdate: Icon: [" + ql.icon.length + "] String: " + ql.fileName.getBytes().length);
			server.sendToAllTCP(ql);
		}
	}
	
	private void openFile(int fileIndex){
		if(quick_launch_files[fileIndex].canExecute()){
			try {
				System.out.println("Attempting to open: "+quick_launch_files[fileIndex].getAbsolutePath());
				Desktop.getDesktop().open( quick_launch_files[fileIndex] );
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Can't open file: "+ quick_launch_files[fileIndex].getName());
		}
	}
	
	//***************************************
	//******************** Server Communication Classes
	//********************
	

	/**
	 * Handles communication from the server for both TCP and UDP
	 * TODO: Put UI changes in EventQueue
	 * @author Bryan Hodge
	 */
	private class MyListener extends Listener{
		
		public static final int PASSWORD_TIMEOUT = 5000;
		public Timer connectionTimer;
		
		
		/**
		 * Invoked when a client connects
		 */
		public void connected(final Connection connection){
			System.out.println("Recieved Connection " + connection.getID());
			
			if(!client_connected && conID == 0 && !waitingOnPassword){
				
				//client_connected = true;
				//conID = connection.getID();
				
				if(psw_enable && !password.equals("")){
					// store the connection ID and await password a password responce
					waitingOnPassword = true;
					//start timer
					connectionTimer = new Timer();
					connectionTimer.schedule(new TimerTask(){

						@Override
						public void run() {
							// TIMES UP, REJECT AND CLOSE CONNECTION!
							ServerMsg msg = new ServerMsg();
							msg.connectionAccepted = false;
							msg.incorrectPassword = false;
							connection.sendTCP(msg);
							connection.close();
														
							System.out.println("Connection attempt failed (No Password Supplied)");
							waitingOnPassword = false;
							
							client_connected = false;
							conID = 0;
						}
						
					}, PASSWORD_TIMEOUT);
				} else {
					waitingOnPassword = false;
					acceptConnection(connection);
				}
				
				
			} else {
				System.out.println("Connection attempt failed (Connection in use)");
				connection.close();
			}
		}
		
		/**
		 * Invoked when any message is received (TCP or UDP)
		 */
		public void received(Connection connection, Object object){
			if(object instanceof Move){
				Move m = (Move)object;
				Point curPos = MouseInfo.getPointerInfo().getLocation();
				rob.mouseMove(curPos.x + m.x, curPos.y + m.y);
			} else if(object instanceof Click){
				Click c = (Click)object;
				if(c.leftClick){
					if(c.clickDown){
						rob.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					}else{
						rob.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					}
				} else {
					if(c.clickDown){
						rob.mousePress(InputEvent.BUTTON3_DOWN_MASK);
					}else{
						rob.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					}
				}
			}else if(object instanceof Keypress){
				//handle key press
				Keypress kp = (Keypress)object;
				int keycode = kp.keycode;
				System.out.println("Keypress: "+keycode);
				rob.typeKeycode(keycode);
			}else if(object instanceof QLRequest){
				QLRequest qlr = (QLRequest)object;
				openFile(qlr.fileRequested);
			}else if(waitingOnPassword && object instanceof ClientPass){
				//check password accept or reject
				ClientPass cpass = (ClientPass)object;
				if(SecurityUtil.sha256HexString(cpass.password, salt).equals(password)){
					// accept connection
					acceptConnection(connection);
				} else {
					if(connectionTimer!=null) connectionTimer.cancel();
					
					ServerMsg msg = new ServerMsg();
					msg.connectionAccepted = false;
					msg.incorrectPassword = true;
					connection.sendTCP(msg);
					System.out.println("Connection attempt failed (Incorrect Password)");
					connection.close();
					
					waitingOnPassword = false;
					client_connected = false;
					conID = 0;
				}
			}
		}
		
		public void acceptConnection(Connection con){
			if(connectionTimer!=null){
				connectionTimer.cancel();
			}
			
			//stop broadcasting
			if(bcast!=null && bcast.isRunning()){
				bcast.stop();
			}
			
			waitingOnPassword = false;
			client_connected = true;
			conID = con.getID();
			
			ServerMsg msg = new ServerMsg();
			msg.connectionAccepted = true;
			con.sendTCP(msg);
			
			client_ip = con.getRemoteAddressTCP().getHostString();
			changedStatus();
			changedIP();
			
			// send the QL list
			sendQuickLaunchFiles();	
		}
		
		/**
		 * Invoked when a client disconnects
		 */
		public void disconnected(Connection con){
			if(client_connected && con.getID() == conID){
				//start broadcasting
				if(bcast_enabled){
					bcast = new Broadcaster();
					new Thread(bcast).start();
				}
				
				client_ip = "---.---.---.---";
				client_connected = false;
				conID = 0;
				changedStatus();
				changedIP();
			}
		}
	}
	
	
	
	/**
	 * Broadcasts servers location to listening clients in group
	 * runs in another thread.
	 * @author Bryan Hodge
	 */
	private class Broadcaster implements Runnable {
		private static final int BUFF_SIZE = 30;
		private static final int BCAST_TIMEOUT = 2000;			// Timeout for socket
		private volatile boolean running;						// Flag to stop the runnable
		private MulticastSocket mcastSock;						// Socket to listen on
		
		public Broadcaster(){
			running = false;
		}
		
		public void run() {
			try {
				mcastSock = new MulticastSocket(BROADCAST_PORT);
				mcastSock.joinGroup(InetAddress.getByName(GROUP_ADDR));
				System.out.println("Broadcasting Enabled");
				mcastSock.setSoTimeout(BCAST_TIMEOUT);
				running = true;
				while(running){
					try{
						// Wait for a client to broadcast that it's looking for servers
						byte[] buf = new byte[BUFF_SIZE];
						DatagramPacket recv = new DatagramPacket(buf, buf.length);
						mcastSock.receive(recv);
						
						String recvMsg = new String(recv.getData());
						System.out.println("Recieved: " + recvMsg  );
						
						// Broadcast to group the servers name/ip
						byte[] bname = (serv_name).getBytes();
						
						byte[] msg = new byte[1 + bname.length];
						System.arraycopy(bname, 0, msg, 1, bname.length);	
						if(psw_enable){
							msg[0] = 1;
						} else {
							msg[0] = 0;
						}
						
						// Respond to the client who made the request
						DatagramPacket lookHere = new DatagramPacket(msg, msg.length, recv.getAddress(), CLIENT_PORT);
						
						mcastSock.send(lookHere);
					} catch( SocketTimeoutException e){
						continue;
					}
				}
				
				mcastSock.leaveGroup(InetAddress.getByName(GROUP_ADDR));
				mcastSock.close();
				
			} catch (IOException e) {
				System.err.println("Multicast Socket I/O Error");
			} finally {
				System.out.println("Broadcasting Disabled");
			}
		}
		
		public boolean isRunning(){
			return running;
		}
		
		public void stop() { 
			running = false; 
		}
		
	}
}
