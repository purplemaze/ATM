package server;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;

import files.FileReaderWriter;

/**
 * ATMServerThread class!
 * 
 * This class is responsible for all the communication with the client via Socket.
 * It runs in it's own thread.
 * 
 * @author Daniel C 
 * @author Ziad S
 * @version 1.0
*/
public class ATMServerThread extends Thread {

    private Socket socket = null;
    private DataInputStream in;
    private DataOutputStream out;
    private ATMServer server;
    private ATMServerClient client;
    private FileReaderWriter reader;
    private byte opcode;
    private byte request;


    /**
     * Constructor
     * @param socket Takes a socket to read and write from.
     * @param server Takes a server to keep track of logged in users.
     */
    public ATMServerThread(Socket socket, ATMServer server) {
        super("ATMServerThread");
        this.socket = socket;
        this.server = server;
        System.out.println(""  + socket + " : " + "connected");
    }
    
    /**
     * Converts a byte array to a Long
     * @param b
     * @return
     */
    private Long byteToLong(byte[] b) {
    	long value = 0;
    	for (int i = 0; i < b.length; i++) {
    		value = (value << 8) + (b[i] & 0xff);
    	}
    	return value;
    }
    
    /**
     * Logs in the client using the server instance's method loginCLient.
     * @param clientID
     * @param cardCode
     * @return
     */
    private int validateClientId(long clientID, int cardCode) {
    	return server.loginClient(clientID, cardCode);
    }
    
    /**
     * This method logs out the client and saves all the user data.
     * @throws IOException
     * @see IOException
     */
    private void logOutUser() throws IOException {
    	client.saveUserData();
    	server.logoutClient(client.getUserId());
    }
    
    /**
     * This method is responsible for checking that the client side follows the ATM protocol
     * for a "log in" and then logs in the user.
     * @throws IOException
     */
    private void loginUser() throws IOException {
    	byte[] cardNumber = new byte[8];
    	byte[] answear = {1,2};
    	byte[] response = new byte[2];
    	out.write(answear); //write [1,2]
   
    	in.read(cardNumber); // read 8 byte card number
    	
    	long longCardNumber = byteToLong(cardNumber);
    	System.out.println(""  + socket + " : " + "gave us a card number: " + longCardNumber);
    	
    	answear[1] = 0;
    	out.write(answear); //write [1,0] got card number
    	
    	int cardCode = in.readInt();
    	System.out.println(""  + socket + " : " + "gave us a card code: " + cardCode);
    	
    	int ret = validateClientId(longCardNumber, cardCode);
    	if(ret == 2) { 
    		client = new ATMServerClient(longCardNumber);
    		answear[1] = 2;
    		out.write(answear); // sends [1,2], success
    		in.read(response);
    		byte respCode = response[1];
    		if(respCode == 3) greeting();
    	}else if(ret == -1) {
    		answear[1] = -1;
    		out.write(answear); // sends [1,-1], no such user
    	}else if(ret == 0) {
    		answear[1] = 0;
    		out.write(answear); // sends [1,0], Already logged in
    	}else if(ret == 1) {
    		answear[1] = 1;
    		out.write(answear); // sends [1,1], wrong cardCode
    	}else {
    		System.err.println("Hur ska vi felhantera detta?, eller ska vi skita i det :S");
    	}
    	System.out.println(""  + socket + " : " + ret);
    	
    }
    
    /**
     * This methods reads the current banner.
     * It also checks if the banner is correctly formatted. 
     * @throws IOException
     */
    private void greeting() throws IOException {
    	reader = new FileReaderWriter("server/res/banner.txt");
    	List<String> banner = reader.readFile();
    	Iterator<String> it = banner.iterator();
    	StringBuilder stringB = new StringBuilder();
    	while(it.hasNext()) {
    		stringB.append(it.next());
    		if(it.hasNext())
    		stringB.append("\n");
    	}
    	String temp = stringB.toString();
    	if(temp.length() > 80 || temp.length() < 0) {
    		out.writeUTF("Welcome to the YOLO Bank of New TrollingTown.. \nPlz deposit moneyz..");
    	}
    	out.writeUTF(temp);
    }
    
    /**
     * This method is responsible for checking that the client side follows the ATM protocol
     * for "balance".
     * <p>
     * First it acknowledges the clients request(3, 0) and checks if the client is logged in.
     * If so it confirms the balance and waits for a "get balance request".
     * Then it writes the balance on the output stream.
     * @throws IOException
     */
    private void balance() throws IOException {
    	byte[] answear = {3,0};
    	if(client != null) {
    		out.write(answear);
    		opcode = in.readByte();
    		request = in.readByte();
    		if(opcode == 3 && request == 2) {
    			out.writeInt(client.getAccountBalance());
    		}else {
    			answear[1] = -1;
        		out.write(answear);
    		}
    	}else {
    		answear[1] = -1;
    		out.write(answear);
    	}
    }
    
    /**
     * 
     * @throws IOException
     */
    private void deposit() throws IOException {
    	byte[] answear = {5,0};
    	if(client != null) {
    		out.write(answear);
    		if(client.deposit(in.readInt())){
    			out.write(answear);
    		}else {
    			answear[1] = -1;
    			out.write(answear);
    		}
    	}else {
    		answear[1] = -1;
			out.write(answear);
    	}
    	System.out.println(""  + socket + " : " + "deposit complete " + "ret: " + answear[1]);
    }
    
    /**
     * 
     * @throws IOException
     */
    private void withdrawal() throws IOException {
    	byte[] answear = {4,0};
    	if(client != null) {
    		out.write(answear);
    		int amount = in.readInt();
    		out.write(answear); 
    		String code = in.readUTF();
    		int ret = client.withdrawal(amount, code);
    		if(ret == 0) {
    			out.write(answear);
    		}else if(ret == -1) {
    			answear[1] = -1;
    			out.write(answear);
    		}else if(ret == -2) {
    			answear[1] = -2;
    			out.write(answear);
    		}else if(ret == 1) {
    			answear[1] = 1;
    			out.write(answear);
    		}
    	}
    }
    
    @SuppressWarnings("unused")
    private boolean verifyAmount(int amount) {
		return false;
    	
    }
    
    @SuppressWarnings("unused")
	private void notLoggedIn() {
    	//answear[1] = -1;
		//out.write(answear);
    }
    
    /**
     * 
     */
    public void run(){         
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            
            greeting();
            
            while(true) {
            	opcode = in.readByte();
            	if(opcode == -1) throw new IOException("broken pipe");
            	request = in.readByte();
            	switch (opcode) {
            	case 1:
            		if(request == 1) {
            			System.out.println(""  + socket + " : " + "is trying to log in");
            			loginUser();
            		}
            		break;
            	case 3:
            		System.out.println(""  + socket + " : " + "is accessing his/hers balance");
            		balance();
            		break;
            	case 4:
            		System.out.println(""  + socket + " : " + "is making a withdrawal");
            		withdrawal();
            		break;
            	case 5:
            		System.out.println(""  + socket + " : " + "is depositing money");
            		deposit();
            		break;
            	case 6:
                    logOutUser();
                    byte[] logout = {6,0};
                    out.write(logout);
            		System.out.println(""  + socket + " : " + "logged out");
            		break;
            	default: 
                    break;
            	}
            }
        }catch (IOException e){
            try {
            	if(client != null)
            		logOutUser();
				out.close();
				in.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	System.out.println(""  + socket + " : " + "disconnected suddenly");
        }
    
    }
}
