import java.io.*; 
import java.net.*; 
import java.text.SimpleDateFormat; 
import java.util.*; 

/**
 * The server that can be run both as a console application or a GUI 
 * uniqueId: a unique ID for each connection
 * al: an ArrayList to keep the list of the Client
 * sg: if I am in a GUI
 * sdf: to display time
 * port: the port number to listen for connection
 * keepGoing: the boolean that will be turned of to stop the server
 */ 
public class Server { 
	private static int uniqueId; 
	private ArrayList<ClientThread> al;  
	private ServerGUI sg; 
	private SimpleDateFormat sdf; 
	private int port;  
	private boolean keepGoing; 


	/**
	 *  server constructor that receive the port to listen to for connection as parameter 
	 *  in console
	 *  @param port the port number to listen for connection 
	 */ 
	public Server(int port) { 
		this(port, null); 
	} 

	/**
	 *  server constructor that receive the port to listen to for connection as parameter 
	 *  in gui
	 *  @param port the port number to listen for connection
	 *  @param sg represent the gui
	 */ 
	public Server(int port, ServerGUI sg) { 
		// GUI or not 
		this.sg = sg; 
		// the port 
		this.port = port; 
		// to display hh:mm:ss 
		sdf = new SimpleDateFormat("HH:mm:ss"); 
		// ArrayList for the Client list 
		al = new ArrayList<ClientThread>(); 
	} 

	public void start() { 
		keepGoing = true; 
		/* create socket server and wait for connection requests */ 
		try  
		{ 
			// the socket used by the server 
			ServerSocket serverSocket = new ServerSocket(port); 

			// infinite loop to wait for connections 
			while(keepGoing)  
			{ 
				// format message saying we are waiting 
				display("Server waiting for Clients on port " + port + "."); 

				Socket socket = serverSocket.accept();   // accept connection 
				// if I was asked to stop 
				if(!keepGoing) 
					break; 
				ClientThread t = new ClientThread(socket);  // make a thread of it 
				al.add(t);         // save it in the ArrayList 
				t.start(); 
			} 
			// I was asked to stop 
			try { 
				serverSocket.close(); 
				for(int i = 0; i < al.size(); ++i) { 
					ClientThread tc = al.get(i); 
					try { 
						tc.sInput.close(); 
						tc.sOutput.close(); 
						tc.socket.close(); 
					} 
					catch(IOException ioE) { 
						// not much I can do 
					} 
				} 
			} 
			catch(Exception e) { 
				display("Exception closing the server and clients: " + e); 
			} 
		} 
		// something went bad 
		catch (IOException e) { 
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n"; 
			display(msg); 
		} 
	}   
	/**
	 * For the GUI to stop the server 
	 */ 
	protected void stop() { 
		keepGoing = false; 
		// connect to myself as Client to exit statement  
		// Socket socket = serverSocket.accept(); 
		try { 
			new Socket("localhost", port); 
		} 
		catch(Exception e) { 
			// nothing I can really do 
		} 
	} 
	/**
	 * Display an event (not a message) to the console or the GUI 
	 * @param msg represent the message
	 */ 
	private void display(String msg) { 
		String time = sdf.format(new Date()) + " " + msg; 
		if(sg == null) 
			System.out.println(time); 
		else 
			sg.appendEvent(time + "\n"); 
	} 
	/**
	 *  to broadcast a message to all Clients
	 *  @param username represent the sender username 
	 *  @param message represent the message is being broadcast 
	 */ 
	private synchronized void broadcast(String username, String message) { 
		// add HH:mm:ss and \n to the message 
		String time = sdf.format(new Date()); 
		String messageLf = time + " " + username + ": " + message + "\n"; 
		// display message on console or GUI 
		if(sg == null) 
			System.out.print(messageLf); 
		else 
			sg.appendRoom(messageLf);     // append in the room window 

		String name;
		String[] name1 = message.split(" ");
		name = name1[0];
		System.out.println(name);

		// we loop in reverse order in case we would have to remove a Client 
		// because it has disconnected 
		if(message.startsWith("%%")) {
			for(int i = al.size(); --i >= 0;) { 
				ClientThread ct = al.get(i);
				//System.out.println(ct.username);
				// try to write to the Client if it fails remove it from the list 
				if(username.equals(ct.username) || name.equals("%%" + ct.username)) {
					String msg1 = foo(message);
					if(!ct.writeMsg(time + " " + username + ": " + msg1 + "\n")) { 
						//ct.writeMsg(ct.username + "disconected");
						al.remove(i); 
						display("Disconnected Client " + ct.username + " removed from list."); 
					}
				}
			}
		} 
		else
			for(int i = al.size(); --i >= 0;) { 
				ClientThread ct = al.get(i);
				//System.out.println(ct.username);
				// try to write to the Client if it fails remove it from the list 
				if(!ct.writeMsg(messageLf)) {
					//ct.writeMsg(ct.username + "disconected");
					al.remove(i); 
					display("Disconnected Client " + ct.username + " removed from list."); 
				}
			}
	}
	
