package javax.arang.genome.bed;

import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.base.Base;

public class CoverageDepth extends I2Owrapper {
	
	final static int X1 = 0;
	final static int X8 = 1;
	final static int X15 = 2;
	final static int X30 = 3;
	
	Vector<Integer> starts = new Vector<Integer>();
	Vector<Integer> ends = new Vector<Integer>();
	Vector<Integer> baitDepth = new Vector<Integer>();
	
	Vector<Float[]> coverage = new Vector<Float[]>();
	Vector<Float[]> depth = new Vector<Float[]>();

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String chr = fr2.getFileName().substring(fr2.getFileName().indexOf("chr"), fr2.getFileName().indexOf(".bed"));
		
		String line = null;
		String[] tokens;
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			if (tokens[2].equals("0"))	continue;
			if (line.equals(""))	continue;
			starts.add(Integer.parseInt(tokens[0]));
			ends.add(Integer.parseInt(tokens[1]));
			baitDepth.add(Integer.parseInt(tokens[2]));
		}
		
		System.out.println(starts.size() + " parsed from bed.cov");
		
		int curIdx = 0;
		boolean hasLine = false;
		
		int[] posCov = new int[4];
		int[] posDepth = new int[4];

		for (int i = 0; i < 4; i++) {
			coverage.add(new Float[starts.size()]);
			depth.add(new Float[starts.size()]);
		}
		
		fm.writeLine("Chr\tStart\tEnd\tRange\tBait depth\t" +
				"Coverage >1x\t>8x\t>15x\t>30x\t" +
				"Mean depth >1x\t>8x\t>15x\t>30x");
		
		READ_LINE : while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			
			if (tokens[Base.CHR].equals(chr)) {
				int pos = Integer.parseInt(tokens[Base.POS]);
				if (ends.get(curIdx) < pos) {
					if (hasLine) {
						hasLine = false;
						// write down
						writeCoverageDepth(chr, curIdx,	posCov, posDepth, fm);
						
						// Initialize
						for (int i = 0; i < 4; i++) {
							posCov[i] = 0;
							posDepth[i] = 0;
						}
						curIdx++;
						if (curIdx == starts.size()) {
							System.out.println("Breaking at " + pos);
							break READ_LINE;
						}
						continue;
					} else {
						while (ends.get(curIdx) < pos) {
							writeCoverageDepth(chr, curIdx,	posCov, posDepth, fm);
							
							// Initialize
							for (int i = 0; i < 4; i++) {
								posCov[i] = 0;
								posDepth[i] = 0;
							}
							curIdx++;
							if (curIdx == starts.size()){
								System.out.println("Breaking at " + pos);
								break READ_LINE;
							}
						}
					}
				}
				if (starts.get(curIdx) <= pos) {
					int d = Integer.parseInt(tokens[Base.TOTAL_COUNT]);

					if (d > 0) {
						posCov[X1]++;
						posDepth[X1] += d;
						if (d > 7) {
							posCov[X8]++;
							posDepth[X8] += d;
							if (d > 14) {
								posCov[X15]++;
								posDepth[X15] += d;
								if (d > 29) {
									posCov[X30]++;
									posDepth[X30] += d;
								}
							}
						}
					}
					
					hasLine = true;
				} 
			} else {
				break;
			}
		}
		
		System.out.println(line);
		
		if (curIdx < starts.size()) {
			for (int i = curIdx; i < starts.size(); i++) {
				writeCoverageDepth(chr, i, posCov, posDepth, fm);
				for (int j = 0; j < 4; j++) {
					posCov[j] = 0;
					posDepth[j] = 0;
				}
			}
		}
		
		fm.writeLine("");
		fm.writeLine("Target\tMean Coverage Depth" + "\t\t" + "Mean coverage" + "\t\t" + "Mean depth");
		String[] covDepthLabel = {"1x", "8x", "15x", "30x"};
		
		for (int i = 0; i < 4; i++) {
			double covSum = 0;
			double depthSum = 0;
			int numBaits = 0;
			for (int j = 0; j < starts.size(); j++) {
				if (String.valueOf(coverage.get(i)[j]).equals("NaN")) {
					continue;
				}
				numBaits++;
				covSum += coverage.get(i)[j];
				depthSum += depth.get(i)[j];
			}
			fm.writeLine(chr + "\t" + covDepthLabel[i]
					+ "\t" + String.format("%,.2f", covSum) + " / " + numBaits + " =\t" + String.format("%,.2f", covSum / numBaits)
					+ "\t" + String.format("%,.2f",depthSum) + " / " + numBaits + " =\t" + String.format("%,.2f", depthSum / numBaits));
		}
	}

	private void writeCoverageDepth(String chr, int curIdx,
			int[] posCov, int[] posDepth,
			FileMaker fm) {
		int start = starts.get(curIdx);
		int end = ends.get(curIdx);
		int range = (end - start + 1);
		
		fm.write(chr + "\t" + start + "\t" + end
				+ "\t" + range
				+ "\t" + baitDepth.get(curIdx));
		
		for (int i = 0; i < 4; i++ ) {
			coverage.get(i)[curIdx] = ((float) posCov[i] * 100) / range;
			fm.write("\t" + String.format("%,.2f", coverage.get(i)[curIdx]));
		}
		
		for (int i = 0; i < 4; i++ ) {
			depth.get(i)[curIdx] = ((float) posDepth[i] / range);
			fm.write("\t" + String.format("%,.2f", depth.get(i)[curIdx]));
		}
		
		fm.writeLine("");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ~/codes/bedCoverageDepth.jar <sorted.bas.chrN> <chrN.bed.cov>");
		System.out.println("\t<sorted.sam.cov>: get mean depth of coverage where bed.cov is > 0");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new CoverageDepth().go(args[0], args[1], args[0] + ".depth");
		} else {
			new CoverageDepth().printHelp();
		}
	}

}
