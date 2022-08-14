package game;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Piece {

	public static final int KING = 0, QUEEN = 1, BISHOP = 2, KNIGHT = 3, ROOK = 4, PAWN = 5;
	private static final String NAME_SHORT = "KQBNRP"; 
	public static final int WHITE = 0, BLACK = 1;
	public static final int[][][] moves = new int[][][] { //x, y | top, top-right-diagonal, right, bottom-right-diagonal, bottom, bottom-left-diagonal, left, top-left-diagonal
		{{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {2, 0}, {-2, 0}},	// King
		{{0,99},{99,99},{99,0},{99,-99},{0,-99},{-99,-99},{-99,0},{-99,99}},	// Queen
		{{0,0},{99,99}, {0,0}, {99, -99},{0,0},{-99, -99}, {0,0}, {-99, 99}},	// Bishop
		{{1, 2},{2, 1},{2, -1},{1, -2},{-1, -2},{-2, -1},{-2, 1},{-1, 2}},		// Knight
		{{0, 99},{0, 0},{99, 0},{0, 0},{0, -99},{0, 0},{-99, 0},{0, 0}},		// Rook
		{{0, 1}, {0, 2}, {1, 1}, {-1, 1}}										// Pawn		
	};
	private static BufferedImage[][] pieceImages;
	
	public static BufferedImage[][] getPieceImages(){
		return pieceImages;
	}
	
	private BufferedImage image;
	private int name;
	private char nameShort;
	private int side;
	private boolean alreadyMoved;
	private Point location;
	private int countAvailablePos;
	
	
	public Piece(int name, int side, int x, int y) {
		this.name = name;
		nameShort = NAME_SHORT.charAt(name);
		this.side = side;
		this.location = new Point(x % 8, y % 8);
		this.alreadyMoved = false;

		try {
			if (pieceImages == null) {
				pieceImages = new BufferedImage[2][6];
				BufferedImage sprite = ImageIO.read(getClass().getResourceAsStream("/Chess_Pieces.png"));
				int imageHeight = sprite.getHeight() / 2;
				int imageWidth = sprite.getWidth() / 6;
				for(int s = 0; s<pieceImages.length; s++) {
					for(int i = 0; i<pieceImages[s].length; i++) {
						pieceImages[s][i] = sprite.getSubimage(i * imageWidth, s * imageHeight, imageWidth, imageHeight);
					}
				}
			}

			image = pieceImages[side][name];
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void promotePawn(int name) {
		if(this.name != PAWN)return;
		this.name = name;
		this.nameShort = NAME_SHORT.charAt(name);
		this.image = pieceImages[side][name];		
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getName() {
		return name;
	}
	
	public char getNameShort() {
		return nameShort;
	}
	
	public int getSide() {
		return side;
	}
	
	public String printLocation() {
		return "[" + location.getX() + "," + location.getY() + "]";
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setLocation(int x, int y) {
		this.location = new Point(x,y);
	}
	
	public boolean getAlreadyMoved() {
		return alreadyMoved;
	}
	
	protected void sAlreadyMoved() {
		alreadyMoved = true;
	}

	public int getCountAvailablePos() {
		return countAvailablePos;
	}

	public void setCountAvailablePos(int countAvailablePos) {
		this.countAvailablePos = countAvailablePos;
	}
	

}
