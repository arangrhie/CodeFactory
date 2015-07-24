/**
 * 
 */
package javax.arang.cov;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.base.Coverage;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class ReplaceCoverage extends I2Owrapper {

	
	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		long depthSum = 0;
		long coveredBasesSum = 0;
		long targetBasesSum = 0;
		Vector<Float> cov1 = new Vector<Float>();
		Vector<Float> cov2 = new Vector<Float>();
		
		HashMap<String, String> toReplaceMap = new HashMap<String, String>();
		String line;
		String[] tokens;
		
		// Write the header line
		Coverage.writeHeader(fm);
		fr1.readLine();
		fr2.readLine();
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			if (tokens.length > 7)	break;
			toReplaceMap.put(tokens[Bed.CHROM] + " " + tokens[Bed.START] + " " + tokens[Bed.END], line);
		}
		
		String key = "";
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			if (tokens.length > 7) {
				Coverage.writeBasicStat(fm, "MeanDepth (DepthSum / CoveredBases)", cov1);
				Coverage.writeBasicStat(fm, "MeanDepth (DepthSum / (End - Start)", cov2);
				Coverage.writeTotalStat(fm, depthSum, coveredBasesSum, targetBasesSum);
				break;
			}
			key = tokens[Bed.CHROM] + " " + tokens[Bed.START] + " " + tokens[Bed.END];
			if (toReplaceMap.containsKey(key)) {
				fm.writeLine(toReplaceMap.get(key));
				tokens = toReplaceMap.get(key).split("\t");
			} else {
				fm.writeLine(line);
			}
			Coverage.addCoverage(cov1, Long.parseLong(tokens[Coverage.DEPTH_SUM]),
					Float.parseFloat(tokens[Coverage.COVERED_BASES]));
			Coverage.addCoverage(cov2, Long.parseLong(tokens[Coverage.DEPTH_SUM]),
					Long.parseLong(tokens[Coverage.END]) - Long.parseLong(tokens[Coverage.START]));
			depthSum += Long.parseLong(tokens[Coverage.DEPTH_SUM]);
			coveredBasesSum += Long.parseLong(tokens[Coverage.COVERED_BASES]);
			targetBasesSum += Long.parseLong(tokens[Coverage.END]) - Long.parseLong(tokens[Coverage.START]);
		}
		System.out.println("Finished!");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar covReplaceCoverage.jar <old.cov> <to_replace.cov> <new.cov>");
		System.out.println("\t<old.cov>: original .cov file to be replaced.");
		System.out.println("\t<to_replace.cov>: .cov file containing new coverage data.");
		System.out.println("\t\tChr\tStart\tEnd must match the <old.cov>. The rest will be replaced.");
		System.out.println("\t\tThis file will be on memory mapped. Try to keep this file small as possible.");
		System.out.println("\t<new.cov>: new .cov file with replaced values. ");
		System.out.println("Arang Rhie, 2014-03-14. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new ReplaceCoverage().go(args[0], args[1], args[2]);
		} else {
			new ReplaceCoverage().printHelp();
		}

	}

}
