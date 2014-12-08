package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import files.FileReaderWriter;


public class ATMClient {
	private static int connectionPort;
	private static String address;
	private Socket ATMSocket;
	private DataOutputStream out;
	private DataInputStream in;
	private FileReaderWriter file; //Reads file
	private HashMap<String, String> lang;	//Holds all phrases
	private List<String> header;			//Holds all languages available
	private Scanner scanner;					
	private boolean login;
	
	/**
	 * Constructor for client
	 * Starts scanner and connects client to server
	 * @throws IOException
	 */
	public ATMClient() throws IOException{
		scanner = new Scanner(System.in);
		startClient();
		login = false;
	}
	
	/**
	 * Starts the client and connects to server. 
	 */
	private void startClient() throws IOException{
		readConnection();	//Reads adress and connetion port from file.
		// Creates socket and out/in-put streams.
		try{
			ATMSocket = new Socket(address, connectionPort);
			out = new DataOutputStream(ATMSocket.getOutputStream());
			in = new DataInputStream(ATMSocket.getInputStream());
			
		}catch (UnknownHostException e){
			System.err.println("Unknown host: " + address);
			System.exit(1);
		}catch (IOException e){
			System.err.println("Couldn't open connection to port " + connectionPort);
			System.exit(1);
		}
		
		langHeader(); // Reads the possible languages
		chosenLanguage(1);	// Loads default language (Eng)
		System.out.println("Connecting to bank..");
		
		try{
			TimeUnit.MILLISECONDS.sleep(500);	//Sleeps just to make it cooler
		}catch (InterruptedException e){
			System.out.println("Wow, what happend");
		}
		bankGreeting();	//Gets greeting from server
		scanLogin();	//Starts listening on user inputs for login menu
	}
	/**
	 * Reads connectionPort and address from file
	 */
	private void readConnection() throws IOException{	
		file = new FileReaderWriter("client/res/client.config");
		List<String> connectionInfo = new ArrayList<String>();
		//Stores the values file to corresponding variable
		connectionInfo = file.readFile();
		connectionPort = Integer.parseInt(connectionInfo.get(0));
		address = connectionInfo.get(1);
	}
	/**
	 * Reads the greeting from server
	 * @throws IOException
	 */
	private void bankGreeting() throws IOException {
		System.out.println(in.readUTF()); // read greeting
	}
	
	/**
	 * Handles ATM login by listening to user inputs.
	 * Starts listening on user inputs in the bank menu if login succeeds
	 * 
	 * @throws IOException
	 */
	private void scanLogin() throws IOException {
		while (!login){
			System.out.println(lang.get("startMenu"));
			int chosen = scanner.nextInt();
			if(chosen == 2){
				System.out.println(lang.get("languageMenu"));				
				chosenLanguage(scanner.nextInt() - 1);
			}if(chosen == 1){
				loginClient();
			}
		}
		scanMenu(); 
	}
	
