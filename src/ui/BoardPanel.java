package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import game.GameLogic;
import game.Piece;


@SuppressWarnings("serial")
public class BoardPanel extends JPanel implements MouseListener {

	public static final int WIDTH = 800, HEIGHT = 600;
	private int marginX, marginY, bSize;

	private Point sqrToLight;
	private ArrayList<Point> possibleMoves;
	private GameLogic game;
	private Color bright = new Color(238, 238, 210), dark = new Color(118, 150, 86);

	public BoardPanel(int side) {
		setSize(WIDTH, HEIGHT);
		addMouseListener(this);
		game = new GameLogic(this, GameLogic.LOCAL, side);
		
		 marginX = 205;
		 marginY = 5;
		 bSize = (HEIGHT-marginY) / 8;

		setVisible(true);

	}

	@Override
	protected void paintComponent(Graphics a) {
		super.paintComponent(a);
		Graphics2D g = (Graphics2D) a;
		
		String horizontalLabels = game.getTileLocationLabel(true);
		String verticalLabels = game.getTileLocationLabel(false);
	
		//TODO : here! For dash board!! 
		g.fillRect(5, marginY, marginX-20 , HEIGHT - marginY*2 );
		
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				
				if ((x + y) % 2 == 0) {
					g.setColor(bright);
				} else {
					g.setColor(dark);
				}
				
				g.fillRect(bSize * x + marginX , bSize * y + marginY, bSize, bSize);		
				
				g.setColor(g.getColor() == bright ? dark : bright);
				g.setFont(new Font("Arial", Font.BOLD, 15));
				if(x == 0) {
					g.drawString(verticalLabels.charAt(7-y)+"", bSize * x + marginX+bSize/20, bSize*y+marginY+bSize/5);					
				}
				if(y == 7) {
					g.drawString(horizontalLabels.charAt(x)+"", bSize * (x+1) + marginX-bSize/7, bSize*(y+1)+marginY-bSize/20);
				}
				//TODO: For debugging
//				g.setFont(new Font("Arial", Font.BOLD, 13));
//				g.setColor(Color.blue);
//				g.drawString("["+x+","+y+"]", x*bSize+marginX, y*bSize+11+marginY);
				// Until here
			}
			
		}
		

		updateBoard(g);

		if (sqrToLight != null) {
			g.setColor(Color.yellow);
			g.drawRect(sqrToLight.x+marginX, sqrToLight.y+marginY, bSize, bSize);
		}

		if (possibleMoves != null) {
			for (int i = 0; i < possibleMoves.size(); i++) {
				g.setColor(new Color(136, 252, 136));
				g.fillArc((int) (possibleMoves.get(i).x * bSize + bSize / 3) + marginX,
						(int) (possibleMoves.get(i).y * bSize + bSize / 3) + marginY, bSize / 3, bSize / 3, 0, 360);

			}
		}	
	}

	private void updateBoard(Graphics2D g) {
		for (int y = 0; y < game.getBoard().length; y++) {
			for (int x = 0; x < game.getBoard()[y].length; x++) {
				if (game.getBoard()[y][x] != null) {
					Piece piece = game.getBoard()[y][x];
					g.drawImage(piece.getImage(), (int)piece.getLocation().getX() * bSize + marginX, (int)piece.getLocation().getY() * bSize + marginY, bSize, bSize, null);
				}
			}
		}
	}
	
	public void emptyPossibleMoves() {
		possibleMoves = null;
	}
	
	public GameLogic newGame(int gameMode, int side) {
		game = new GameLogic(this, gameMode, side);
		emptyPossibleMoves();
		sqrToLight = null;
		repaint();
		return game;
	}
	
	public void changeTheme() {
		bright = bright.equals(new Color(238, 238, 210)) ?  new Color(227, 202, 178) : new Color(238, 238, 210);
		dark = dark.equals(new Color(118, 150, 86)) ? new Color(162, 110, 91) : new Color(118, 150, 86);
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//TODO: for debugging
		if(e.getButton() == 3) {
		Piece[][] b = game.getBoard();
		int count = 0;
		for(int v = 0; v<b.length; v++) {
			for(int h = 0; h<b[v].length; h++) {
				if(b[v][h]!=null) {
					count++;
				}
			}
		}
		System.out.println("Pieces left:" + count);
		changeTheme();
		game.printBoard();
		return;
		}
		//until here!
		
		
		int x = (e.getX()-marginX) / bSize;
		int y = (e.getY()-marginY) / bSize;
		
		if(x < 0 || y < 0 || x >= 8 || y >= 8)return;
		
		if (sqrToLight == null) {
			if (game.getBoard()[y][x] == null || game.getBoard()[y][x].getSide() != game.getTurn())
				return;
			sqrToLight = new Point(x * bSize, y * bSize);
			possibleMoves = game.getPossibleMoves(x, y);
		}else {
			if(possibleMoves != null && possibleMoves.contains(new Point(x,y))){
				game.move(sqrToLight.x/bSize, sqrToLight.y/bSize,x,y);
			}
			sqrToLight = null;
			emptyPossibleMoves();
		}

		repaint();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}