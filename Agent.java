import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.*;
import java.lang.Math;

class GameState {
	public int[] state;
	public double cost;
	public double actionCost;
	public double heuristic;
	public GameState parent;

	GameState(double _actionCost, int x, int y) {
		cost = Double.MAX_VALUE;
		actionCost = _actionCost;
		heuristic = 0;
		state = new int[2];
		state[0] = x;
		state[1] = y;
	}

	GameState[] getChildStates(Model model) {
		int maxX = (int) Model.XMAX / 10;
		int maxY = (int) Model.YMAX / 10;

		GameState[] possibleStates = new GameState[8];
		possibleStates[0] = (state[0] + 1 <= maxX && state[1] - 1 >= 0) ? new GameState(
				Math.sqrt(2) / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] + 1, state[1] - 1) : null;
		possibleStates[1] = (state[0] + 1 <= maxX)
				? new GameState(1 / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] + 1, state[1])
				: null;
		possibleStates[2] = (state[0] + 1 <= maxX && state[1] + 1 <= maxY) ? new GameState(
				Math.sqrt(2) / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] + 1, state[1] + 1) : null;
		possibleStates[3] = (state[1] + 1 <= maxY)
				? new GameState(1 / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0], state[1] + 1)
				: null;
		possibleStates[4] = (state[1] - 1 >= 0)
				? new GameState(1 / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0], state[1] - 1)
				: null;
		possibleStates[5] = (state[0] - 1 >= 0 && state[1] - 1 >= 0) ? new GameState(
				Math.sqrt(2) / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] - 1, state[1] - 1) : null;
		possibleStates[6] = (state[0] - 1 >= 0)
				? new GameState(1 / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] - 1, state[1])
				: null;
		possibleStates[7] = (state[0] - 1 >= 0 && state[1] + 1 <= maxY) ? new GameState(
				Math.sqrt(2) / model.getTravelSpeed(state[0] * 10, state[1] * 10), state[0] - 1, state[1] + 1) : null;
		return possibleStates;
	}

	boolean isEqual(GameState b) {
		if (state[0] == b.state[0] && state[1] == b.state[1])
			return true;
		return false;
	}
}

class Agent {
	float xDistination = 100;
	float yDistination = 100;
	float maxSpeed = 0;

	static boolean astar = false;

	LinkedList<GameState> path;
	PriorityQueue<GameState> queue;

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		for (int i = 0; i < path.size() - 1; i++) {
			g.drawLine((int) path.get(i).state[0] * 10, (int) path.get(i).state[1] * 10,
					(int) path.get(i + 1).state[0] * 10, (int) path.get(i + 1).state[1] * 10);
		}
		g.setColor(Color.orange);
		while (!queue.isEmpty()) {
			GameState current = queue.remove();
			g.fillOval((int) current.state[0] * 10, (int) current.state[1] * 10, 10, 10);
		}
	}

	void update(Model m) {
		path = new LinkedList<GameState>();
		queue = new PriorityQueue<GameState>((a, b) -> Double.compare(a.cost + a.heuristic, b.cost + b.heuristic));
		GameState start;
		GameState goal;
		Controller c = m.getController();
		if (maxSpeed == 0)
			maxSpeed = getHighestSpeed(m);
		while (true) {
			MouseEvent e = c.nextMouseEvent();
			if (e == null) {
				break;
			}
			// Left Click -- UCS
			if (e.getButton() == MouseEvent.BUTTON1) {
				xDistination = e.getX();
				yDistination = e.getY();
				astar = false;
			}
			// Right Click -- A*
			if (e.getButton() == MouseEvent.BUTTON3) {
				xDistination = e.getX();
				yDistination = e.getY();
				astar = true;
			}

		}
		start = new GameState(0, (int) m.getX() / 10, (int) m.getY() / 10);
		goal = new GameState(0, (int) xDistination / 10, (int) yDistination / 10);
		GameState result = UCS(start, goal, m, astar);
		setPath(result);
		if (path.size() > 0) {
			GameState step = path.getLast();
			m.setDestination(step.state[0] * 10, step.state[1] * 10);
		}
	}

	void setPath(GameState result) {
		while (result.parent != null) {
			path.add(result);
			result = result.parent;
		}
	}

	float getHighestSpeed(Model m) {
		float maxSpeed = 0;
		for (int i = 0; i < ((int) Model.XMAX / 10) + 1; i++) {
			for (int j = 0; j < ((int) Model.YMAX / 10) + 1; j++) {
				float speed = m.getTravelSpeed(i * 10, j * 10);
				if (maxSpeed < speed)
					maxSpeed = speed;
			}
		}
		return maxSpeed;
	}

	GameState UCS(GameState start, GameState end, Model m, boolean heuristic) {
		boolean[][] explored = new boolean[((int) Model.XMAX / 10) + 1][((int) Model.YMAX / 10) + 1];
		start.cost = 0.0;
		start.parent = null;

		queue.add(start);
		explored[start.state[0]][start.state[1]] = true;

		while (!queue.isEmpty()) {
			GameState current = queue.poll();
			if (current.isEqual(end))
				return current;
			for (GameState child : current.getChildStates(m)) {
				if (child == null)
					break;
				if (heuristic)
					child.heuristic = Math.sqrt(
							Math.pow((child.state[0] - end.state[0]), 2) + Math.pow((child.state[1] - end.state[1]), 2))
							/ maxSpeed;
				child.cost = current.cost + child.actionCost;
				child.parent = current;
				if (!explored[child.state[0]][child.state[1]]) {
					queue.add(child);
					explored[child.state[0]][child.state[1]] = true;
				} else {
					Iterator<GameState> itr = queue.iterator();
					while (itr.hasNext()) {
						GameState tmp = itr.next();
						if (tmp.isEqual(child) && tmp.cost > child.cost) {
							if (queue.remove(tmp))
								queue.add(child);
							break;
						}
					}
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		Controller.playGame();
	}
}