package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import files.FileReaderWriter;

/**
 * 
 * @author Daniel
 *
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
	 * 
	 * @param userId
	 * @throws IOException
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
	 * getUserData
	 * @throws IOException 
	 * 
	 */
	private void getUserData() throws IOException {
		clients = (ArrayList<String>) clientsDB.readFile();		
	}
	
	private void getClientCodes() throws IOException {
		clientCodes = (ArrayList<String>) clientCodesDB.readFile();
	}
	
	private String buildUserData() {
		return "" + userId + "," + cardCode + "," + balance;
	}
	/**
	 * saveUserData
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
	 * getAccountBalance
	 * @return
	 */
	public int getAccountBalance() {
		return balance;		
	}
	
	/**
	 * Withdrawal
	 * @param amount
	 * @return
	 */
	public int withdrawal(int amount, String code) {
		try {
			getClientCodes();
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
		
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
	 * deposit
	 * @param amount
	 * @return
	 */
	public boolean deposit(int amount) {
		balance += amount;
		return true;
	}
	
	public long getUserId() {
		return userId;
	}
}
