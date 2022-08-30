package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import multiplayer.ChessClient;
import ui.BoardPanel;
import ui.PopUpWindow;


public class GameLogic {
	public static int LOCAL = 0, NETWORK = 1;
	public static String HORIZONTAL= "abcdefgh", VERTICAL = "12345678";
	private static final int CALCULATE_FOR_MOVE = 10, CALCULATE_FOR_CHECK = 11, CALCULATE_FOR_CHECKMATE = 12;
	
	private BoardPanel panelUI;
	private Piece[][] board;
	private int gameMode;
	private int side;
	private int turn = Piece.WHITE;
	private int check;
	private Piece[] kings;
	private HashMap<Piece, Point> enPassant;
	private int castling;
	
	private String lastMove = ChessClient.EMPTY;



	public GameLogic(BoardPanel panelUI, int gameMode, int side) {
		this.panelUI = panelUI;
		this.gameMode = gameMode;
		this.side = side;
		board = new Piece[8][8];
		kings = new Piece[2];
		enPassant = new HashMap<>();
		check = -1;
		castling = -1;
		

		int[] order = new int[] { Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK };

		for (int k = 0; k<2; k++) {
			int s = side==Piece.WHITE ? k : 1-k;
			for (int i = 0; i < order.length; i++) {
				int orderIndex = side == Piece.WHITE ? i : (order.length-1) - i;				
				Piece pawn = new Piece(Piece.PAWN, 1 - s, i, 1 + 5 * k);
				board[1 + 5 * k][i] = pawn;
				Piece piece = new Piece(order[orderIndex], 1 - s, i, 7 * k);
				if(order[orderIndex] == Piece.KING ) {
					kings[piece.getSide()] = piece;
				}
				board[7 * k][i] = piece;
			}
		}
	}

	public ArrayList<Point> getPossibleMoves(int x, int y) {
		if(board[y][x].getSide() != turn || (gameMode == GameLogic.NETWORK && turn!=side)) {
			return null;
		}
		return calculatePMoves(x, y, CALCULATE_FOR_MOVE);
	}
	
	private ArrayList<Point> calculatePMoves(int x, int y, int purpose){
		Piece piece = board[y][x];
		ArrayList<Point> output = new ArrayList<>();
		int[][] moves = Piece.moves[piece.getName()];
		int direction = side == piece.getSide() ? -1 : 1;

		for (int i = 0; i < moves.length; i++) {
			int dx = moves[i][0] * direction;
			int dy = moves[i][1] * direction;

			if (dy == 99 || dy == -99 || dx == 99 || dx == -99) {
				dx = dx == 99 ? 1 : dx == -99 ? -1 : 0;
				dy = dy == 99 ? 1 : dy == -99 ? -1 : 0;
				int nx = x + dx;
				int ny = y + dy;
				boolean out = nx < 0 || nx >= board[0].length || ny < 0 || ny >= board.length ? true : false;
				while (!out && (board[ny][nx] == null || purpose == CALCULATE_FOR_CHECKMATE)) {
					output.add(new Point(nx, ny));
					nx += dx;
					ny += dy;
					if (nx < 0 || nx >= board[0].length || ny < 0 || ny >= board.length) {
						out = true;
					}
				}
				if (!out && board[ny][nx].getSide() != piece.getSide()) {
					output.add(new Point(nx, ny));
				}

			} else {

				if (dx == 0 && dy == 0 || dx + x < 0 || dx + x >= board[0].length || dy + y < 0
						|| dy + y >= board.length
						|| board[dy + y][dx + x] != null && board[dy + y][dx + x].getSide() == piece.getSide()) {
					continue;
				}
				if (piece.getName() == Piece.PAWN && (i == 0 && board[dy + y][dx + x] != null || i == 1 && ((y != 1 && y != 6) || board[dy/2+y][dx+x]!=null || board[dy+y][dx+x]!=null) || i > 1 && purpose == CALCULATE_FOR_MOVE && board[dy + y][dx + x] == null)) {
					continue;
				}
				
				if(piece.getName() == Piece.KING && i>7 && (piece.getAlreadyMoved() ||  (dx==-2 ? board[y][3] != null || board[y][1] != null || board[y][0] == null || board[y][0].getAlreadyMoved() : board[y][5] != null || board[y][7] == null || board[y][7].getAlreadyMoved())) ) {
					continue;
				}else if (piece.getName() == Piece.KING && i>7 && purpose == CALCULATE_FOR_MOVE){
					castling = piece.getSide();
				}
					
				output.add(new Point(x + dx, y + dy));									
			}
		}
		
		if(enPassant.containsKey(piece)) {
			output.add(enPassant.get(piece));	
		}	
		
		if(piece.getName() == Piece.KING && piece.getSide() == turn && purpose == CALCULATE_FOR_MOVE) {
			int oldNumofOutput = output.size();
			String oldOutput = output.toString();
			for(int yOpponent = 0; yOpponent<board.length; yOpponent++) {
				for(int xOpponent = 0; xOpponent<board[yOpponent].length; xOpponent++) {
					Piece opponent = board[yOpponent][xOpponent];
					if(opponent != null && opponent.getSide()!=turn) {
						ArrayList<Point> opponentPMoves = calculatePMoves(xOpponent, yOpponent, CALCULATE_FOR_CHECKMATE);
						if(opponentPMoves != null) {
							output.removeAll(opponentPMoves);
						}
					}
				}
			}
			System.out.println(piece.getSide() + " King: "+ oldNumofOutput + " -> " +output.size() + "\t||\t" + oldOutput + " -> " + output.toString());
			
		}
		
		piece.setCountAvailablePos(output.size());
		return output;
	}
	
