import java.util.ArrayList;
import java.util.Random;

public class CheckersAI {
	public class NoMovesLeftException extends Exception {
		private static final long serialVersionUID = -1782064012078589916L;

		public String getMessage() {
			return "No moves left";
		}
	}
	
	private static CheckersAI instance = null;
	private Random random = new Random();
	
	private CheckersAI() { }
	
	public static CheckersAI getInstance() {
		if (instance == null) instance = new CheckersAI();
		return instance;
	}

	public int getScore(GameState state) {
		int no = 0, nO = 0, nt = 0, nT = 0;
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				if (state.get(r, c) == 'o') no++;
				else if (state.get(r, c) == 'O') nO++;
				else if (state.get(r, c) == 't') nt++;
				else if (state.get(r, c) == 'T') nT++;
		if (no + nO == 0) return -999999;
		else if (nt + nT == 0) return 999999;
		else return no + 2 * nO - nt - 2 * nT;
	}
	
	public ArrayList<GameState.Move> getSimpleMovesAt(GameState state, int r, int c) {
		ArrayList<GameState.Move> moves = new ArrayList<GameState.Move>();
		GameState.Location from = new GameState.Location(r, c);
		GameState.Move move;
		
		//Test four basic moves
		move = new GameState.Move(from, new GameState.Location(from.row - 1, from.col + 1));
		if (state.isLegal(move)) moves.add(move);

		move = new GameState.Move(from, new GameState.Location(from.row - 1, from.col - 1));
		if (state.isLegal(move)) moves.add(move);

		move = new GameState.Move(from, new GameState.Location(from.row + 1, from.col + 1));
		if (state.isLegal(move)) moves.add(move);

		move = new GameState.Move(from, new GameState.Location(from.row + 1, from.col - 1));
		if (state.isLegal(move)) moves.add(move);
		
		return moves;
	}
	
	public ArrayList<GameState.Move> getJumpsAt(GameState state, int r, int c) {
		ArrayList<GameState.Move> moves = new ArrayList<GameState.Move>();
		ArrayList<GameState.Move> moreJumps;
		GameState.Location from = new GameState.Location(r, c);
		GameState.Move move;
		
		//Test four jumps
		move = new GameState.Move(from, new GameState.Location(from.row - 2, from.col + 2));
		if (state.isLegal(move)) {
			moves.add(move);
			moreJumps = getJumpsAt(new GameState(state, move), move.to().row, move.to().col);
			for (int i = 0; i < moreJumps.size(); i++)
				moves.add(new GameState.Move(from, moreJumps.get(i)));
		}

		move = new GameState.Move(from, new GameState.Location(from.row - 2, from.col - 2));
		if (state.isLegal(move)) {
			moves.add(move);
			moreJumps = getJumpsAt(new GameState(state, move), move.to().row, move.to().col);
			for (int i = 0; i < moreJumps.size(); i++)
				moves.add(new GameState.Move(from, moreJumps.get(i)));
		}

		move = new GameState.Move(from, new GameState.Location(from.row + 2, from.col + 2));
		if (state.isLegal(move)) {
			moves.add(move);
			moreJumps = getJumpsAt(new GameState(state, move), move.to().row, move.to().col);
			for (int i = 0; i < moreJumps.size(); i++)
				moves.add(new GameState.Move(from, moreJumps.get(i)));
		}

		move = new GameState.Move(from, new GameState.Location(from.row + 2, from.col - 2));
		if (state.isLegal(move)) {
			moves.add(move);
			moreJumps = getJumpsAt(new GameState(state, move), move.to().row, move.to().col);
			for (int i = 0; i < moreJumps.size(); i++)
				moves.add(new GameState.Move(from, moreJumps.get(i)));
		}
		
		return moves;
	}

	public GameState.Move getBestMove(GameState state, int lookahead) throws NoMovesLeftException {
		//Generate all legal moves that player one can make
		ArrayList<GameState.Move> moves = new ArrayList<GameState.Move>();
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++) {
				if (state.getPlayer(r, c) != 1) continue;
				moves.addAll(getSimpleMovesAt(state, r, c));
				moves.addAll(getJumpsAt(state, r, c));				
			}

		//Now it's time to score them.
		ArrayList<GameState.Move> bestMoves = new ArrayList<GameState.Move>();
		int bscore = -999;
		for (int i = 0; i < moves.size(); i++)
			try {
				GameState next = new GameState(state, moves.get(i));
				int score;
				if (lookahead == 0) score = getScore(next);
				else {
					//uf
					next.flip();
					GameState.Move min = getBestMove(next, lookahead - 1);
					next.applyMove(min);
					next.flip();
					score = getScore(next);
				}

				if (score == bscore) {
					bestMoves.add(moves.get(i));
				}
				else if (score > bscore) {
					bestMoves.clear();
					bestMoves.add(moves.get(i));
					bscore = score;
				}
			} catch (GameState.IllegalMoveException e) {continue;}

		if (bestMoves.isEmpty()) throw new NoMovesLeftException();
		
		//Then return a winner chosen randomly from the best moves
		return bestMoves.get(random.nextInt(bestMoves.size()));
	}
};