	/**
	 * Handles login communication between server and client when user is trying to login.
	 * @throws IOException
	 */
	private void loginClient() throws IOException {
		byte opCode;
		byte response;
		byte[] loginReq = {1, 1};
		out.write(loginReq); // log in request
		
		byte[] loginAns = new byte[2];
		in.read(loginAns);
		
		opCode = loginAns[0];
		response = loginAns[1];
		
		if(opCode == 1 && response == 2) {
			long cardNumber = 0;
			while( !(validateCardNumber(cardNumber))) {
				try {
					System.out.println(lang.get("cardNumber"));
					cardNumber = scanner.nextLong();
				}catch (InputMismatchException e){
					scanner.next();
				}
				if(!(validateCardNumber(cardNumber)))
				System.out.println(lang.get("invalidCardNumber"));
			}
			out.write(longToBytes(cardNumber));  // send card number to server
			
		}
		//Reads answer from server and handles card code input
		in.read(loginAns);		
		opCode = loginAns[0];
		response = loginAns[1];
		if(opCode == 1 && response == 0) {
			int cardCode = 0;
			while( !(validateCardCode(cardCode))) {
				try {
					System.out.println(lang.get("cardCode"));
					cardCode = scanner.nextInt();
				}catch (InputMismatchException e){
					scanner.next();
				}
				if( !(validateCardCode(cardCode)))
				System.out.println(lang.get("invalidCardCode"));
			}
			out.writeInt(cardCode);;  // send card code to server
			
		}else {
			// kanske?
		}
		//Reads answer from server and handles if it's a valid login.
		in.read(loginAns);
		opCode = loginAns[0];
		response = loginAns[1];
    	if(response == 2) { 
    		login = true;
    	}else if(response == -1) {
    		System.out.println(lang.get("noSuchUser"));
    	}else if(response == 0) {
    		System.out.println(lang.get("alreadyLoggedIn"));
    	}else if(response == 1) {
    		System.out.println(lang.get("wrongCardCode"));
    	}else {
    		System.err.println("Hur ska vi felhantera detta?, eller ska vi skita i det :S");
    	}
	}
	/**
	 * Handles communication with server to retrieve balance
	 * @throws IOException
	 */	
	private void balance() throws IOException {
		//byte opCode;
		byte response;
		byte[] balanceReq = {3, 1};
		byte[] answear = new byte[2];
		out.write(balanceReq); // balance request
		in.read(answear);
		response = answear[1];
		if(response == 0) {
			balanceReq[1] = 2;
			out.write(balanceReq);
			System.out.println(lang.get("balanceText")+ in.readInt());
		}
	}
	/**
	 * Handles communication with server to deposit money in bank.
	 * @throws IOException
	 */	
	private void deposit() throws IOException {
		//byte opCode;
		byte response;
		byte[] depositReq = {5, 1};
		byte[] answear = new byte[2];
		out.write(depositReq); // deposit request
		in.read(answear);		// Answer 
		response = answear[1];
		if(response == 0) {		// Request went through
			int amount = 0;
			while(!(validateAmount(amount))) {
				try {
					System.out.println(lang.get("depositAmount"));
					amount = scanner.nextInt();
				}catch (InputMismatchException e){
					scanner.next();
				}	
				if(!(validateAmount(amount))) 
					if(amount > 1000000){
						System.out.println(lang.get("toMuchWithdrawal"));
					}else{
						System.out.println(lang.get("negativeWithdrawal"));
					}
			}
			out.writeInt(amount);
			in.read(answear);
		}else {
			System.err.println("Server blocked permission to deposit money"); 
		}
		
	}
	/**
	 * Handles communication with server to withdrawal money from bank
	 * @throws IOException
	 */
	private void withdrawal() throws IOException {
		//byte opCode;
		byte response;
		byte[] withdrawlReq = {4, 1};
		byte[] answear = new byte[2];
		out.write(withdrawlReq); // withdrawl request
		in.read(answear);
		response = answear[1];
		if(response == 0) {
			int amount = 0;
			while(!(validateAmount(amount))) {
				try {
					System.out.println(lang.get("withdrawAmount"));	
					amount = scanner.nextInt();
				}catch (InputMismatchException e){
					scanner.next();
				}	
				if(!(validateAmount(amount))) 
					if(amount > 1000000){
						System.out.println(lang.get("toMuchWithdrawal"));
					}else{
						System.out.println(lang.get("negativeWithdrawal"));
					}
			}
			//Checks whether the amount request is valid and handles code input
			out.writeInt(amount);
			in.read(answear);
			response = answear[1];
			if(response == 0) {
				String code;
				while(true) {
					try {
						System.out.println(lang.get("code"));	
						code = scanner.next();
						int ret = validateWithdrawalCode(code);
						if(ret == -1) {
							System.out.println(lang.get("invalidWithdrawalCode"));
						}else if(ret == -2) {
							System.out.println(lang.get("invalidWithdrawalCode"));	
						} else {
							break;
						}
					}catch (InputMismatchException e){
						scanner.next();
						System.out.println(lang.get("wrongCode"));
					}
				}
				//Checks if code is correct and handles server inputs
				out.writeUTF(code);
				in.read(answear);
				response = answear[1];
				if(response == 0) {
					System.out.println(lang.get("validWithdrawal"));
				}else if (response == -1) {
					System.out.println(lang.get("wrongCode"));
				}else if (response == 1) {
					System.out.println(lang.get("noMoreCodes"));
				}else if (response == -2) {
					System.out.println(lang.get("noMoney"));
				}else {
					System.err.println("Something went wrong... contact bank");
				}
			}else {
				System.err.println("Something went wrong... contact bank");
			}
		}else {
			System.err.println("Not logged in error.. contact bank");
		}
		
	}
	/**
	 * Handles input from user if logged in.
	 * Listens for: Balance, Deposit, Withdrawal, Language, Logout
	 * @throws IOException
	 */
	private void scanMenu() throws IOException {
		byte[] getGreeting = {1,3};
		out.write(getGreeting);
		bankGreeting();
		Boolean listening = true;

		while (listening){
			System.out.println(lang.get("mainMenu"));
			int chosen = scanner.nextInt();
			if(chosen == 1){
				balance();
			}else if(chosen == 2){
				withdrawal();
			}else if(chosen == 3){
				deposit();
			}else if(chosen == 4){
				System.out.println(lang.get("languageMenu"));			
				chosenLanguage(scanner.nextInt() - 1);
			}else if(chosen == 5){
				System.out.println(lang.get("byePhrase"));
				byte[] logout = {6, 1};
				byte[] answear = new byte[2];
				out.write(logout);
				in.read(answear);
				listening = false;
			}
		}
		this.login = false;
		scanLogin();

	}
	/**
	 * Reads all the possible languages and saves them into a ArrayList.
	 * @throws IOException
	 */
	private void langHeader() throws IOException{
		file = new FileReaderWriter("client/res/header.lang");
		header = new ArrayList<String>();
		header = file.readFile();	
	}
	/**
	 * Reads language files and replaces all phrases into chosen language.
	 * @param chosenLang, array index for the chosen language
	 * @throws IOException
	 */
	private void chosenLanguage(int chosenLang) throws IOException{
		//loads language
		
		String lOptions = header.get(0);	
		String[] languages = lOptions.split(" ");	//Splits possible languages
		
		if(chosenLang < 0 || chosenLang > languages.length){	//Checks if user inputs are correct
			System.out.println(lang.get("invalidLanguage"));
			
		}else {
		file = new FileReaderWriter("client/res/" + languages[chosenLang] + ".lang");
		List<String> tempLanguage = new ArrayList<String>();
		tempLanguage = file.readFile();
		lang = new HashMap<>();
		lang.put("startMenu",tempLanguage.get(0));
		lang.put("cardNumber", tempLanguage.get(1));
		lang.put("cardCode", tempLanguage.get(2));
		lang.put("languageMenu",tempLanguage.get(3));
		lang.put("mainMenu", tempLanguage.get(4));
		lang.put("balanceText",tempLanguage.get(5));
		lang.put("withdrawAmount", tempLanguage.get(6));
		lang.put("code", tempLanguage.get(7));
		lang.put("depositAmount", tempLanguage.get(8));
		lang.put("wrongCode", tempLanguage.get(9));
		lang.put("noMoney", tempLanguage.get(10));
		lang.put("byePhrase", tempLanguage.get(11));
		lang.put("invalidCardNumber", tempLanguage.get(12));
		lang.put("invalidCardCode", tempLanguage.get(13));
		lang.put("noUser", tempLanguage.get(14));
		lang.put("alreadyLoggedIn", tempLanguage.get(15));
		lang.put("toMuchInsert", tempLanguage.get(16));
		lang.put("negativeInsert", tempLanguage.get(17));
		lang.put("invalidWithdrawCode", tempLanguage.get(18));
		lang.put("validWithdrawal", tempLanguage.get(19));
		lang.put("endWithdrawalCode", tempLanguage.get(20));
		lang.put("invalidLanguage", tempLanguage.get(21));
		lang.put("validDeposit", tempLanguage.get(22));
		lang.put("toMuchWithdrawal", tempLanguage.get(23));
		lang.put("negativeWithdrawal", tempLanguage.get(24));
		}
	}
	/**
	 * Transforms the cardNumber from long to byte
	 * @param cardNumber in long
	 * @return cardNumber in bytes
	 */
	private byte[] longToBytes(long cardNumber) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(cardNumber);
		return buffer.array();
	}
	/**
	 * Validates card number input
	 * @param cardNumber
	 * @return if number is correct
	 */
	private boolean validateCardNumber(long cardNumber) {
		int length = (int) Math.log10(cardNumber) + 1;
		if(length != 16) return false;
		return true;
	}
	/**
	 * Validates card code input
	 * @param cardNumber
	 * @return true if code is correct
	 */
	private boolean validateCardCode(int cardCode) {
		int length = (int) Math.log10(cardCode) + 1;
		if(length != 3) return false;
		
		return true;
	}
	/**
	 * Validates amount range
	 * @param cardNumber
	 * @return true if amount is within range 0 to 1 000 000
	 */
	private boolean validateAmount(int amount) {
		if(amount > 0 && amount < 1000000) return true;
		return false;
	}
	/**
	 * Validates the withdrawal code.
	 * @param cardCode
	 * @return
	 * -1 = Invalid code length
	 * -2 = Code is not a number
	 *  0 = Success
	 */
	private int validateWithdrawalCode(String cardCode) {
		char[] tempArray = cardCode.toCharArray();
		if(tempArray.length != 2) return -1;
		try {
			Integer.parseInt("" + tempArray[0]);
			Integer.parseInt("" + tempArray[1]);
		}catch(NumberFormatException e){
			return -2;
		}
		return 0;
	}
	public static void main(String[] args) throws IOException {
		new ATMClient();	// Starts client
	
	}
}