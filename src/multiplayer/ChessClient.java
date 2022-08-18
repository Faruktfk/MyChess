package multiplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class ChessClient {
	
	public static final String EMPTY="empty", MOVE = "move";	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private boolean isConnection;
	private boolean stopWaitingForOpponent;
	private int side = -1;
	private int turn;
	
	private String lastMove = EMPTY;

	public ChessClient(String playerName, String ip, int port) {
		stopWaitingForOpponent = false;
		try {
			socket = new Socket(ip, port);
			isConnection = true;
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						
			bufferedWriter.write(playerName);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			String initMsg = bufferedReader.readLine().strip().trim();
			if(initMsg.equals(ChessRoom.CLOSE_COMMAND)) {
				isConnection = false;
			}else if(initMsg.equals("0") || initMsg.equals("1")) {
				side = Integer.parseInt(initMsg);
				turn = side;
			}
			
			listenIncoming();
						
		} catch (IOException e) {
			closeEverything();
		}catch (IllegalArgumentException e) {
			System.out.println("Port is out of valid range!");
		}
		
		if(socket != null && !socket.isConnected()) {
			closeEverything();
		}
		
	} 

	public void sendMessage(String message) {
		try {
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			closeEverything();
		}
	}
	
	public String recieveMessage() {
		String temp = lastMove;
		lastMove = EMPTY;
		return temp;
	}
	
	private void listenIncoming() {
		new Thread(() -> {
			while(isConnection) {
				try {
					String message = bufferedReader.readLine();
					if(message == null || message.equals(ChessRoom.CLOSE_COMMAND)) {
						isConnection = false;
					}else if(message.contains(ChessRoom.DONTWAIT_COMMAND))  {
						stopWaitingForOpponent = true;
					}else if(message.contains(MOVE))  {
						lastMove = message.split(MOVE)[1];
					}
				} catch (IOException e) {
					closeEverything();
				}
			}
		}).start();
	}
	
	public void closeCommand() {
		if(isConnection) {
			try {
				bufferedWriter.write(ChessRoom.CLOSE_COMMAND);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			} catch (IOException e) {
				closeEverything();
			}
		}
		
	}
	
	private void closeEverything() {
		System.out.println("Error occured, connection will be closed.");
		isConnection = false;
		try {
			if(bufferedReader!=null)bufferedReader.close();
			if(bufferedWriter!=null)bufferedWriter.close();
			if(socket!=null)socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getSide() {
		return side;
	}
	
	public int getTurn() {
		return turn;
	}
	
	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	public boolean getIsConnection() {
		return isConnection;
	}
	
	public boolean getStopWaitingForOpponent() {
		return stopWaitingForOpponent;
	}
}
