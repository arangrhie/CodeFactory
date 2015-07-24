/**
 * 
 */
package javax.arang.aCGH;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class GetCountsWoMiddles extends INOwrapper {

	static final int numCuts = 41;

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar aCGHgetCountsWoMiddles.jar <columnFrom> <numSamples> <in_0.00.txt> <in_0.05.txt> ... <in_2.00.txt> ");
		System.out.println("\tCounts without the middle part, beginning with <columnFrom>");
		System.out.println("\t\tStarting removing interval from 0.05, 0.10, ..., 2.00");
		System.out.println();
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {

		String[][] countArray = new String[numSamples][cutlines.length*2];
		String[] sampleAnnotation = new String[numSamples];

		StringBuffer header = new StringBuffer();
		FileReader fr = frs.get(0);
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		boolean isFirst = true;

		// Get header
		for (int i = 0; i < columnFrom; i++) {
			header.append(tokens[i] + "\t");
		}
		header = new StringBuffer(header.toString().trim());
		for (int i = 0; i < cutlines.length; i++) {
			float cut = cutlines[i];
			header.append("\tLoss_" + String.format("%,.2f", cut) + "\t" + "Gain_" + String.format("%,.2f", cut));
		}
		System.out.println("Write header");
		fm.writeLine(header.toString());

		System.out.println("Get counts...");
		for (int cutIdx = 0; cutIdx < frs.size(); cutIdx++) {
			float cut = cutlines[cutIdx];
			System.out.println(cut);

			fr = frs.get(cutIdx);
			if (cutIdx > 0) {
				isFirst = false;
			}
			if (!isFirst) {
				fr.readLine();	// skip header
			}
			int i = 0;
			int sampleIdx = 0;
			try {
				for (sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
					line = fr.readLine();
					tokens = line.split("\t");
					header = new StringBuffer();
					if (isFirst) {
						for (i = 0; i < columnFrom; i++) {
							header.append(tokens[i] + "\t");
						}
						sampleAnnotation[sampleIdx] = header.toString().trim();
					}
					
					int count_less = 0;
					int count_more = 0;
					for (i = columnFrom; i < tokens.length; i++) {
						if (tokens[i].equals("NA"))
							continue;
						if (Float.parseFloat(tokens[i]) <= cut*(-1)) {
							count_less++;
						} else if (Float.parseFloat(tokens[i]) >= cut){
							count_more++;
						}
					}
					countArray[sampleIdx][cutIdx*2] = String.valueOf(count_less);
					countArray[sampleIdx][cutIdx*2 + 1] = String.valueOf(count_more);
				}
			} catch (Exception e) {
				System.out.println(line);
				System.out.println(tokens[i] + " " + i + " " + sampleIdx);
				
				break;
			}
		}

		// Write down
		for (int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
			StringBuffer newLine = new StringBuffer();
			newLine.append(sampleAnnotation[sampleIdx] + "\t");
			for (int cutIdx = 0; cutIdx < cutlines.length; cutIdx++) {
				newLine.append(countArray[sampleIdx][cutIdx*2] + "\t" + countArray[sampleIdx][cutIdx*2 + 1] + "\t");
			}
			fm.writeLine(newLine.toString().trim());
		}
	}

	static int columnFrom = 0;
	static int numSamples = 0;
	static Float[] cutlines = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 4) {
			columnFrom = Integer.parseInt(args[0]);
			numSamples = Integer.parseInt(args[1]);
			cutlines = new Float[args.length - 2];
			String[] inFiles = new String[args.length - 2];
			for (int i = 2; i < args.length; i++) {
				cutlines[i - 2] = Float.parseFloat(
						args[i].substring(args[i].lastIndexOf(".") - 4,
								args[i].lastIndexOf(".")));
				inFiles[i - 2] = args[i];
			}
			new GetCountsWoMiddles().go(inFiles, "qc_counts_wo_middle.txt");
		} else {
			new GetCountsWoMiddles().printHelp();
		}
	}

}
