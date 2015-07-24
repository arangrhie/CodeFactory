package javax.arang.genome.coverage;

import java.util.StringTokenizer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class CoveragePerDepth extends Rwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CoveragePerDepth covPerX = new CoveragePerDepth();
		if (args.length == 1) {
			new CoveragePerDepth().go(args[0]);
		} else if (args.length == 2) {
			OFFSET = Integer.parseInt(args[1]);
			new CoveragePerDepth().go(args[0]);
		} else if (args.length == 3) {
			OFFSET = Integer.parseInt(args[1]);
			MAX_DEPTH = Integer.parseInt(args[2]);
			new CoveragePerDepth().go(args[0]);
		} else {
			covPerX.printHelp();
		}
	}
	
	private static int MAX_DEPTH = 100;
	private static int OFFSET = 5;

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		StringTokenizer st;
		
		int numOffs = MAX_DEPTH/OFFSET + 1;
		int[] coverageArr = new int[numOffs];
		int[] depthArr = new int[numOffs];
		
		long avgDepth = 0;
		long avgCoverage = 0;
		long range = 0;
		
		boolean isFirst = true;
		int rangeStart = 0;
		int rangeEnd = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			if (isFirst) {
				rangeStart = Integer.parseInt(st.nextToken());	// pos
				isFirst = false;
			} else {
				rangeEnd = Integer.parseInt(st.nextToken());	// pos
			}
			int depth = Integer.parseInt(st.nextToken());	// depth
			int j = numOffs - 1;
			for (int i = MAX_DEPTH; i >= 0 && j >= 0; i -= OFFSET) {
				if (depth > i) {
					coverageArr[j]++;
					depthArr[j] += depth;
					break;
				}
				j--;
			}
			
			if (depth > 0) {
				avgCoverage++;
				avgDepth += depth;
				range++;
			}
		}
		
		int validSeqRange = rangeEnd - rangeStart + 1;
		
		for (int m = 0; m < numOffs; m++) {
			int covered = 0;
			int depthed = 0;
			for (int j = m; j < numOffs; j++) {
				covered += coverageArr[j];
				depthed += depthArr[j];
			}
			System.out.println((m * OFFSET) + "\t" + String.format("%,.2f", (100*(float) covered) / validSeqRange) + "\t"
											+ String.format("%,.2f", ((float) depthed / validSeqRange)));
		}
		
		String sampleName = fr.getFileName().substring(0, fr.getFileName().indexOf("_"));
		
		System.out.println("Valid Positions\t" + range);
		System.out.println("Valid Sequencing Range\t" + validSeqRange);
		System.out.println("Total # of bases\t" + sampleName + "\t" + avgDepth);
		
		
		float avgCovPerRange = (100 * (float) avgCoverage) / validSeqRange;
		float avgDepthOfCoverage = (float) avgDepth / validSeqRange;
		
		System.out.println("Average Coverage\t" + String.format("%,.2f", avgCovPerRange) + "%\t"
				+ String.format("%,.2f", avgDepthOfCoverage));

	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar depthPerCov.jar <inFile> [OFFSET MAX_DEPTH]");
		System.out.println("\tInput file: base sort result in the following format:");
		System.out.println("\t56609660\t90");
		System.out.println("\t[OFFSET]: offset(간격) of the coverage");
		System.out.println("\t[MAX_DEPTH]: maximum sequencing depth");
	}

}
