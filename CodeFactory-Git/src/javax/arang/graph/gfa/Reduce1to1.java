package javax.arang.graph.gfa;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class Reduce1to1 extends Rwrapper {

	private static final boolean SEARCH_BACKWARD = false;
	private static final boolean SEARCH_FORWARD = true;
	
	@Override
	public void hooker(FileReader fr) {
		ToGraph graph = new ToGraph();
		graph.hooker(fr);
		//ArrayList<Link> links = graph.getLinkList();
		HashMap<String, Segment> segments = graph.getSegMap();
		
		Segment seg;
		
		int inEdges;
		int outEdges;
		int count1to1 = 0;
		int prevOutEdges;
		int postInEdges;
		
		ArrayList<Path> backPath;
		ArrayList<Path> forwardPath;
		Path prevPath;
		Path postPath;
		
		for (String segName : segments.keySet()) {
			seg = segments.get(segName);
			inEdges = seg.getInPathsSize();
			outEdges = seg.getOutPathsSize();
			
			if (inEdges == 1 && outEdges == 1 && !seg.isCirculare() && !seg.isMarkedVisited()) {
				prevPath = seg.getInPaths().iterator().next();
				prevOutEdges = prevPath.segment.getEdges(prevPath.isForward(), SEARCH_FORWARD);
				postPath = seg.getOutPaths().iterator().next();
				postInEdges = postPath.segment.getEdges(postPath.isForward(), SEARCH_BACKWARD);
				
				if (prevOutEdges == postInEdges && prevOutEdges == 1) {
//					System.err.println(prevPath.getName() 
//							+ "\t" + seg.getName() + " (" + String.format("%,.0f", seg.getSize()) + ")"
//							+ "\t" + postPath.getName());
					if (!seg.isMarkedVisited()) {
						backPath = traverse1to1(prevPath, SEARCH_BACKWARD);
						forwardPath = traverse1to1(postPath, SEARCH_FORWARD);
						if (backPath.size() > 0 && forwardPath.size() > 0) {
							printPaths(backPath, seg, forwardPath);
							count1to1++;
						}
					}
				}
			}
		}
		
		System.err.println("[DEBUG] :: Num. reduceable 1-to-1 segments: " + count1to1);
		
		//Summary.printSummary(segments);
		
	}
	
	private void printPaths(ArrayList<Path> backPath, Segment seg, ArrayList<Path> forwardPath) {
		int numEdges;
		Path path;
		double estimatedSum = 0;
		for (int i = backPath.size() - 1; i >= 0; i--) {
			path = backPath.get(i);
			numEdges = path.segment.getEdges(path.isForward(), SEARCH_BACKWARD);
			System.out.print("(" + numEdges + ")" + backPath.get(i).getName() + " : " + backPath.get(i).segment.getPrintableSize());
			numEdges = path.segment.getEdges(path.isForward(), SEARCH_FORWARD);
			System.out.print("(" + numEdges + ")" + "\t");
			if (numEdges == 1) {
				estimatedSum += backPath.get(i).segment.getSize();
			}
		}
		System.out.print("(1)" + seg.getName() + "+ : " + seg.getPrintableSize() + "(1)");
		estimatedSum += seg.getSize();
		for (int i = 0; i < forwardPath.size(); i++) {
			path = forwardPath.get(i);
			numEdges = path.segment.getEdges(path.isForward(), SEARCH_BACKWARD);
			if (numEdges == 1) {
				estimatedSum += forwardPath.get(i).segment.getSize();
			}
			System.out.print("\t" + "(" + numEdges + ")" + forwardPath.get(i).getName() + " : " + forwardPath.get(i).segment.getPrintableSize());
			numEdges = path.segment.getEdges(path.isForward(), SEARCH_FORWARD);
			System.out.print("(" + numEdges + ")");
		}
		System.out.println("\t|\t" + String.format("%,.0f", estimatedSum));
	}
	
	/***
	 * traverse the graph forward or backward
	 * until no 1 in/out edges are observed
	 * @param seg
	 * @param dir
	 * @param isForwardSearch
	 * @return
	 */
	public ArrayList<Path> traverse1to1(Path path, boolean isForwardSearch) {
		ArrayList<Path> paths = new ArrayList<Path>();
		if (path.segment.isCirculare()) {
			return paths;
		}
		paths.add(path);
		if (path.segment.isMarkedVisited()) {
			path.segment.setCircular();
			return paths;
		}
		path.segment.markVisited();
		
		if (path.segment.getOutPathsSize() == 1 && path.segment.getInPathsSize() == 1) {
			Path nextPath;
			ArrayList<Path> subPaths = new ArrayList<Path>();
			if (isForwardSearch) {
				if (path.isForward()) {
					nextPath = path.segment.getOutPaths().iterator().next();
					subPaths = traverse1to1(nextPath, isForwardSearch);
				} else if (!path.isForward()) {
					nextPath = path.segment.getInPaths().iterator().next();
					nextPath.flip();
					subPaths = traverse1to1(nextPath, isForwardSearch);
				}
			} else {
				// isBackwardSearch
				if (path.isForward()) {
					nextPath = path.segment.getInPaths().iterator().next();
					subPaths = traverse1to1(nextPath, isForwardSearch);
				} else if (!path.isForward()) {
					nextPath = path.segment.getOutPaths().iterator().next();
					nextPath.flip();
					subPaths = traverse1to1(nextPath, isForwardSearch);
				}
			}
			paths.addAll(subPaths);
		}
		
		return paths;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gfaReduce1to1.jar <in.gfa>");
		System.out.println("\t<stdout>: <abstract_segs>\t<[segment][direction]>");
		System.out.println("Arang Rhie, 2017-09-13. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Reduce1to1().go(args[0]);
		} else {
			new Reduce1to1().printHelp();
		}
	}

}
