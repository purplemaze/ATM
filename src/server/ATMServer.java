package server;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.*;

import files.FileReaderWriter;

/**
 *  @author Daniel C 
 *  @author Ziad S
*/
public class ATMServer {

    private static int connectionPort;
    private boolean listening;
    private ServerSocket serverSocket;
    private FileReaderWriter clientsDB;
    private Map<Long, Boolean> clients;

    /**
     * Konstruktor för ATMServer
     * Tar en connectionPort(int) och sätter alla fält samt anropar setupServ.
     * Kastar en IOException
     */
    public ATMServer(int connectionPort) throws IOException {
        ATMServer.connectionPort = connectionPort;  //Static
        listening = true;
        setupClientDB();
        setupServ();
    }

    /**
     * setupServ
     * Tar en Port och skapar en ny ServerScoket
     * 
     */
    private void setupServ() throws IOException {

        try {
            serverSocket = new ServerSocket(connectionPort); 
            System.out.println("ATMServer started listening on port: " + connectionPort);
            while (listening)
                new ATMServerThread(serverSocket.accept(), this).start();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + connectionPort);
            System.exit(1);
        }

    }
    
    private void setupClientDB() throws IOException {	
    	clientsDB = new FileReaderWriter("/home/daniel/Documents/workspace/Inet/src/server/res/clients.db");
    	clients = new HashMap<Long, Boolean>(); 
    	
    	ArrayList<String> lines = (ArrayList<String>) clientsDB.readFile();
		Iterator<String> it = lines.iterator();
		
		while(it.hasNext()) {
			String temp = it.next();
			String[] tempArray = temp.split(",");
			clients.put(Long.parseLong(tempArray[0]), false);
			System.out.println(temp);
		}
    }
    
    /**
     * loginClient
     * @return :
     * -1 if no such user
     * 0 if already logged in
     * 1 if not we log in the user
     */
    public int loginClient(Long clientID) {
    	if(clients.containsKey(clientID) == false) return -1;
    	
    	boolean isLoggedin =  clients.get(clientID);
    	if(isLoggedin) return 0;
    	
    	// log in the user
    	clients.put(clientID, true);
    	return 1;
    	
    }
    
    
    public static void main(String[] args) throws IOException {
    	
        try {
            connectionPort = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Missing argument connection port");
            System.exit(1);
        }

        // Create a new ATMServer
        new ATMServer(connectionPort);

    }
}