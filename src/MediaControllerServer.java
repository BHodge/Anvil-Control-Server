import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Server for the Media Controller Android App
 * @author Bryan Hodge
 * TODO: FIX TCP/UDP parts from not recognizing user connection loss
 */
public class MediaControllerServer{
	private static final int SERVER_PORT = 4498; 			// Port for TCP/UDP traffic to listen on
	private static final int BROADCAST_PORT = 4497;			// Port for Multicast traffic on
	private static final int CLIENT_PORT = 4499;			// Port the client listens to Multicast traffic on 
	private static final String GROUP_ADDR = "230.1.1.1";	// Address of Multicast group to join
	private static final String SERV_NAME = "TESTSERV";		// Name the server sends, will later grab Compy name if null
	private static final boolean BROADCAST = true;			// Enable/Disable publicly broadcasting server location to clients
	private static final int TIMEOUT = 300000;				// Timeout for socket in milliseconds (5 mins)
	
	private ServerSocket serverSock;	// TCP Server Socket for communications
	private Broadcaster bcast;			// Broadcaster runnable that publicly gives server location
	private SmartRobot robot;			// Robot used to control Mouse/Keyboard events
	
	public static void main(String [] args){
		MediaControllerServer mcs = new MediaControllerServer();
		mcs.startServer();
	}
	
	public MediaControllerServer(){
		if(BROADCAST){
			bcast = new Broadcaster();
			new Thread(bcast).start();
		}
	}
	
	public void startServer(){
		try {
			robot = new SmartRobot();
			serverSock = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			System.err.println("Error with ServerSocket");
			e.printStackTrace();
			return;
		} catch (AWTException e) {
			System.err.println("Error creating Robot");
			e.printStackTrace();
			return;
		}
		
		System.out.println ("Server Waiting on port: "+SERVER_PORT);
			
			
		while(true){
			Socket connectSocket = null;
			try{
				connectSocket = serverSock.accept();
				connectSocket.setSoTimeout(TIMEOUT);
				//serverSock.setReceiveBufferSize(64000);
			} catch(IOException e){
				// wait for a new connection
				continue;
			} 
			
			
			// disable broadcasting if needed
			if(BROADCAST && bcast.isRunning()){
				bcast.stop();
			}

			System.out.println("Recieved Connection: "+ connectSocket.getInetAddress().toString());
			
			//Start UDP mouse listener
			UDPReciever udpserv = new UDPReciever();
			new Thread(udpserv).start();
			
			
			BufferedReader commandInput = null;
			try{	
				commandInput = new BufferedReader( new InputStreamReader(connectSocket.getInputStream() ));
			} catch(IOException e){
				// wait on new connection
				System.err.println("Did something happen?");
				continue;
			}
			
			
			while(true){
				
				String command = null;
				try {
				
					if((command = commandInput.readLine()) == null){ 
						System.err.println("got null, end connection"); 
						break;
					}
					
				} catch(SocketTimeoutException e){
					System.out.println("Connection Timedout");
					break;
				} catch(EOFException e){
					System.err.println("Other side hung up");
					break;
				} catch (IOException e) {
					System.err.println("Something happened-close connection");
					break;
				}

				
				System.out.print("RECIEVED ");
				String[] comA = command.split(" ");
				if(comA[0].equals("1")){
					// Key press
					pressKey(comA[1].charAt(0));
					System.out.println("KEY PRESS!");
				} else if(comA[0].equals("2")){
					// Keep alive
					System.out.println("PING!");
				} else {
					System.err.println("Unknown command");
				}
			}
					
			udpserv.stopRun();
			try{
				commandInput.close();
				connectSocket.close();
			} catch (IOException e){
				System.err.println("Error closing socket--Ignoring");
			}
			
			//re enable broadcasting if needed
			if(BROADCAST && !bcast.isRunning()){
				bcast = new Broadcaster();
				new Thread(bcast).start();
			}
			
		}
	}


		
	
	
	/*#####################################
	 *	SERVER COMMANDS
	 *
	 */
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void moveMouse(int x, int y) {
		//get mouse pos
		Point curPos = MouseInfo.getPointerInfo().getLocation();

		robot.mouseMove(curPos.x + x, curPos.y + y);
	}
	
	/**
	 * 
	 * @param leftclick
	 */
	public void clickMouse(boolean leftclick){
		if(leftclick){
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		} else {
			robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);			
		}
	}
	
	public void pressKey(char key){
		robot.typeChar(key);
	}
	
	/*#####################################
	 *	SERVER Components
	 *
	 */	
	
	/**
	 * UDP Listener for mouse events sent by client
	 * TODO: Possibly throw an exception
	 * @author Bryan Hodge
	 */
	class UDPReciever implements Runnable {
		private static final int BUF_SIZE = 80;
		private static final int SO_TIMEOUT = 1000;
		
		private volatile boolean running;
		private DatagramSocket dsock;
		
		public UDPReciever(){
			running = false;
		}
		
		public void run(){
			try {
				dsock = new DatagramSocket(SERVER_PORT);
				dsock.setSoTimeout(SO_TIMEOUT);
				dsock.setReceiveBufferSize(BUF_SIZE); //TODO:set to min packet size!

			} catch (BindException e){
				System.err.println("UDPSocket in use!");
				
			} catch (SocketException e) {
				System.err.println("Error with UDPSocket");
				e.printStackTrace();
			} 	
			
			System.out.println("Server listening for mouse events");
			running = true;			
				
			byte[] buff = new byte[BUF_SIZE];
			while(running){
				DatagramPacket  recv = new DatagramPacket(buff, buff.length);
				
				try{
					dsock.receive(recv);
				} catch(SocketTimeoutException e){
					continue;
				}catch(IOException e){
					break;
				}
				

				String com = new String(recv.getData(),0,recv.getLength());
				//if(com == null) break;
				
				int[] moves = new int[3];
				int x = 0;
				for(String num: com.split(" ")){
					moves[x] = Integer.parseInt(num);
					x++;
				}
				moveMouse(moves[1],moves[2]);	
				
			}
			
			dsock.close();

			running = false;
		}
		
		public void stopRun(){
			running = false;
		}
	}
	
	
	
	/**
	 * Broadcasts servers location to listening clients in group
	 * runs in another thread.
	 * @author Bryan Hodge
	 *
	 */
	static class Broadcaster implements Runnable {
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
						byte[] msg = (SERV_NAME).getBytes();
						DatagramPacket lookHere = new DatagramPacket(msg, msg.length, InetAddress.getByName(GROUP_ADDR), CLIENT_PORT);
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