	public void networkMove(String move) {
		ArrayList<Integer> xy = new ArrayList<>();
		String[] locs = move.split("#")[1].split("-");
		for(int i = 0; i<2; i++) {
			xy.add(getTileLocationLabel(true).indexOf(locs[i].split(",")[0]));
			xy.add(7-getTileLocationLabel(false).indexOf(locs[i].split(",")[1]));
		}
		move(xy.get(0), xy.get(1), xy.get(2), xy.get(3));
	}

	public void move(int oldX, int oldY, int x, int y) {
		Piece piece = board[oldY][oldX];
				
		if (piece.getSide() == turn && (board[y][x] != kings[0] && board[y][x] != kings[1])) {
			
			Piece tempEnPassantPiece = null;
						
			//En passant in action
			if(enPassant.containsKey(piece) && enPassant.get(piece).equals(new Point(x,y))) {
				tempEnPassantPiece = board[oldY][x];
				board[oldY][x] = null;
			}			
			Piece tempPiece = board[y][x];
			board[y][x] = piece;
			board[oldY][oldX] = null;
			piece.setLocation(x, y);
			
			// In case of Check
			int tempCheck = check;
			check = isInCheck();
			if(check==turn) {
				board[y][x] = tempPiece;
				board[oldY][oldX] = piece;
				if(tempEnPassantPiece!=null)
					board[oldY][x] = tempEnPassantPiece;
				piece.setLocation(oldX, oldY);
				check = tempCheck;
				return;
			}
//			if(check != -1) { TODO: for check game over, i guess...
//			}
			
			//Castling
			if(piece.getName() == Piece.KING && piece.getSide() == castling) {
				int rookOldX = x - oldX == -2 ? 0 : 7;
				int rookNewX = rookOldX == 0 ? 3 : 5;
				Piece pieceRook = board[y][rookOldX];
				board[y][rookNewX] = pieceRook;				
				board[y][rookOldX] = null;	
				pieceRook.setLocation(rookNewX, y);
				pieceRook.sAlreadyMoved();
			}
			castling = -1;
			
			enPassant.clear();
			piece.sAlreadyMoved();
			
			//Pawn promotion
			if(piece.getName() == Piece.PAWN && (piece.getSide() == side ? piece.getLocation().y == 0 : piece.getLocation().y == 7)) {
				panelUI.emptyPossibleMoves();
				panelUI.repaint();
				piece.promotePawn((int) new PopUpWindow(turn).getInput());	
			}
			
			
			turn = turn == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
			lastMove = ChessClient.MOVE + ":"+ piece.getNameShort() + "#" 
					+ getTileLocationLabel(true).charAt(oldX) + "," + getTileLocationLabel(false).charAt(7-oldY) + "-" 
					+ getTileLocationLabel(true).charAt(x) + "," + getTileLocationLabel(false).charAt(7-y); // Piece short name # oldx,oldy - x,y	//To send the last move to other player. 
			
			
			//Looking for En passant 
			if(piece.getName() == Piece.PAWN && Math.abs(oldY-y)==2) {
				for(int i = -1; i<2; i++) {
					if(x+i < 0 || x+i >= board.length || i == 0 || board[y][x+i]==null || piece.getSide() == board[y][x+i].getSide() || board[y][x+i].getName()!=Piece.PAWN) continue;
					enPassant.put(board[y][x+i], new Point(oldX, oldY+(y-oldY)/2));				
				}
			}		
			
		}
	
	}
	
