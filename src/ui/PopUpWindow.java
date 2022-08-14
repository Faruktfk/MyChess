package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import game.Piece;

public class PopUpWindow {

	public static final int WIDTH = 300, HEIGHT = 200;

	private Object output;
	private int currentImageIndex;
	

	/**
	 * Constructor for a reusable Pop-Up for Inputs.
	 * 
	 * @param title Title of the Pop-Up window
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
		imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		panel.add(imageLbl, BorderLayout.CENTER);
		
		JButton leftBtn = new JButton("<");
		leftBtn.setFocusable(false);
		panel.add(leftBtn, BorderLayout.WEST);
		leftBtn.addActionListener(e -> {
			currentImageIndex = currentImageIndex==Piece.QUEEN ? Piece.ROOK : currentImageIndex-1;
			imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		});
		
		JButton rightBtn = new JButton(">");
		rightBtn.setFocusable(false);
		panel.add(rightBtn, BorderLayout.EAST);
		rightBtn.addActionListener(e -> {
			currentImageIndex = currentImageIndex==Piece.ROOK ? Piece.QUEEN : currentImageIndex+1;
			imageLbl.setIcon(new ImageIcon(new ImageIcon(Piece.getPieceImages()[side][currentImageIndex]).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
		});

		String[] options = new String[] {"Save & Exit"};

		int choice = JOptionPane.showOptionDialog(null, dialog, "Pawn Promotion", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			output = currentImageIndex;
		}else if(choice == JOptionPane.CLOSED_OPTION) {
			output = Piece.QUEEN;			
		}

	}
	
	int count = 0;
	public PopUpWindow() {
		JOptionPane jop = new JOptionPane();
		String message = "Waiting for an opponent.";
		String loadingChar = ".";
		jop.setMessage(message);
		JDialog dialog = jop.createDialog("Chess Room - Message!");
		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String load = "";
				for(int i = 0; i<count%30; i++) {
					load+=loadingChar;
				}
				jop.setMessage(message + "" + load);
				count+=1; 
				
				System.out.println(count);
				
				
			}
		}).start();
		dialog.setVisible(true);
		
	}
	
	public Object getInput() {
		return output;
	}
	

}
