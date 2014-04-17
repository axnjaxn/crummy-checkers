import java.util.ArrayList;
import java.util.Random;

public class CheckersAI {
	private static Random random = new Random();

	public static int getScore(GameState state) {
		int score = 0;
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++)
				if (state.getPlayer(r, c) == 1) score++;
				else if (state.getPlayer(r, c) == 2) score--;
		return score;
	}

	public static GameState.Move getBestMove(GameState state/*, int lookahead*/) {
		//Generate all legal moves that player one can make
		ArrayList<GameState.Move> moves = new ArrayList<GameState.Move>();
		for (int r = 0; r < 8; r++)
			for (int c = 0; c < 8; c++) {
				if (state.getPlayer(r, c) != 1) continue;
				
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

				//Test four jumps
				move = new GameState.Move(from, new GameState.Location(from.row - 2, from.col + 2));
				if (state.isLegal(move)) moves.add(move);

				move = new GameState.Move(from, new GameState.Location(from.row - 2, from.col - 2));
				if (state.isLegal(move)) moves.add(move);

				move = new GameState.Move(from, new GameState.Location(from.row + 2, from.col + 2));
				if (state.isLegal(move)) moves.add(move);

				move = new GameState.Move(from, new GameState.Location(from.row + 2, from.col - 2));
				if (state.isLegal(move)) moves.add(move);
			}

		//Now it's time to score them.
		ArrayList<GameState.Move> bestMoves = new ArrayList<GameState.Move>();
		int bscore = -99999;
		for (int i = 0; i < moves.size(); i++)
			try {
				GameState next = new GameState(state, moves.get(i));
				int score = getScore(next);

				if (score == bscore) {
					bestMoves.add(moves.get(i));
				}
				else if (score > bscore) {
					bestMoves.clear();
					bestMoves.add(moves.get(i));
					bscore = score;
				}
			} catch (GameState.IllegalMoveException e) {continue;}

		if (bestMoves.isEmpty()) return null;//No moves!
		
		//Then return a winner chosen randomly from the best moves
		return bestMoves.get(random.nextInt(bestMoves.size()));
	}

	public static void main(String[] args) {
		GameState state = GameState.initialBoard();
		try {
			for (int i = 0; i < 10; i++) {
				System.out.printf("Move %d\n", i);

				GameState.Move m = CheckersAI.getBestMove(state);
				state = new GameState(state, m);
				System.out.println(state);

				state.flip();
				m = CheckersAI.getBestMove(state);
				state = new GameState(state, m);
				state.flip();
				System.out.println(state);
			}
		} 
		catch (NullPointerException e) {
			System.out.println("Null pointer exception");
		}
		catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
};