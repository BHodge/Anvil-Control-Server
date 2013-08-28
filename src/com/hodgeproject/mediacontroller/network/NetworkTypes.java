package com.hodgeproject.mediacontroller.network;

//import javax.swing.Icon;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class NetworkTypes {
	static public void register(EndPoint endpoint){
		Kryo kryo = endpoint.getKryo();
		kryo.register(Keypress.class);
		kryo.register(Move.class);
		kryo.register(Click.class);
		kryo.register(byte[].class);
		kryo.register(byte[][].class);
		kryo.register(String[].class);
		kryo.register(QLUpdate.class);
		kryo.register(QLRequest.class);
		kryo.register(ServerMsg.class);
		kryo.register(ClientPass.class);
	}
	
	static public class Keypress{
		public int keycode;
	}
	
	/**
	 * Designates delta mouse movement
	 * @author BMCJ
	 */
	static public class Move{
		public int x;
		public int y;
	}
	
	/**
	 * Designates a mouse click
	 * @author BMCJ
	 */
	static public class Click{
		public boolean leftClick;
		public boolean clickDown;
	}
	
	/**
	 * Designates changes to the Quick Launch bar
	 * @author BMCJ
	 */
	static public class QLUpdate{
		public int itemIndex;
		public byte[] icon;
		public String fileName;
	}
	
	/**
	 * Designates a request for the given Quick Launch item index
	 * @author BMCJ
	 */
	static public class QLRequest{
		public int fileRequested;
	}
	
	/**
	 * Designates a message from the server
	 * connectionAccepted - Whether the connection was accepted or rejected
	 * incorrectPassword - Whether the password didn't match or was not supplied
	 * @author Bryan Hodge
	 *
	 */
	static public class ServerMsg{
		public boolean connectionAccepted;
		public boolean incorrectPassword;
	}
	
	/**
	 * Password request from client
	 * @author Bryan Hodge
	 *
	 */
	static public class ClientPass{
		public String password;
	}
}
