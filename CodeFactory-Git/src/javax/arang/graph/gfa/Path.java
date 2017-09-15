package javax.arang.graph.gfa;

import java.util.ArrayList;

public class Path {
	protected Segment segment = null;
	protected Character direction = null;	// forward+ or reverse-
	
	Path(Segment seg, char dir) {
		this.segment = seg;
		this.direction = dir;
	}
	
	public String getName() {
		return segment.getName() + direction;
	}
	
	public boolean isForward() {
		if (direction == '+') {
			return true;
		}
		return false;
	}

	public static ArrayList<Path> flipDirection(ArrayList<Path> subPaths) {
		for (int i = 0; i < subPaths.size(); i++) {
			subPaths.get(i).flip();
		}
		return subPaths;
	}
	
	public void flip() {
		direction = GFA.switchDirection(direction);
	}
	
	
}
