/**
 * 
 */
package javax.arang.gatk;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class AggregateStat extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gatkAggregateStat.jar *.targetCoverage.out.sample_summary");
		System.out.println("\t<input>: <sample>.bam.targetCovergae.out.sample_summary, generated with GATK");
		System.out.println("\t<output>: total_target_coverage.txt");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		boolean hasHeader = false;
		for (FileReader fr : frs) {
			String line = fr.readLine();
			if (!hasHeader) {
				fm.writeLine(line);
				hasHeader = true;
			}
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("Total")) {
					break;
				}
				fm.writeLine(line);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			new AggregateStat().printHelp();
		} else {
			new AggregateStat().go(args, "total_target_coverage.txt");
		}
	}

}
