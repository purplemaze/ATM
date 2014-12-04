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
    ArrayList<String> clients;
    Iterator<String> it;
	
	/**
	 * 
	 * @param userId
	 * @throws IOException
	 */
	public ATMServerClient(long userId) throws IOException {
		this.userId = userId;
		getUserData();
		
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
		clientsDB = new FileReaderWriter("/home/daniel/Documents/workspace/Inet/src/server/res/clients.db");
		clients = (ArrayList<String>) clientsDB.readFile();		
	}
	
	private String buildUserData() {
		return "" + userId + "," + cardCode + "," + balance;
	}
	/**
	 * saveUserData
	 */
	public void saveUserData() {
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
	 * withdrawl
	 * @param amount
	 * @return
	 */
	public boolean withdrawl(int amount) {
		if(amount <= balance) {
			balance -= amount;
			return true;
		}
		return false;
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
	
	public static void main(String[] args) throws IOException{
		
		ATMServerClient test = new ATMServerClient(5555666677778888L);
		System.out.println(test.getAccountBalance());
		
		test.withdrawl(500);
		test.saveUserData();
		System.out.println(test.getAccountBalance());

	}
}
