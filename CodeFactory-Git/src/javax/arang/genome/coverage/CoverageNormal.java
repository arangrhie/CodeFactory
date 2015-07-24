package javax.arang.genome.coverage;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CoverageNormal extends IOwrapper {

	static int totalBaseNum = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		HashMap<Integer, Integer> covMap = new HashMap<Integer, Integer>();
		String line;
		String[] tokens;
		int minPos = Integer.MAX_VALUE;
		int maxPos = -1;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[1].equals("0"))	continue;
			int pos = Integer.parseInt(tokens[0]);
			int depth = Integer.parseInt(tokens[1]);
			if (minPos > pos) {
				minPos = pos;
			}
			if (maxPos < pos) {
				maxPos = pos;
			}
			covMap.put(pos, depth);
			totalBaseNum += depth;
			
		}
		
		fm.writeLine("# Total number of bases:\t" + totalBaseNum);
		for (int i = minPos; i <= maxPos; i++) {
			if (covMap.containsKey(i)) {
				fm.writeLine(i + "\t" + String.format("%,.3f", ((float)covMap.get(i) * 1000)/totalBaseNum));
			} else {
				fm.writeLine(i + "\t" + String.format("%,.3f", 0f));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar coverageNormal.jar <in.cov> [total_base_num]"); 
		System.out.println("\t<out.cov.nor>: Noramlize sequencing depth by total bases aligned.");
		System.out.println("\t\t<position>\t<normalized_depth>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			totalBaseNum = Integer.parseInt(args[1]);
			new CoverageNormal().go(args[0], args[0] + ".norm");
		} else if (args.length == 1) {
			new CoverageNormal().go(args[0], args[0] + ".norm");
		} else {
			new CoverageNormal().printHelp();
		}

	}

}
