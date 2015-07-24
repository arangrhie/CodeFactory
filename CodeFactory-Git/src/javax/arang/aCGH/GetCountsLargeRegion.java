/**
 * 
 */
package javax.arang.aCGH;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class GetCountsLargeRegion extends IOwrapper {

	//static final int numCuts = 41;
	static final int numCuts = 1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		int count_less = 0;
		int count_more = 0;
		//float cut = 0.00f;
		float cut = 0.30f;
		StringBuffer newLine = new StringBuffer();
		for (int i = 0; i < columnFrom; i++) {
			newLine.append(tokens[i] + "\t");
		}
		for (int j = 0; j < numCuts; j++) {
			newLine.append("Loss_" + String.format("%,.2f", cut) + "\t" + "Gain_" + String.format("%,.2f", cut) + "\t");
			cut += 0.05f;
		}
		fm.writeLine(newLine.toString().trim());
		newLine = new StringBuffer();
		cut = 0f;
		
		int count = 0;
		boolean isGain = false;
		boolean isLoss = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			for (int i = 0; i < columnFrom; i++) {
				newLine.append(tokens[i] + "\t");
			}
			cut = 0.00f;
			count = 0;
			//System.out.println(tokens[0]);
			for (int j = 0; j < numCuts; j++) {
				for (int i = columnFrom; i < tokens.length; i++) {
					if (tokens[i].equals("NA")) {
						if (count >= moreThan) {
							if (isGain)	count_more++;
							if (isLoss)	count_less++;
							count = 0;
						}
						isGain = false;
						isLoss = false;
						continue;
					} else if (Float.parseFloat(tokens[i]) < cut*(-1)) {
						if (isLoss) {
							count++;
						} else {	// isGain: increment gain, reset count
							if (count >= moreThan) {
								count_more++;
								count = 0;
							}
							isGain = false;
							isLoss = true;
							count++;
						}
					} else if (Float.parseFloat(tokens[i]) > cut){
						if (isGain) {
							count++;
						} else {
							if (count >= moreThan) {
								count_less++;
								count = 0;
							}
							isGain = true;
							isLoss = false;
							count++;
						}
					} else {
						if (count >= moreThan) {
							if (isGain) count_more++;
							else if (isLoss) count_less++;
						}
						isGain = false;
						isLoss = false;
						count = 0;
					}
					//System.out.println(cut + " | isGain: " + isGain + " | isLoss: " + isLoss + " | count_less: " + count_less + " | count_more: " + count_more + " | count: " + count);
				}
				
				if (count >= moreThan) {
					if (isGain) count_more++;
					else if (isLoss) count_less++;
				}
				newLine.append(count_less + "\t" + count_more + "\t");

				cut += 0.05f;
				count_less = 0;
				count_more = 0;
				count = 0;
			}
			fm.writeLine(newLine.toString().trim());
			newLine = new StringBuffer();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar aCGHgetCountsLargeRegion.jar <in.txt> <columnFrom> <moreThanRegion>");
		System.out.println("\tCounts without the middle part, beginning with <columnFrom>");
		System.out.println("\t\tcovering >= <moreThan> region.");
		System.out.println("\t\tStarting removing interval from 0.00, 0.05, 0.10, ..., 2.00");
	}
	
	static int columnFrom = 0;
	static int moreThan = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			columnFrom = Integer.parseInt(args[1]);
			moreThan = Integer.parseInt(args[2]);
			new GetCountsLargeRegion().go(args[0], args[0].replace(".txt", "_counts_" + moreThan + ".txt"));
		} else {
			new GetCountsLargeRegion().printHelp();
		}
	}
}
