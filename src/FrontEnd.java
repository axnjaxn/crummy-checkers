import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class FrontEnd extends JFrame {
	private MyPanel gpanel;

	private GameState state = GameState.initialBoard();
	private boolean p1move = true;

	private static final long serialVersionUID = -8480088609842367264L;

	private class MyPanel extends JPanel implements MouseListener {
		private static final long serialVersionUID = -6972157768927514460L;

		private Rectangle rect;
		
		MyPanel() {
			super();
			addMouseListener(this);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D gfx = (Graphics2D)g;
			rect = gfx.getClipBounds();
			gfx.setColor(new Color(255, 255, 255));
			gfx.fillRect(rect.x, rect.y, rect.width, rect.height);
			for (int r = 0; r < 8; r++)
				for (int c = 0; c < 8; c++) {
					if (hasSelected && r == selectLoc.row && c == selectLoc.col) {
						gfx.setColor(new Color(255, 255, 0));
						gfx.fillRect(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
					}
					switch(state.get(r, c)) {
					case 'o':
						gfx.setColor(new Color(255, 0, 0));
						gfx.fillOval(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
						break;
					case 'O':
						gfx.setColor(new Color(128, 0, 0));
						gfx.fillOval(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
						break;
					case 't':
						gfx.setColor(new Color(0, 0, 255));
						gfx.fillOval(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
						break;
					case 'T':
						gfx.setColor(new Color(0, 0, 128));
						gfx.fillOval(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
						break;
					case 'X':
						gfx.setColor(new Color(0, 0, 0));
						gfx.fillRect(rect.x + c * rect.width / 8, rect.y + r * rect.height / 8, rect.width / 8, rect.height / 8);
						break;
					default:
						break;
					}					
				}
		}

		private GameState.Location selectLoc;
		private boolean hasSelected = false;
		
		public void mouseClicked(MouseEvent e) {
			if (!hasSelected) {
				selectLoc = new GameState.Location(8 * e.getY() / rect.height, 8 * e.getX() / rect.width);
				if (p1move && (state.getPlayer(selectLoc.row, selectLoc.col) == 1)
					|| !p1move && (state.getPlayer(selectLoc.row, selectLoc.col) == 2))
					hasSelected = true;
			}
			else {
				hasSelected = false;
				GameState.Location newLoc = new GameState.Location(8 * e.getY() / rect.height, 8 * e.getX() / rect.width);
				GameState.Move move = new GameState.Move(selectLoc, newLoc);
				try {
					state.applyMove(move);
					hasSelected = false;
					state.flip();
					try {
						GameState.Move m = CheckersAI.getBestMove(state, 4);
						state = new GameState(state, m);
					}
					catch (CheckersAI.NoMovesLeftException ex) {
						JOptionPane.showMessageDialog(this, "No moves left!");
					}					
					state.flip();
				} catch (GameState.IllegalMoveException ex) {
					JOptionPane.showMessageDialog(this, "Illegal move.");
				} 
			}
			repaint();
		}

		public void mouseEntered(MouseEvent e) { }

		public void mouseExited(MouseEvent e) { }

		public void mousePressed(MouseEvent e) { }

		public void mouseReleased(MouseEvent e) { }
	}

	public FrontEnd() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);//So that it doesn't just hide the window
		setSize(512, 512);//Change the size of the window (in pixels)

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		gpanel = new MyPanel();
		gpanel.setVisible(false);
		panel.add(gpanel);

		JButton button = new JButton("Make move");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!p1move) state.flip();
				try {								
					GameState.Move m = CheckersAI.getBestMove(state, 4);
					state = new GameState(state, m);				
				} 
				catch (GameState.IllegalMoveException e){

				}
				catch (CheckersAI.NoMovesLeftException e) {

				}
				if (!p1move) state.flip();
				p1move = !p1move;
				gpanel.repaint();
			}
		});
		panel.add(button);

		getContentPane().add(panel);

		gpanel.setVisible(true);
		gpanel.repaint();
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FrontEnd m = new FrontEnd();
				m.setVisible(true);
			}
		});
	}
};
