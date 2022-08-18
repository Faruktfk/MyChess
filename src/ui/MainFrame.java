package ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Font;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import game.GameLogic;
import game.Piece;
import multiplayer.ChessClient;

import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;


@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ComponentListener, WindowListener {
	
	public static void main(String[] args) {
		new MainFrame();		
	}
	private final String APP_NAME = "MyChess";
	private final int WIDTH = 1000, HEIGHT = 900;
	private int marginX = 0, marginY = 0;
	private ChessClient chessClient;

	public MainFrame() {
		setTitle(APP_NAME);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		addComponentListener(this);
		addWindowListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		getContentPane().setBackground(Color.darkGray);
		setMinimumSize(new Dimension(BoardPanel.WIDTH+100, BoardPanel.HEIGHT+100));
		
		
		BoardPanel chessBoardPanel = new BoardPanel(Piece.WHITE);		
		chessBoardPanel.setLocation((WIDTH-BoardPanel.WIDTH-20)/2+marginX, (HEIGHT-BoardPanel.HEIGHT-60)/2 + marginY);
		getContentPane().add(chessBoardPanel);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setFont(new Font("Bahnschrift", Font.PLAIN, 13));
		menuBar.setBackground(Color.WHITE);
		setJMenuBar(menuBar);
		
		JMenu firstMenu = new JMenu("Settings");
		firstMenu.setFont(new Font("Calibri", Font.PLAIN, 15));
		firstMenu.setBackground(Color.WHITE);
		menuBar.add(firstMenu);
		
		JMenuItem resartGame = new JMenuItem("New Game");
		resartGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		resartGame.setFont(new Font("Calibri", Font.PLAIN, 12));
		resartGame.setBackground(Color.WHITE);
		resartGame.addActionListener(e -> chessBoardPanel.newGame(GameLogic.LOCAL, Piece.WHITE));
		firstMenu.add(resartGame);
		
		JMenu gameModeMenu = new JMenu("Game Mode");
		gameModeMenu.setFont(new Font("Calibri", Font.PLAIN, 12));
		gameModeMenu.setBackground(Color.WHITE);
		firstMenu.add(gameModeMenu);

		JCheckBoxMenuItem localGameM = new JCheckBoxMenuItem("Local");
		JCheckBoxMenuItem networkGameM = new JCheckBoxMenuItem("Network");
		localGameM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		localGameM.setSelected(true);
		localGameM.setFont(new Font("Calibri", Font.PLAIN, 12));
		localGameM.setBackground(Color.WHITE);
		localGameM.addActionListener(e -> {
			if (chessClient != null) {
				chessClient.closeCommand();
			}
			setTitle(APP_NAME);
			networkGameM.setSelected(false);
			chessBoardPanel.newGame(GameLogic.LOCAL, Piece.WHITE);
			chessClient = null;
		}); 
		gameModeMenu.add(localGameM);
		
		networkGameM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		networkGameM.setFont(new Font("Calibri", Font.PLAIN, 12));
		networkGameM.setBackground(Color.WHITE);
		networkGameM.addActionListener(e -> {
			PopUpWindow popup = new PopUpWindow("Settings", new String[] { "Player name", "Server IP-Address", "Server Port" });
			String[] inputs = (String[]) popup.getInput();
			if (inputs == null) {
				networkGameM.setSelected(false);
				localGameM.setSelected(true);
				return;
			}
//			System.out.println("Username: " + inputs[0]);

			try {
				chessClient = new ChessClient(inputs[0], inputs[1], Integer.parseInt(inputs[2]));

				int side = chessClient.getSide();
				if (side >= 0) {
					if(new PopUpWindow(chessClient).getInput().equals(PopUpWindow.DENIED)) {
						if (chessClient != null) {
							chessClient.closeCommand();
						}
						chessClient = null;							
						chessBoardPanel.newGame(GameLogic.LOCAL, Piece.WHITE);
						localGameM.setSelected(true);
						networkGameM.setSelected(false);
						return;
					}
					
					localGameM.setSelected(false);
					GameLogic game = chessBoardPanel.newGame(GameLogic.NETWORK, side);
					setTitle(APP_NAME + " - " + inputs[0] + " - " + inputs[1] + " - " + inputs[2]);
					new Thread(() -> {
						while(game != null && chessClient != null && chessClient.getIsConnection()) {
							try {
								Thread.sleep(1000);
								if (game != null && chessClient != null) {
									String lastMove = game.getLastMove();
									if (chessClient.getTurn() != game.getTurn()
											&& !lastMove.equals(ChessClient.EMPTY)) {
										chessClient.sendMessage(lastMove);
									}
									String networkMove = chessClient.recieveMessage();
									if(!networkMove.equals(ChessClient.EMPTY)) {
										game.networkMove(networkMove);
										chessBoardPanel.repaint();
									}
								}
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
						localGameM.doClick();
					}).start();
				} else {
					chessClient = null;
					chessBoardPanel.newGame(GameLogic.LOCAL, Piece.WHITE);
					localGameM.setSelected(true);
					networkGameM.setSelected(false);

				}
			} catch (NumberFormatException e1) {
				System.out.println("Invalid port given!");
			}

		}); 
		gameModeMenu.add(networkGameM);
		
		JMenuItem quitGame = new JMenuItem("Quit");
		quitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		quitGame.setFont(new Font("Calibri", Font.PLAIN, 12));
		quitGame.setBackground(Color.WHITE);
		quitGame.addActionListener(e -> {
			if(chessClient != null) {
				chessClient.closeCommand();
			}
			System.exit(0);
		});
		firstMenu.add(quitGame);
		
		setVisible(true);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		getContentPane().getComponent(0).setLocation((getWidth()-BoardPanel.WIDTH-20)/2+marginX, (getHeight()-BoardPanel.HEIGHT-60)/2 + marginY);		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if(chessClient != null) {
			chessClient.closeCommand();
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}


	@Override
	public void windowClosed(WindowEvent e) {
	}
 
	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}


}

//--Done
//Pawn promotion!!
//If black is my side, the board needs to be rotated accordingly +
//Networking
//En passan


//--In progress
// TODO: Wait for the opponent!!!!


//--Future
// TODO: Networking issues
// TODO: Save the current game
// TODO: Kings cannot get too close to each other!!
// TODO: Stalemate : when the King is not under threat and cannot move to anywhere else. => draw (No one wins)
// TODO: Check mate


