package server;

/**
 * 
 * @author Daniel
 *
 */
public class ATMServerClients {
	
	private int userId;
	private int balance;
	
	
	public ATMServerClients(int userId) {
		this.userId = userId;
		getUserData();
	}
	
	/**
	 * getUserData
	 * 
	 */
	private void getUserData() {
		
	}
	
	/**
	 * saveUserData
	 */
	public void saveUserData() {
		
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
		return false;
		
	}
	
	/**
	 * deposit
	 * @param amount
	 * @return
	 */
	public boolean deposit(int amount) {
		return false;
		
	}
}
