package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import files.FileReaderWriter;

/**
 * ATMServerClient class!
 * 
 * This class is a representation of the client on the server side.
 * It holds the state of a client's balance, card code, and client codes(for withdrawal).
 * 
 * @author Daniel C 
 * @author Ziad S
 * @version 1.0
 */
public class ATMServerClient {
	
	private long userId;
	private int cardCode;
	private int balance;
    private FileReaderWriter clientsDB;
    private FileReaderWriter clientCodesDB;
    ArrayList<String> clients;
    Iterator<String> it;
    ArrayList<String> clientCodes;
	
	/**
	 * Constructor
	 * @param userId
	 * @throws IOException
	 * @see IOException
	 */
	public ATMServerClient(long userId) throws IOException {
		this.userId = userId;
		clientCodesDB = new FileReaderWriter("server/res/clientcodes/" + userId + ".codes");
		clientsDB = new FileReaderWriter("server/res/clients.db");
		getClientCodes();
		getUserData();
		
		// Populate all the client fields.
		it = clients.iterator();
		while(it.hasNext()) {
			String temp = it.next();
			String[] tempArray = temp.split(",");
			if(Long.parseLong(tempArray[0]) == userId) {
				cardCode = Integer.parseInt(tempArray[1]);
				balance = Integer.parseInt(tempArray[2]);
			}
		}
	}
	
	/**
	 * This method reads from the clients database and stores all the
	 * information in an ArrayList<String>.
	 * @throws IOException 
	 * @see IOException
	 */
	private void getUserData() throws IOException {
		clients = (ArrayList<String>) clientsDB.readFile();		
	}
	
	/**
	 * This method reads this clients codes(for withdrawal).
	 * @throws IOException
	 * @see IOException
	 */
	private void getClientCodes() throws IOException {
		clientCodes = (ArrayList<String>) clientCodesDB.readFile();
	}
	
	/**
	 * This help method creates a string consisting of this clients userId cardCode 
	 * and balance, to later be written back to the clients database.
	 * @return
	 */
	private String buildUserData() {
		return "" + userId + "," + cardCode + "," + balance;
	}
	
	/**
	 * This method reads the whole user database and updates the clients balance
	 * and the client codes(for withdrawal).
	 * @throws IOException 
	 */
	public void saveUserData() throws IOException {
		try {
			getUserData();
			it = clients.iterator();
			while(it.hasNext()) {
				String temp = it.next();
				String[] tempArray = temp.split(",");
				if(Long.parseLong(tempArray[0]) == userId) {
					it.remove();
				}
			}
			clients.add(buildUserData());
			clientsDB.writeFile(clients);
			clientCodesDB.writeFile(clientCodes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
	}
	
	/**
	 * @return int This clients balance
	 */
	public int getAccountBalance() {
		return balance;		
	}
	
	/**
	 * This method performs a withdrawal on this clients account.
	 * @param amount
	 * @return int This returns the following:
	 * 0 = successful.
	 * -1 = wrong code.
	 * -2 Insufficient founds.
	 */
	public int withdrawal(int amount, String code) {
		if(code.equals(clientCodes.get(0))) {
			if(amount <= balance) {
				balance -= amount;
				clientCodes.remove(0);
				return 0;
			}else {
				return -2;
			}	
		}else {
			return -1;
		}
	}
	
	/**
	 * This method performs a deposit into this clients account.
	 * @param amount
	 * @return
	 */
	public boolean deposit(int amount) {
		balance += amount;
		return true;
	}
	
	/**
	 * This method returns this clients user id.
	 * @return
	 */
	public long getUserId() {
		return userId;
	}
}
