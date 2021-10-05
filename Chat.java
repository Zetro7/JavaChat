// Title: Chat.java
// Rudolph Hanzes HAN7739@CALU.EDU
// Robby Minerd MIN6111@CALU.EDU
// CET - 350 Technical Computing using Java
// Group 7

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

class Chat extends Frame implements WindowListener, ActionListener, Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected final static boolean autoFlush = true;

	Thread TheThread;
	
	Boolean more;
	//---------------------------------------------------------------------------------- BufferedReader and Writer set
	BufferedReader br;
	PrintWriter pw;
	//---------------------------------------------------------------------------------- Initial port Number Created
	int portNumb = 44004;
	//---------------------------------------------------------------------------------- Server and client Sockets created and listenSocket ServerSocket created
	Socket Client;
	Socket Server;
	
	ServerSocket listenSocket;
	//---------------------------------------------------------------------------------- Initial timeout created
	static int timeout = 1000;
	
	String line;
	//---------------------------------------------------------------------------------- Initial host Created
	String host = "127.0.0.1";
	//---------------------------------------------------------------------------------- control panel created and all the objects used
	Panel control = new Panel();
	
	TextArea message = new TextArea("",10,80);
	TextArea status = new TextArea("",3,80);
	
	Button send = new Button("Send");
	Button changeHost = new Button("Change Host");
	Button startServer = new Button("Start Server");
	Button changePort = new Button("Change Port");
	Button connect = new Button("Connect");
	Button disconnect = new Button("Disconnect");
	
	Label hostLabel = new Label("Host:");
	Label portLabel = new Label("Port:");
	
	TextField text = new TextField();
	TextField hostField = new TextField();
	TextField portField = new TextField();
	
	public static void main (String[] args)
	{
	//---------------------------------------------------------------------------------- Checks if a timeout was specified by user input when running program, if not timeout remains 1000
		try {
			timeout = Integer.parseInt(args[0]);
		} catch (NumberFormatException e)
		{}catch (ArrayIndexOutOfBoundsException ex)
		{}
		new Chat();
	}
	
	public Chat()
	{
		more = true;
	//---------------------------------------------------------------------------------- sets up control panel
		addListenersComponents();
		createGridBag();
		
	//---------------------------------------------------------------------------------- Sets up the frame
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(600,400));
		this.setMinimumSize(getPreferredSize());
		this.setLocation(100,100);
		this.setTitle("Chat");
		this.addWindowListener(this);
		this.setResizable(true);
		this.add(control);
	
		
		
		this.setVisible(true);
	}
	//---------------------------------------------------------------------------------- adds all the listeners to the objects
	public void addListenersComponents()
	{
		message.setEditable(false);
		status.setEditable(false);
		
		text.addActionListener(this);
		send.addActionListener(this);
		changeHost.addActionListener(this);
		startServer.addActionListener(this);
		changePort.addActionListener(this);
		connect.addActionListener(this);
		disconnect.addActionListener(this);
		
		control.add(message);
		control.add(disconnect);
		control.add(portLabel);
		control.add(portField);
		portField.setText(Integer.toString(portNumb));
		hostField.setText(host);
		control.add(changePort);
		control.add(connect);
		connect.setEnabled(false);
		control.add(changeHost);
		control.add(startServer);
		control.add(hostField);
		control.add(hostLabel);
		control.add(send);
		control.add(status);
		control.add(text);
		text.setEnabled(false);
	}
	//---------------------------------------------------------------------------------- Sets up all the objects to fit correctly and resize using grid bag Layout
	public void createGridBag()
	{
		double colWeight[] = {1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		int colWidth[] = {1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		
		GridBagConstraints c = new GridBagConstraints();
		GridBagLayout displ = new GridBagLayout();

		displ.columnWidths = colWidth;
		
		displ.columnWeights = colWeight;
		
		c.anchor = GridBagConstraints.CENTER;
		
		control.setLayout(displ);
		c.fill = GridBagConstraints.BOTH;
		
		c.weightx = 1;
		c.weighty = 1;
		
		c.gridwidth = 0;
		c.gridheight = 1;
		
		c.gridx = 0;
		c.gridy = 0;
		
		displ.setConstraints(message, c);
		
		c.gridwidth = 14;
		c.gridx = 0;
		c.gridy = 1;
		
		displ.setConstraints(text, c);
		
		c.gridwidth = 0;
		c.gridx = 14;
		c.gridy = 1;
		
		displ.setConstraints(send, c);
		
		c.gridx = 1;
		c.gridy = 2;
		
		displ.setConstraints(hostLabel, c);
		
		c.gridwidth = 9;
		c.gridx = 2;
		c.gridy = 2;
		
		displ.setConstraints(hostField, c);

		c.gridwidth = 1;
		c.gridx = 13;
		c.gridy = 2;
		
		displ.setConstraints(changeHost, c);
		
		c.gridx = 14;
		c.gridy = 2;
		
		displ.setConstraints(startServer, c);
		
		c.gridx = 1;
		c.gridy = 3;
		
		displ.setConstraints(portLabel, c);
		
		c.gridwidth = 9;
		c.gridx = 2;
		c.gridy = 3;
		
		displ.setConstraints(portField, c);
		
		c.gridwidth = 1;
		c.gridx = 13;
		c.gridy = 3;
		
		displ.setConstraints(changePort, c);
		
		c.gridx = 14;
		c.gridy = 3;
		
		displ.setConstraints(connect, c);
		
		c.gridx = 14;
		c.gridy = 4;
		
		displ.setConstraints(disconnect, c);
		
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 5;
		
		displ.setConstraints(status, c);
	}
	//---------------------------------------------------------------------------------- Creates the Thread if it doesn't already exist
	public void start()
	{
		if(TheThread == null)
		{
			TheThread = new Thread(this);
			TheThread.start();
		}
	}
	//---------------------------------------------------------------------------------- Heart of the program, loops until disconnected by the user
	public void run() 
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		while(more)
		{
	//---------------------------------------------------------------------------------- reads from the bufferedReader into line, then appends in to the text area of the receiver
			try
			{
				line = br.readLine();
				if (line != null)
				{
					message.append("in: "+line+"\n");
				}
				else
				{
	//---------------------------------------------------------------------------------- ends the loop when a null is read from the buffered reader
					more = false;
					status.append("Chat Disconnected\n");
					close();
				}
			} catch (IOException e)
			{
				more = false;
				status.append("Chat Disconnected\n");
				close();
			}
		}
	}
	//---------------------------------------------------------------------------------- Removes all the listeners and disposes of the frame
	public void stop()
	{
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		more = false;
		
		text.removeActionListener(this);
		send.removeActionListener(this);
		changeHost.removeActionListener(this);
		startServer.removeActionListener(this);
		changePort.removeActionListener(this);
		connect.removeActionListener(this);
		disconnect.removeActionListener(this);
		
		this.remove(control);
		this.removeWindowListener(this);
		this.dispose();
	}
	//---------------------------------------------------------------------------------- closes all the sockets and reader/writers
	public void close()
	{
		try
		{
			if(Server != null)
			{
				Server.close();
				Server = null;
			}
			if(Client != null)
			{
				Client.close();
				Client = null;
			}
			if(listenSocket != null)
			{
				listenSocket.close();
				listenSocket = null;
			}
			if(br != null)
			{
				br.close();
				br = null;
			}
			if(pw != null)
			{
				pw.close();
				pw = null;
			}
		} catch (IOException e) {}
	//---------------------------------------------------------------------------------- Resets the buttons and text fields to restart the chat
		startServer.setEnabled(true);
		text.setEnabled(false);
		connect.setEnabled(false);
		hostField.setText("");
		this.setTitle("Chat");
		TheThread = null;
	}

	public void actionPerformed(ActionEvent e) 
	{
		Object selected = e.getSource();
		
		if (selected == send || !text.getText().equals(""))
		{
			String data = text.getText();
			if (!data.equals(""))
			{
				message.append("out: "+data+"\n");
				pw.println(data);
			}
			text.setText("");
		}
		if (selected == changeHost && !hostField.getText().contentEquals(""))
		{
			host = hostField.getText();
			
			if (Server == null && Client == null)
			{
				status.append("Host set to: "+host+"\n");
				connect.setEnabled(true);
			}
			else
			{
				status.append("Host can't be changed while chat is running\n");
			}
		}
		if (selected == changePort && !portField.getText().contentEquals(""))
		{
			try
			{
				if (Server == null && Client == null)
				{
					portNumb = Integer.parseInt(portField.getText());
					status.append("Port Set to: "+portNumb+"\n");
				}
				else
				{
					status.append("Port can't be changed while chat is running\n");
				}
			} catch (NumberFormatException ex) 
			{
				status.append("Port must be a number..\n");
			}
		}
	//---------------------------------------------------------------------------------- Where the Server is initialized and specified, thread is started
		if (selected == startServer)
		{
			try 
			{
	//---------------------------------------------------------------------------------- Changes the frame title and displays status messages to status text area
				this.setTitle("Server");
				status.append("Starting Server\n");
				status.append("Server: Listening on Port "+portNumb+"\n");
				status.append("Server: Timeout set to "+10*timeout+" mS.\n");
				status.append("Server: Waiting for a Request on "+portNumb+"\n");
				
				startServer.setEnabled(false);
				connect.setEnabled(false);
	//---------------------------------------------------------------------------------- Resets the Sockets to null
				if (listenSocket != null)
				{
					listenSocket.close();
					listenSocket = null;
				}
				if (Client != null)
				{
					Client.close();
					Client = null;
				}
				
				listenSocket = new ServerSocket(portNumb);
				listenSocket.setSoTimeout(10*timeout);
				
				Client = listenSocket.accept();
				
				this.setTitle("Server");
				status.append("Server: Connection From "+Client.getInetAddress()+"\n");
				status.append("Server: Chat is Running\n");
				
				br = new BufferedReader(new InputStreamReader(Client.getInputStream()));
				pw = new PrintWriter(Client.getOutputStream(), autoFlush);
				
				text.setEnabled(true);
				more = true;
				start();
			} catch (SocketTimeoutException s) 
			{
				status.append("Failed to Connect..\n");
				close();
			} catch (IOException ex) 
			{
				close();
			}
		}
	//---------------------------------------------------------------------------------- Where the Client is initialized and Specified, thread is started
		if (selected == connect)
		{
			try 
			{
				startServer.setEnabled(false);
				connect.setEnabled(false);
				
				status.append("Starting Client\n");
				status.append("Client: Timeout set to "+timeout+" mS.\n");
				status.append("Client: Connecting to "+host+" : "+portNumb+"\n");
	//---------------------------------------------------------------------------------- Resets the Sockets to null
				if (Server != null)
				{
					Server.close();
					Server = null;
				}
				if (Client != null)
				{
					Client.close();
					Client = null;
				}
				Server = new Socket();
				Server.connect(new InetSocketAddress(host, portNumb));
	//---------------------------------------------------------------------------------- Changes the frame title and displays status messages to status text area
				this.setTitle("Client");
				status.append("Client: Connected to "+host+" at port "+portNumb+"\n");
				status.append("Client: Chat is Running\n");
				
				br = new BufferedReader(new InputStreamReader(Server.getInputStream()));
				pw = new PrintWriter(Server.getOutputStream(), autoFlush);
				
				text.setEnabled(true);
				more = true;
				start();
			
			} catch (SocketTimeoutException s)
			{
				status.append("TO Failed to Connect..\n");
				close();
			} catch (IOException ex) 
			{
				status.append("Failed to Connect..\n");
				close();
			}
		}
	//---------------------------------------------------------------------------------- interrupts the thread and closes everything, displays a message if never connected
		if (selected == disconnect)
		{
			try
			{
				TheThread.interrupt();
				close();
			} catch (NullPointerException ex) 
			{
				status.append("Never Connected\n");
				close();
			}
		}
		text.requestFocus();
	}
	//---------------------------------------------------------------------------------- All the basic Window Events, each requesting focus to the message text Field, besides windowClosing which shuts everything down
	public void windowOpened(WindowEvent e) {
		text.requestFocus();
	}

	public void windowClosing(WindowEvent e) {
		close();
		stop();
	}

	public void windowClosed(WindowEvent e) {
		text.requestFocus();
	}

	public void windowIconified(WindowEvent e) {
		text.requestFocus();
	}

	public void windowDeiconified(WindowEvent e) {
		text.requestFocus();
	}

	public void windowActivated(WindowEvent e) {
		text.requestFocus();
	}

	public void windowDeactivated(WindowEvent e) {
		text.requestFocus();
	}
}
