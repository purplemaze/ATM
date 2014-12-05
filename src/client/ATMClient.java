package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import files.FileReaderWriter;


public class ATMClient {
	private static int connectionPort;
	private static String address;
	private static Socket ATMSocket;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static FileReaderWriter file;
	private static HashMap<String, String> lang;
	private static Scanner scanner;
	private boolean login;
	
	public ATMClient() throws IOException{
		scanner = new Scanner(System.in);
		startClient();
		login = false;
	}
	
	/*
	 * Setting up the bank and starts listening
	 */
	private void startClient() throws IOException{
		readConnection();	//Reads address and port from file.
		//  Creating socket and connects
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
		
		defaultLanguage(); // Reads Language from files
		System.out.println("Connecting to bank..");
		bankGreeting();
		scanLogin();	//Listens to client input
	}
	/*
	 * Reads connectionPort and address from file
	 */
	private void readConnection() throws IOException{	
		file = new FileReaderWriter("client/res/client.config");
		List<String> connectionInfo = new ArrayList<String>();
		connectionInfo = file.readFile();
		connectionPort = Integer.parseInt(connectionInfo.get(0));
		address = connectionInfo.get(1);
	}
	
	private void bankGreeting() throws IOException {
		System.out.println(in.readUTF()); // read greeting
	}
	
	/*
	 * Listens on client input.
	 */
	private void scanLogin() throws IOException {
		while (!login){
			System.out.println(lang.get("startMenu"));
			int chosen = scanner.nextInt();
			if(chosen == 2){
				System.out.println(lang.get("languageMenu"));
				if(scanner.nextInt() == 1){
					choseSwedish();
					System.out.println(lang.get("startMenu"));
				}else{
					defaultLanguage();
					System.out.println(lang.get("startMenu"));
				}
			}if(chosen == 1){
				loginClient();
			}
		}
		scanMenu();
	}
	
	/**
	 * 
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
				System.out.println("Invalid card number, it must be exactly 16 digits"); // Ziad fler språk för fel.
			}
			out.write(longToBytes(cardNumber));  // send card number to server
			
		}
		
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
				System.out.println("Invalid card code, it must be exactly 3 digits"); // Ziad fler språk för fel.
			}
			out.writeInt(cardCode);;  // send card code to server
			
		}else {
			// kanske?
		}
		in.read(loginAns);
		opCode = loginAns[0];
		response = loginAns[1];
    	if(response == 2) { 
    		login = true;
    	}else if(response == -1) {
    		System.out.println("No such user.."); // Ziad fixa sen med språkstöd
    	}else if(response == 0) {
    		System.out.println("You are already logged in"); // Ziad fixa sen med språkstöd
    	}else if(response == 1) {
    		System.out.println("Wrong card code"); // Ziad fixa sen med språkstöd
    	}else {
    		System.err.println("Hur ska vi felhantera detta?, eller ska vi skita i det :S");
    	}
	}
	
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
	
	private void deposit() throws IOException {
		//byte opCode;
		byte response;
		byte[] depositReq = {5, 1};
		byte[] answear = new byte[2];
		out.write(depositReq); // deposit request
		in.read(answear);
		response = answear[1];
		if(response == 0) {
			int amount = 0;
			while(!(validateAmount(amount))) {
				try {
					System.out.println(lang.get("depositAmount"));
					amount = scanner.nextInt();
				}catch (InputMismatchException e){
					scanner.next();
				}	
				if(!(validateAmount(amount))) 
					System.out.println("You can't insert more than 1 000 000:-");
			}
			out.writeInt(amount);
			in.read(answear);
		}
		
	}
	
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
					System.out.println("You can't withdraw more than 1 000 000:-");
			}
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
							System.out.println("Invalid code length, the code needs to be exactly 2 digits");
						}else if(ret == -2) {
							System.out.println("Invalid code, the code needs to be exactly 2 digits");	
						} else {
							break;
						}
					}catch (InputMismatchException e){
						scanner.next();
						System.out.println("Invalid code");
					}
				}
				out.writeUTF(code);
				in.read(answear);
				response = answear[1];
				if(response == 0) {
					System.out.println("Withdrawal successfull");
				}else if (response == -1) {
					System.out.println(lang.get("wrongCode"));
				}else if (response == 1) {
					System.out.println("You have no more codes avaliable.. contact bank for new codes..");
				}else if (response == -2) {
					System.out.println("Insufficient founds! You are poor..");
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
				if(scanner.nextInt() == 1){
					choseSwedish();
					System.out.println(lang.get("mainMenu"));
				}else{
					defaultLanguage();
					System.out.println(lang.get("mainMenu"));
				}
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
	/*
	 * Reads from language file
	 */
	private void defaultLanguage() throws IOException{
		file = new FileReaderWriter("client/res/english.lang");
		List<String> eng = new ArrayList<String>();
		eng = file.readFile();
		lang = new HashMap<>();
		lang.put("startMenu",eng.get(0));
		lang.put("cardNumber", eng.get(1));
		lang.put("cardCode", eng.get(2));
		lang.put("languageMenu",eng.get(3));
		lang.put("mainMenu", eng.get(4));
		lang.put("balanceText", eng.get(5));
		lang.put("withdrawAmount", eng.get(6));
		lang.put("code", eng.get(7));
		lang.put("depositAmount", eng.get(8));
		lang.put("wrongCode", eng.get(9));
		lang.put("noMoney", eng.get(10));
		lang.put("byePhrase", eng.get(11));
	
	}
	private void choseSwedish() throws IOException{
		file = new FileReaderWriter("client/res/english.lang");
		List<String> swe = new ArrayList<String>();
		swe = file.readFile();
		lang.put("startMenu",swe.get(0));
		lang.put("cardNumber", swe.get(1));
		lang.put("cardCode", swe.get(2));
		lang.put("languageMenu",swe.get(3));
		lang.put("mainMenu", swe.get(4));
		lang.put("balanceText", swe.get(5));
		lang.put("withdrawAmount", swe.get(6));
		lang.put("code", swe.get(7));
		lang.put("depositAmount", swe.get(8));
		lang.put("wrongCode", swe.get(9));
		lang.put("noMoney", swe.get(10));
		lang.put("byePhrase", swe.get(11));
		}
	
	
	private byte[] longToBytes(long cardNumber) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(cardNumber);
		return buffer.array();
	}
	
	private boolean validateCardNumber(long cardNumber) {
		int length = (int) Math.log10(cardNumber) + 1;
		if(length != 16) return false;
		
		return true;
	}
	
	private boolean validateCardCode(int cardCode) {
		int length = (int) Math.log10(cardCode) + 1;
		if(length != 3) return false;
		
		return true;
	}
	
	private boolean validateAmount(int amount) {
		if(amount > 0 && amount < 1000000) return true;
		return false;
	}
	
	/**
	 * 
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
		new ATMClient();
	
	}
}
