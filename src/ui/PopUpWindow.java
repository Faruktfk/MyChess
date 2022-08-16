package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import game.Piece;
import multiplayer.ChessClient;

public class PopUpWindow {

	public static final int WIDTH = 300, HEIGHT = 200;
	public static final String ALLOWED = "111", DENIED = "000";

	private Object output;
	private int currentImageIndex;

	/**
	 * Constructor for a reusable Pop-Up for Inputs.
	 * 
	 * @param title      Title of the Pop-Up window
	 * @param attributes Different Input names
	 */
	public PopUpWindow(String title, String[] attributes) {
		if (attributes == null || attributes.length == 0) {
			return;
		}
		JTextField[] tfs = new JTextField[attributes.length];

		JPanel dialog = new JPanel();
		dialog.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		Font lblFont = new Font("Arial", Font.PLAIN, 14);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(WIDTH - 20, (int) (HEIGHT * 0.9)));
		panel.setLayout(new GridLayout(attributes.length, 2, 10, 10));
		dialog.add(panel);

		for (int i = 0; i < attributes.length; i++) {
			JLabel label = new JLabel(attributes[i].strip() + ":");
			label.setFont(lblFont);
			panel.add(label);

			tfs[i] = new JTextField();
			tfs[i].setFont(lblFont);
			panel.add(tfs[i]);
		}

		String[] options = new String[] { "Save & Exit", "Exit" };

		int choice = JOptionPane.showOptionDialog(null, dialog, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

		if (choice == 0) {
			String[] tempOutputs = new String[attributes.length];
			boolean isDone = true;
			for (int i = 0; i < tempOutputs.length; i++) {
				String input = tfs[i].getText().strip().trim();
				tempOutputs[i] = input;
				if (input.isBlank()) {
					isDone = false;
				}
			}
			if (isDone) {
				output = tempOutputs;
			}
		}

	}

	/**
	 * Constructor for Pawn-Promotion Pop-Up.
	 * 
	 * @param side Player's side (i.e. Black or White).
	 */
	public PopUpWindow(int side) {
		JPanel dialog = new JPanel();
		dialog.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(WIDTH - 20, (int) (HEIGHT * 0.9)));
		panel.setLayout(new BorderLayout());
		dialog.add(panel);

		currentImageIndex = Piece.QUEEN;
		JLabel imageLbl = new JLabel();
		imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage()
				.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		panel.add(imageLbl, BorderLayout.CENTER);

		JButton leftBtn = new JButton("<");
		leftBtn.setFocusable(false);
		panel.add(leftBtn, BorderLayout.WEST);
		leftBtn.addActionListener(e -> {
			currentImageIndex = currentImageIndex == Piece.QUEEN ? Piece.ROOK : currentImageIndex - 1;
			imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage()
					.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		});

		JButton rightBtn = new JButton(">");
		rightBtn.setFocusable(false);
		panel.add(rightBtn, BorderLayout.EAST);
		rightBtn.addActionListener(e -> {
			currentImageIndex = currentImageIndex == Piece.ROOK ? Piece.QUEEN : currentImageIndex + 1;
			imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage()
					.getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		});

		String[] options = new String[] { "Save & Exit" };

		int choice = JOptionPane.showOptionDialog(null, dialog, "Pawn Promotion", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			output = currentImageIndex;
		} else if (choice == JOptionPane.CLOSED_OPTION) {
			output = Piece.QUEEN;
		}

	}


/**
 * Constructor for Waiting Pop-Up. 
 * To let the user know, that he has to wait for another player.
 * 
 * @param client A ChessClient Object is needed to detect an opponent's connection.
 */
	public PopUpWindow(ChessClient client) {
		int width = 400, height = 200;
		JPanel dialog = new JPanel();

		JPanel panel = new JPanel();
		panel.setBounds(width / 8 - 8, height / 8, width * 6 / 8, height * 5 / 8);

		panel.setLayout(new GridLayout(2, 1));
		dialog.add(panel);

		JLabel msg = new JLabel("Waiting for an opponent...");
		msg.setHorizontalAlignment(JLabel.CENTER);
		msg.setOpaque(true);
		panel.add(msg);

		LoadingPanel loading = new LoadingPanel(client);
		panel.add(loading);

		String[] options = new String[] { "Cancel" };
		JOptionPane.showOptionDialog(null, dialog, "Waiting...", JOptionPane.CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);

		output = DENIED;

	}

	public Object getInput() {
		return output;
	}

	@SuppressWarnings("serial")
	private class LoadingPanel extends JPanel implements ActionListener {

		private Timer timer;
		private int maxX, maxY;
		private int progress;
		private ChessClient client;

		public LoadingPanel(ChessClient client) {
			timer = new Timer(500, this);
			this.client = client;
			int width = 145;
			int height = 10;

			maxX = width - 2;
			maxY = height;

			progress = 10;
			timer.start();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			progress = progress >= maxX ? 10 : progress + 10;
			repaint();

			Window[] windows = Window.getWindows();
			for (int i = 0; i < windows.length; i++) {
				if (windows[i] instanceof JDialog) {
					JDialog d = (JDialog)windows[i];
					if (d.getTitle().equals("Waiting...") && !d.isActive()) {
						output = DENIED;
						timer.stop();							
					}
				}

			}
			if (client.getIsOpponentPresent()) {
				output = ALLOWED;
				timer.stop();
			}

		}

		@Override
		protected void paintComponent(Graphics a) {
			super.paintComponent(a);
			Graphics2D g = (Graphics2D) a;

			g.drawRect(0, 5, maxX, maxY);

			g.setColor(new Color(0, 230, 0));

			g.fillRect(1, 6, progress, maxY - 1);

		}

	}

}
