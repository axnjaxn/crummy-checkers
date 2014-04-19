//package com.frillingJackson.checkersCheat;

/*
 * Piece encoding:
 * O (red)   king for the dark pieces 
 * o (black) pawn for the dark pieces 
 * T (green) king for light piece
 * t (tan)   pawn for light pieces
 * E (white) empty space on board
 * X (black) unreachable space
 */

public class GameState {
	public class IllegalMoveException extends Exception {
		private static final long serialVersionUID = 1959781460558884384L;
		private Move move;

		IllegalMoveException(Move move) {this.move = move;}

		public String getMessage() {
			return "Illegal move: " + move;
		}
		public Move getMove() {return move;}
	}

	public static class Location {
		public int row;
		public int col;

		public Location(int row, int col) {this.row = row; this.col = col;}
		public String toString() {return "[" + row + ", " + col + "]";}
	}

	public static class Move {
		public Location location;
		public Move next;

		public Location from() {return location;}
		public Location to() {return next.location;}
		
		public Move(Location location, Move next) {this.location = location; this.next = next;}
		public Move(Location from, Location to) {this(from, new Move(to, (Move)null));}
		public String toString() {return from() + " - " + to();}
	}

	private char[][] board = new char[8][8];

	public GameState() {
		this("EXEXEXEX"
				+ "XEXEXEXE"
				+ "EXEXEXEX"
				+ "XEXEXEXE"
				+ "EXEXEXEX"
				+ "XEXEXEXE"
				+ "EXEXEXEX"
				+ "XEXEXEXE");
	}

	public GameState(GameState prev, Move m) throws IllegalMoveException {
		this(prev.toString());
		applyMove(m);
	}

	public boolean isLegal(Move m) {
		Location from = m.from();
		Location to = m.to();
		
		//If either move is off the board, it's illegal
		if (!isOnBoard(to.row, to.col)
				|| !isOnBoard(from.row, from.col)) return false;

		//Or if it's moving from a non-checker square or onto an occupied square
		if (get(from.row, from.col) == 'E'
				|| get(from.row, from.col) == 'X'
				|| get(to.row, to.col) != 'E') return false;

		//Compute distances and absolute distances
		int dr = to.row - from.row;
		int adr = (dr > 0)? dr : -dr;
		int dc = to.col - from.col;
		int adc = (dc > 0)? dc : -dc;

		//Legal moves move diagonally, one or two squares
		if (adr != adc || adr < 1 || adr > 2) return false;

		//Player one pawns can only move up
		if (dr > 0 && get(from.row, from.col) == 'o') return false;

		//Player two pawns can only move down
		if (dr < 0 && get(from.row, from.col) == 't') return false;

		//If jumping...
		if (adr == 2) {			
			Location jumped = new Location(from.row + (dr / adr), from.col + (dc / adc));
			int fromPlayer = getPlayer(from.row, from.col);
			int jumpedPlayer = getPlayer(jumped.row, jumped.col); 

			//Can't jump empty squares, off-color squares, or your own team
			if (jumpedPlayer == 0 || jumpedPlayer == fromPlayer) return false;
		}

		//If all invalid cases are handled, then this must be valid!
		return true;
	}

	public void applyMove(Move m) throws IllegalMoveException {
		if (!isLegal(m)) throw new IllegalMoveException(m);
		Location to = m.to();
		Location from = m.from();

		//Compute distances and absolute distances
		int dr = to.row - from.row;
		int adr = (dr > 0)? dr : -dr;
		int dc = to.col - from.col;
		int adc = (dc > 0)? dc : -dc;

		//Is it a jump?
		if (adr > 1) {
			//Find and set the jumped square to empty
			Location jumped = new Location(from.row + (dr / adr), from.col + (dc / adc));
			set(jumped.row, jumped.col, 'E');
		}

		//Execute basic movement
		if (to.row == 0 && get(from.row, from.col) == 'o') set(to.row, to.col, 'O');
		else if (to.row == 7 && get(from.row, from.col) == 't') set(to.row, to.col, 'T');
		else set(to.row, to.col, get(from.row, from.col));
		
		set(from.row, from.col, 'E');
	}

	public GameState(String state){
		char[] charArray = state.toCharArray();
		int index = 0;
		for (int r = 0; r < 8; r++)
			for(int c = 0; c < 8; c++) {
				while (Character.isWhitespace(charArray[index])) index++;
				set(r, c, charArray[index++]);
			}
	}

	public static GameState initialBoard() {
		return new GameState("tXtXtXtX"
				+ "XtXtXtXt"
				+ "tXtXtXtX"
				+ "XEXEXEXE"
				+ "EXEXEXEX"
				+ "XoXoXoXo"
				+ "oXoXoXoX"
				+ "XoXoXoXo");
	}

	public void rotateCW() {
		char[][] rotated = new char [8][8];
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				rotated[c][7 - r] = board[r][c];
		board = rotated;
	}

	public void rotateCCW() {
		char[][] rotated = new char [8][8];
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				rotated[r][c] = board[c][7 - r];
		board = rotated;
	}

	public void flip() {
		rotateCW();
		rotateCW();
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				if (board[r][c] == 'o') board[r][c] = 't';
				else if (board[r][c] == 'O') board[r][c] = 'T';
				else if (board[r][c] == 't') board[r][c] = 'o';
				else if (board[r][c] == 'T') board[r][c] = 'O';
	}

	public boolean isOnBoard(int row, int col) {
		return (row >= 0 && row < 8 && col >= 0 && col < 8);
	}

	public char get(int row, int col) {return board[row][col];}

	public void set(int row, int col, char piece) {
		if (piece == 'o' || piece == 'O' || piece == 't' || piece == 'T' || piece == 'E')
			board[row][col] = piece;
		else 
			board[row][col] = 'X';
	}

	public int getPlayer(int row, int col) {
		char piece = get(row, col);
		if (piece == 'o' || piece == 'O') return 1;
		else if (piece == 't' || piece == 'T') return 2;
		else return 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++)
				sb.append(board[r][c]);
			sb.append('\n');
		}
		return sb.toString();
	}
}
