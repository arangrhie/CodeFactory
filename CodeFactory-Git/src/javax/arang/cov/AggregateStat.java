/**
 * 
 */
package javax.arang.cov;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class AggregateStat extends INOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.INOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar covAggregateStat.jar <in1.cov> <in2.cov> ... <inN.cov>");
		System.out.println("\t<in.cov>: .cov file generated with bamBaseDepth.jar and baseCoverage.jar or covReplaceCoverage.jar");
		System.out.println("\tcoverage_summary.stat: output file. Output format is as following:");
		System.out.println("\t\t\tin1(sample1)\tin2(sample2)\t...");
		System.out.println("\t\tCovered Min\t...");
		System.out.println("\t\tCovered Q1\t...");
		System.out.println("\t\tCovered Q2 (Median)\t...");
		System.out.println("\t\tCovered Q3\t...");
		System.out.println("\t\tCovered Max\t...");
		System.out.println("\t\tCovered Mean\t...");
		System.out.println("\t\tTarget Min\t...");
		System.out.println("\t\tTarget Q1\t...");
		System.out.println("\t\tTarget Q2 (Median)\t...");
		System.out.println("\t\tTarget Q3\t...");
		System.out.println("\t\tTarget Max\t...");
		System.out.println("\t\tTarget Mean\t...");
		System.out.println("\t\tTotal depth\t...");
		System.out.println("\t\tTotal covered bases\t...");
		System.out.println("\t\tTotal target\t...");
		System.out.println("\t\tTotal covered bases coverage\t...");
		System.out.println("\t\tTotal target coverage\t...");
		System.out.println("Arang Rhie, 2014-03-14. arrhie@gmail.com");
	}

	public static final short MIN = 2;
	public static final short Q1 = 4;
	public static final short Q2 = 6;
	public static final short Q3 = 8;
	public static final short MAX = 10;
	public static final short MEAN = 12;
	public static final short TOTAL_DEPTH = 2;
	public static final short TOTAL_COVERED = 4;
	public static final short TOTAL_TARGET = 6;
	public static final short TOTAL_COVERED_BASES_COV = 8;
	public static final short TOTAL_TARGET_COV = 10;
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.INOwrapper#hooker(java.util.ArrayList, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		
		Vector<String> coveredMin = new Vector<String>();
		Vector<String> coveredQ1 = new Vector<String>();
		Vector<String> coveredQ2 = new Vector<String>();
		Vector<String> coveredQ3 = new Vector<String>();
		Vector<String> coveredMax = new Vector<String>();
		Vector<String> coveredMean = new Vector<String>();
		
		Vector<String> targetMin = new Vector<String>();
		Vector<String> targetQ1 = new Vector<String>();
		Vector<String> targetQ2 = new Vector<String>();
		Vector<String> targetQ3 = new Vector<String>();
		Vector<String> targetMax = new Vector<String>();
		Vector<String> targetMean = new Vector<String>();
		
		Vector<String> totalDepth = new Vector<String>();
		Vector<String> totalCovered = new Vector<String>();
		Vector<String> totalTarget = new Vector<String>();
		Vector<String> totalCoveredCov = new Vector<String>();
		Vector<String> totalTargetCov = new Vector<String>();
		
		String line;
		String[] tokens;
		
		fm.write("Coverage");
		for (FileReader fr : frs) {
			// write the file names (sample names)
			fm.write("\t" + fr.getFileName().substring(0, fr.getFileName().indexOf(".")));
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split("\t");
				if (tokens.length > MAX) {
					// save statistics
					coveredMin.add(tokens[MIN]);
					coveredQ1.add(tokens[Q1]);
					coveredQ2.add(tokens[Q2]);
					coveredQ3.add(tokens[Q3]);
					coveredMax.add(tokens[MAX]);
					coveredMean.add(tokens[MEAN]);
					
					line = fr.readLine();
					tokens = line.split("\t");
					targetMin.add(tokens[MIN]);
					targetQ1.add(tokens[Q1]);
					targetQ2.add(tokens[Q2]);
					targetQ3.add(tokens[Q3]);
					targetMax.add(tokens[MAX]);
					targetMean.add(tokens[MEAN]);
					
					line = fr.readLine();
					tokens = line.split("\t");
					totalDepth.add(tokens[TOTAL_DEPTH]);
					totalCovered.add(tokens[TOTAL_COVERED]);
					totalTarget.add(tokens[TOTAL_TARGET]);
					totalCoveredCov.add(tokens[TOTAL_COVERED_BASES_COV]);
					totalTargetCov.add(tokens[TOTAL_TARGET_COV]);
					break;
				}
			}
		}
		
		fm.writeLine();
		
		// write out in format
		writeStatValues(fm, "Covered Min", coveredMin);
		writeStatValues(fm, "Covered Q1", coveredQ1);
		writeStatValues(fm, "Covered Q2 (Median)", coveredQ2);
		writeStatValues(fm, "Covered Q3", coveredQ3);
		writeStatValues(fm, "Covered Max", coveredMax);
		writeStatValues(fm, "Covered Mean", coveredMean);
		
		writeStatValues(fm, "Target Min", targetMin);
		writeStatValues(fm, "Target Q1", targetQ1);
		writeStatValues(fm, "Target Q2 (Median)", targetQ2);
		writeStatValues(fm, "Target Q3", targetQ3);
		writeStatValues(fm, "Target Max", targetMax);
		writeStatValues(fm, "Target Mean", targetMean);
		
		writeStatValues(fm, "Total Depth", totalDepth);
		writeStatValues(fm, "Total Covered Bases", totalCovered);
		writeStatValues(fm, "Total Target Bases", totalTarget);
		writeStatValues(fm, "Total Coverd Bases Coverage", totalCoveredCov);
		writeStatValues(fm, "Total Target Coverage", totalTargetCov);

	}
	
	private void writeStatValues(FileMaker fm, String category, Vector<String> values) {
		fm.write(category);
		for (String token : values) {
			fm.write("\t" + token);
		}
		fm.writeLine();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 1) {
			new AggregateStat().go(args, "coverage_summary.stat");
		} else {
			new AggregateStat().printHelp();
		}
	}

}
