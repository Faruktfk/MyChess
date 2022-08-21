package multiplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChessRoom {

	public static final String CLOSE_COMMAND = "close!", DONTWAIT_COMMAND = "continueplzzz!", OPPONENT_LEFT = "OpponentHasLeft";
	private static ArrayList<ChessRoom> chessRooms = new ArrayList<>();

	public static ChessRoom findAvailableChessRoom() {
		if (chessRooms.size() == 0) {
			return new ChessRoom();
		} else {
			return chessRooms.stream().filter(cr -> cr.playerCount < 2).findAny().orElseGet(() -> new ChessRoom());
		}
	}

	public static void broadcastClose() {
		for (ChessRoom cr : chessRooms) {
			ArrayList<Player> toRemovePlayer = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				if (cr.players.size() > i && cr.players.get(i) != null) {
					cr.players.get(i).write(CLOSE_COMMAND);
					toRemovePlayer.add(cr.players.get(i));
				}
			}
			cr.players.removeAll(toRemovePlayer);
		}
	}

	public static int getPlayerCount() {
		int count = 0;
		for (ChessRoom cr : chessRooms) {
			count += cr.players.size();
		}
		return count;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int playerCount = 0;
	private ArrayList<Player> players = new ArrayList<>();

	public ChessRoom() {
		chessRooms.add(this);

	}

	public void addPlayer(Socket client) {
		if (playerCount < 2) {
			players.add(new Player(this, client, players.size() - 1));
			players.get(playerCount).write(playerCount + "");
			playerCount++;
		}
		if (playerCount == 2) {
			for (Player p : players) {
				p.write(DONTWAIT_COMMAND);
			}
		}
	}

	private void removePlayer(Player player) {
		players.remove(player);
		playerCount = players.size();
		for(Player otherPlayer: players) {
			otherPlayer.write(OPPONENT_LEFT);
		}
		if (players.size() == 0) {
			chessRooms.remove(this);
		}
	}

	private void broadCastMove(Player player, String move) {
		players.get(1 - players.indexOf(player)).write(move);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - -

	private class Player {
		ChessRoom chessRoom;
		String name;
		Socket socket;
		BufferedReader bufferedReader;
		BufferedWriter bufferedWriter;
		boolean isConnected;

		public Player(ChessRoom chessRoom, Socket socket, int side) {
			this.chessRoom = chessRoom;
			try {
				this.socket = socket;
				isConnected = true;
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				name = bufferedReader.readLine();
				System.out.println(name + " joined the room!");

				listenIncoming();
			} catch (IOException e) {
				closeEverything();
			}

		}

		private void write(String msg) {
			try {
				bufferedWriter.write(msg);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			} catch (IOException e) {
				closeEverything();
			}
		}

		private void listenIncoming() {
			new Thread(() -> {
				while (isConnected) {
					try {
						String msg = bufferedReader.readLine();
						if (msg == null || msg.equals(CLOSE_COMMAND)) {
							isConnected = false;
							chessRoom.removePlayer(this);
						} else {
							chessRoom.broadCastMove(this, msg);
						}
					} catch (IOException e) {
						closeEverything();
					}
				}
			}).start();
		}

		private void closeEverything() {
			System.out.println(this.name + " is leaving");
			isConnected = false;
			try {
				if (bufferedReader != null)
					bufferedReader.close();
				if (bufferedWriter != null)
					bufferedWriter.close();
				if (socket != null)
					socket.close();
				chessRoom.removePlayer(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
