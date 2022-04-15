package javax.arang.gaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.gaf.Path.PathSortingComparator;
import javax.arang.paf.PAF;

public class ToPathCov extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		Path path;
		boolean readIsPositive = true;	// read orientation
		
		HashMap<String, ReadCov> pathToReadCovMap = new HashMap<String, ReadCov>();
		ArrayList<Path> paths = new ArrayList<Path>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			readIsPositive = true;
			
			//  inNode found in path
			if (tokens[PAF.T_NAME].contains(inNode)) {
				//  break down path to nodes
				path = new Path(tokens[PAF.T_NAME]);
				
				//  to speed up, ignore nodes less than out_min_num_nodes
				if (path.getNumNodes() < minNumNodes) continue;
				
				//  normalize, put the lexicographically smaller node up front
				if (path.normalize()) {
					readIsPositive = false;
				}
				
				//  merge identical paths while adding to paths
				if (pathToReadCovMap.containsKey(path.getPath())) {
					pathToReadCovMap.get(path.getPath()).addRead(tokens[PAF.Q_NAME], readIsPositive, tokens[tokens.length - 1]);
				} else {
					paths.add(path);
					pathToReadCovMap.put(path.getPath(), new ReadCov(tokens[PAF.Q_NAME], readIsPositive, tokens[tokens.length - 1]));
				}
			}
			//  skip other gaf records
		}
		//  sort by longest path
		Collections.sort(paths, new PathSortingComparator());
		
		//  print output
		ReadCov cov;
		for (Path p : paths) {
			cov = pathToReadCovMap.get(p.getPath());
			System.out.println(inNode + "\t" + p.getPath() + "\t" + cov.getCoverage() + "\t" + cov.getIds() + "\t" + cov.getIdys());
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar gafToPathCov.jar in.gaf node_name out_min_num_nodes");
		System.err.println("Extract paths and read coverage including node_name");
		System.err.println("  in.gaf     graph aligner output");
		System.err.println("  node_name  node name to include in output path. STRING");
		System.err.println("  out_min_num_nodes  minimum num. nodes in path to output. OPTIONAL. INT. DEFAULT=0, reports all nodes.");
		System.err.println("  output     path_name  path  cov  +|-rid[,+|-rid]  idy[,idy]");
		System.err.println("Arang Rhie, 2022-02-17");
		System.err.println();
	}

	private static String inNode;
	private static int minNumNodes = 0;
	
	public static void main(String[] args) {
		if (args.length > 2) {
			minNumNodes = Integer.parseInt(args[2]);
		}
		if (args.length >= 2) {
			inNode = args[1];
			new ToPathCov().go(args[0]);
		} else {
			new ToPathCov().printHelp();
		}
	}

}
