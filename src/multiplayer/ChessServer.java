package multiplayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class ChessServer implements ActionListener {

	public static void main(String[] args) {
		new ChessServer();
	}

	public static final int WIDTH = 300, HEIGHT = 500; 
	public static final String ON = "ON", OFF = "OFF";
	private Color onColor = new Color(127, 184, 0), offColor = new Color(215, 38, 61);
	private boolean serverOn;

	private ServerSocket serverSocket;
	private int port;
	private JTextField portField;
	private JLabel clientCountLbl, consoleLbl;
	private JButton serverBtn;

	public ChessServer() {
		serverOn = false;
		port = 4999;

		JFrame frame = new JFrame("ChessServer");
		frame.setSize(WIDTH, HEIGHT);
		frame.setResizable(false);
//		frame.setLocationRelativeTo(null);
		frame.setLocation(100,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel portLbl = new JLabel("Port:");
		portLbl.setBounds(20, 41, 91, 31);
		frame.getContentPane().add(portLbl);

		JLabel clientsLbl = new JLabel("Clients:");
		clientsLbl.setBounds(20, 89, 91, 31);
		frame.getContentPane().add(clientsLbl);

		consoleLbl = new JLabel("");
		consoleLbl.setBorder(new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		consoleLbl.setBounds(10, 131, 261, 178);
		frame.getContentPane().add(consoleLbl);

		clientCountLbl = new JLabel("0");
		clientCountLbl.setBounds(121, 89, 118, 31);
		frame.getContentPane().add(clientCountLbl);

		serverBtn = new JButton(serverOn ? ON : OFF);
		serverBtn.setFocusable(true);
		serverBtn.setFont(new Font("Arial Black", Font.BOLD, 23));
		serverBtn.setBackground(serverOn ? onColor : offColor);
		serverBtn.setBounds(60, 334, 163, 79);
		frame.getContentPane().add(serverBtn);
		serverBtn.addActionListener(this);

		portField = new JTextField();
		portField.setText(port + "");
		portField.setBounds(121, 41, 115, 31);
		portField.setColumns(10);
		frame.getContentPane().add(portField);
		portField.addActionListener(this);

		frame.setVisible(true);

	}

	private void startServer() {
		int tempPort = port;
		try {
			tempPort = Integer.parseInt(portField.getText());
		} catch (NumberFormatException e1) {
			serverOn = false;
			portField.setText(port+ "");
			startServer();
			return;
		}
		

		try {
			serverSocket = new ServerSocket(tempPort);
			System.out.println("Server is running...");
			new Thread(() -> {
				while (!serverSocket.isClosed()) {
					try {
						Socket client = serverSocket.accept();
						ChessRoom.findAvailableChessRoom().addPlayer(client);

					} catch (IOException e) {
						closeServer();
					}
				}
			}).start();
		} catch (IOException e) {
			closeServer();
		
		} catch(IllegalArgumentException e1) {
			serverOn = false;
			portField.setText(port + "");
			startServer();
			return;	
		}
		
		serverBtn.setText(ON);
		serverBtn.setBackground(onColor);		
		port = tempPort;
		
		new Thread(() -> {
			while(serverOn) {
				try {
					Thread.sleep(1000);
					clientCountLbl.setText(ChessRoom.getPlayerCount() + "");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();


	}

	private void closeServer() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			System.out.println("Server shut down!");
			int numOfClients = ChessRoom.getPlayerCount();
			if(numOfClients>0)ChessRoom.broadcastClose();
			clientCountLbl.setText(ChessRoom.getPlayerCount() + "");

			new Thread(() -> {
				try {
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
		serverBtn.setText(OFF);
		serverBtn.setBackground(offColor);
		serverOn = false;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(serverBtn)) {
			if (serverOn) {
				closeServer();				
			} else {
				startServer();
			}
			serverOn = !serverOn;
		}

	}
}
