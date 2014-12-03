package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ATMClient {
	private static int connectionPort;
	private static String address;
	private static Socket ATMSocket = null;
	private static PrintWriter out = null;
	private static InputStreamReader isr = null;
	private static BufferedReader in = null;
	
	
	public ATMClient() throws IOException{
		startClient();
	}
	
	/*
	 * Setting up the bank and starts listening
	 */
	private void startClient() throws IOException{
		readConnection();	//Reads address and port from file.
		//  Creating socket and connects
		try{
			ATMSocket = new Socket(address, connectionPort);
			out = new PrintWriter(ATMSocket.getOutputStream(), true);
			isr = new InputStreamReader(ATMSocket.getInputStream());
			in = new BufferedReader(isr);
			
		}catch (UnknownHostException e){
			System.err.println("Unknown host: " + address);
			System.exit(1);
		}catch (IOException e){
			System.err.println("Couldn't open connection to port " + connectionPort);
			System.exit(1);
		}
		System.out.println("Connected to bank");
		scanInput();	//Listens to client input
	}
	/*
	 * Reads connectionPort and address from file
	 */
	private void readConnection() throws IOException{
		/*
		FileReader file = new FileReader("directory");
		List<String> info = new ArrayList<Sring>();
		info = file.readInfo();
		
		connectionPort = Integer.parseInt(info.get(0));
		address = info.get(1);
		System.out.println("readConnection test! Adress: " + address + " port: " + connectionPort);
		*/
		address = "127.0.0.1";
		connectionPort = 8989;
		if (address == ""){
			throw new IllegalArgumentException();
		}
	}	
	/*
	 * Listens on client input.
	 */
	private void scanInput() throws IOException {
		Scanner scanner = new Scanner(System.in);
		Boolean listening = true;
	
		while (listening){
			if(scanner.nextInt() == 0){
				System.out.println("Bank closing Byeeeee");
				listening = false;
				 out.close();
			     in.close();
			     ATMSocket.close();
			}
		}
		scanner.close();
		
	}
	
	public static void main(String[] args) throws IOException {
		new ATMClient();

	}
}