	//Looking for Check
	private int isInCheck() { 
		for(int y = 0; y<board.length; y++) {
			for(int x = 0; x<board[y].length; x++) {
				if(board[y][x] == null)continue;
				ArrayList<Point> possibleActions = calculatePMoves(x, y, CALCULATE_FOR_CHECK);
				for(Point p : possibleActions) {
					for(int i = 0; i<2; i++) {
						if(kings[i].getLocation().equals(p) && board[y][x].getSide() != i) {
							return i;
						}
					}
				}				
			}
		}
		return -1;		
	}

//	private boolean checkCheckMate() {//Whether the king can escape somewhere else while being in check.
//		for (int i = 0; i < 2; i++) {
//			ArrayList<Point> KingsPossibleMoves = calculatePMoves(kings[i].getLocation().x, kings[i].getLocation().y);
//			for (int y = 0; y < board.length; y++) {
//				for (int x = 0; x < board[y].length; x++) {
//					if (board[y][x] == null || board[y][x].getSide() == i)
//						continue;
//					ArrayList<Point> threat = calculatePMoves(x, y);
//					KingsPossibleMoves.removeIf(p -> threat.contains(p));
//					if (KingsPossibleMoves.size() == 0) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
	
	private boolean lookForCheckMate() {
//		for(int i = 0; i<)
		
		return true;
	}
	
	
	public Piece[][] getBoard() {
		return board;
	}
	
	public int getTurn() {
		return turn;
	}
	
	public int getSide() {
		return side;
	}
	
	public String getLastMove() {
		String temp = lastMove;
		lastMove = ChessClient.EMPTY;
		return temp;
	}

	public String getTileLocationLabel(boolean horizontal) {
		if(side == Piece.WHITE) {
			return horizontal ? HORIZONTAL : VERTICAL;
		}else {
			return new StringBuilder(horizontal ? HORIZONTAL : VERTICAL).reverse().toString();
		}
	}
	
	public void printBoard() {
		System.out.println("castling:" + castling);
		System.out.println();
		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[y].length; x++) {
				System.out.print((board[y][x] == null ? "[   ]" : "[" + board[y][x].getLocation().x + ","+board[y][x].getLocation().y + "]") + " ");
//				System.out.print((board[y][x] == null ? "[ ]" : "[" + board[y][x].getSide() + "]") + " ");
//				System.out.print((board[y][x] == null ? "[ ]" : "[" + board[y][x].getNameShort() + "]") + " ");

			}
			System.out.println();
		}
		System.out.println();
	}
	
}
