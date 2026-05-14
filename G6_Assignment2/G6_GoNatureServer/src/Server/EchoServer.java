package Server;

import java.util.ArrayList;
import Common.Chat;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer {

	// Class variables *************************************************

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 */
	private DBController dbController;

	public EchoServer(int port) {
		super(port);
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		
		try {
			Chat request = (Chat) msg;
			String command = request.getCommand();
			System.out.println("GoNatureServer received: " + command);

			if (command.equals("GET_ORDERS")) {
				String query = "SELECT * FROM `orders`";
				ArrayList<ArrayList<String>> orderList = dbController.selectQuery(query);
				client.sendToClient(orderList);

			} else if (command.equals("UPDATE_ORDER")) {
				ArrayList<Object> updateData = (ArrayList<Object>) request.getData();
				int id = (int) updateData.get(0);
				String newDate = (String) updateData.get(1);
				int visitors = (int) updateData.get(2);
				boolean updateResult = dbController.updateQuery(id, newDate, visitors);
				client.sendToClient(updateResult);

			} else {
				System.out.println("GoNatureServer: unrecognized command — " + command);
				client.sendToClient(false);
			}

		} catch (Exception e) {
			System.out.println("GoNatureServer error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	@Override
	protected void serverStarted() {
		dbController = new DBController();
		System.out.println("GoNature Server started on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("GoNature Server stopped listening.");
	}
	
	@Override
	protected void clientConnected(ConnectionToClient client) {
	    String ip = client.getInetAddress().getHostAddress();
	    String host = client.getInetAddress().getHostName();
	    System.out.println("Client connected — IP: " + ip + " | Host: " + host);
	}
}