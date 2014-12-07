package server;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.*;

import files.FileReaderWriter;

/**
 * ATMServer class!
 * 
 * This is the main class for the ATM server application.
 * It creates a new ServerSocket that listens and accept new connections.
 * The ATMServer creates one thread per each client connection.
 * 
 * @author Daniel C 
 * @author Ziad S
 * @version 1.0
*/
public class ATMServer {

    private static int connectionPort;
    private boolean listening;
    private ServerSocket serverSocket;
    private FileReaderWriter clientsDB;
    private Map<Long, Integer> clients;
    private Map<Long, Boolean> clientsLoggedIn;

    /**
     * Constructor.
     * @param connectionPort The connection port for the server
     * @throws IOException when creating a new server with setupServ or reading from file in setupClientDB
	 * @see IOException
     */
    public ATMServer(int connectionPort) throws IOException {
        ATMServer.connectionPort = connectionPort;
        listening = true;
        setupClientDB();
        setupServ();
    }

    /**
     * This method creates a new ServerSocket with a static port.
     * It creates one thread per each client connection.
     * @throws IOException when creating a new server
     * @see IOException
     */
    private void setupServ() throws IOException {
        try {
            serverSocket = new ServerSocket(connectionPort); 
            System.out.println("ATMServer started listening on port: " + connectionPort);
            while (listening)
                new ATMServerThread(serverSocket.accept(), this).start();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + connectionPort);
            System.exit(-1);
        }
    }
    
    /**
     * This method loads the client database.
     * Then it stores all the clients in memory as not logged in.
     * @throws IOException when reading a file
     * @see IOException
     */
    private void setupClientDB() throws IOException {	
    	clientsDB = new FileReaderWriter("server/res/clients.db");
    	clients = new HashMap<Long, Integer>(); 
    	clientsLoggedIn = new HashMap<Long, Boolean>(); 
    	
    	ArrayList<String> lines = (ArrayList<String>) clientsDB.readFile();
		Iterator<String> it = lines.iterator();
		
		while(it.hasNext()) {
			String temp = it.next();
			String[] tempArray = temp.split(",");
			clientsLoggedIn.put(Long.parseLong(tempArray[0]), false);
			clients.put(Long.parseLong(tempArray[0]), Integer.parseInt(tempArray[1]));
			System.out.println(temp);
		}
    }
    
    /**
     * This method tries to log in the client.
     * @param clientID
     * @param cardCode
     * @return int This returns the following:
     * -1 if no such user
     * 0 if already logged in
     * 1 wrong cardCode
     * 2 if not we log in the user
     */
    public int loginClient(Long clientID, int cardCode) {
    	if(clientsLoggedIn.containsKey(clientID) == false) return -1;
    	
    	if(cardCode != clients.get(clientID)) return 1;
    	
    	if(clientsLoggedIn.get(clientID)) return 0;
    	
    	clientsLoggedIn.put(clientID, true);
    	return 2;
    	
    }
    
    /**
     * This method will log out the client
     * @param clientID
     */
    public void logoutClient(Long clientID) {
    	clientsLoggedIn.put(clientID, false);
    }
     
    /**
     * This is the main method which creates a new ATMServer
     * @param args 
     */
    public static void main(String[] args) {
        try {
            connectionPort = Integer.parseInt(args[0]);
            new ATMServer(connectionPort);
        } catch (ArrayIndexOutOfBoundsException e1) {
            System.err.println("Missing argument connection port");
            System.exit(1);
        } catch (IOException e2) {
        	e2.printStackTrace();
        }
    }
}