	/**
	 *  to cut the message 
	 *  @param msgg the message that we wont to cut
	 *  @return the message after the cut 
	 */ 
	public String foo (String msgg) {
		int start = msgg.indexOf(" ");
		String cutMsg = msgg.substring(start + 1);
		return cutMsg;	
	}


	/**
	 *  for a client who logoff using the LOGOUT message  
	 *  @param id the id that we wont to remove from the list
	 */ 
	synchronized void remove(int id) { 
		// scan the array list until we found the Id 
		for(int i = 0; i < al.size(); ++i) { 
			ClientThread ct = al.get(i); 
			// found it 
			if(ct.id == id) { 
				al.remove(i); 
				return; 
			} 
		} 
	} 

	/**
	 *  To run as a console application just open a console window and:  
	 *  java Server 
	 *  java Server portNumber 
	 * If the port number is not specified 1500 is used 
	 * @param args main
	 */  
	public static void main(String[] args) { 
		// start server on port 1500 unless a PortNumber is specified  
		int portNumber = 1500; 
		switch(args.length) { 
		case 1: 
			try { 
				portNumber = Integer.parseInt(args[0]); 
			} 
			catch(Exception e) { 
				System.out.println("Invalid port number."); 
				System.out.println("Usage is: > java Server [portNumber]"); 
				return; 
			} 
		case 0: 
			break; 
		default: 
			System.out.println("Usage is: > java Server [portNumber]"); 
			return; 

		} 
		// create a server object and start it 
		Server server = new Server(portNumber); 
		server.start(); 
	} 
	
	public boolean equals (Server s) {
		if(this.port != s.port || this.sg != s.sg)
			return false;
		return true;
	}

	/** One instance of this thread will run for each client */ 
	class ClientThread extends Thread { 
		// the socket where to listen/talk 
		Socket socket; 
		ObjectInputStream sInput; 
		ObjectOutputStream sOutput; 
		// my unique id (easier for deconnection) 
		int id; 
		// the Username of the Client 
		String username; 
		// the only type of message a will receive 
		ChatMessage cm; 
		// the date I connect 
		String date; 

		/**
		 *  ClientThread constructor that receive the socket 
		 *  @param socket represent the socket where to listen/talk   
		 */ 
		ClientThread(Socket socket) { 
			// a unique id 
			id = ++uniqueId; 
			this.socket = socket; 
			/* Creating both Data Stream */ 
			System.out.println("Thread trying to create Object Input/Output Streams"); 
			try 
			{ 
				// create output first 
				sOutput = new ObjectOutputStream(socket.getOutputStream()); 
				sInput  = new ObjectInputStream(socket.getInputStream()); 
				// read the username 
				username = (String) sInput.readObject(); 
				display(username + " just connected."); 
			} 
			catch (IOException e) { 
				display("Exception creating new Input/output Streams: " + e); 
				return; 
			} 
			// have to catch ClassNotFoundException 
			// but I read a String, I am sure it will work 
			catch (ClassNotFoundException e) { 
			} 
			date = new Date().toString() + "\n"; 
		} 

		// what will run forever 
		public void run() { 
			// to loop until LOGOUT 
			boolean keepGoing = true; 
			while(keepGoing) { 
				// read a String (which is an object) 
				try { 
					cm = (ChatMessage) sInput.readObject(); 
				} 
				catch (IOException e) { 
					display(username + " Exception reading Streams: " + e); 
					break;     
				} 
				catch(ClassNotFoundException e2) { 
					break; 
				} 
				// the messaage part of the ChatMessage 
				String message = cm.getMessage(); 

				// Switch on the type of message receive 
				switch(cm.getType()) { 

				case ChatMessage.MESSAGE: 
					System.out.println(username);
					broadcast(username , message); 
					break; 
				case ChatMessage.LOGOUT: 
					display(username + " disconnected with a LOGOUT message."); 
					keepGoing = false; 
					break; 
				case ChatMessage.WHOISIN: 
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n"); 
					// scan al the users connected 
					for(int i = 0; i < al.size(); ++i) { 
						ClientThread ct = al.get(i); 
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date); 
					} 
					break; 
				} 
			} 
			// remove myself from the arrayList containing the list of the 
			// connected Clients 
			remove(id); 
			close(); 
		} 

		// try to close everything 
		private void close() { 
			// try to close the connection 
			try { 
				if(sOutput != null) sOutput.close(); 
			} 
			catch(Exception e) {} 
			try { 
				if(sInput != null) sInput.close(); 
			} 
			catch(Exception e) {}; 
			try { 
				if(socket != null) socket.close(); 
			} 
			catch (Exception e) {} 
		} 

		/**
		 * Write a String to the Client output stream
		 * @param represent the message 
		 */ 
		private boolean writeMsg(String msg) { 
			// if Client is still connected send the message to it 
			if(!socket.isConnected()) { 
				close(); 
				return false; 
			} 
			// write the message to the stream 
			try { 
				sOutput.writeObject(msg); 
			} 
			// if an error occurs, do not abort just inform the user 
			catch(IOException e) { 
				display("Error sending message to " + username); 
				display(e.toString()); 
			} 
			return true; 
		} 
	} 
}