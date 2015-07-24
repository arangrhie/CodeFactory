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
public class GetCounts extends IOwrapper {

	static final int numCuts = 41;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		int count_less = 0;
		int count_more = 0;
		float cut = -1.0f;
		StringBuffer newLine = new StringBuffer();
		
		for (int i = 0; i < columnFrom; i++) {
			newLine.append(tokens[i] + "\t");
		}
		for (int j = 0; j < numCuts; j++) {
			newLine.append("Loss_" + String.format("%,.2f", cut) + "\t" + "Gain_" + String.format("%,.2f", cut) + "\t");
			cut += 0.050f;
		}
		fm.writeLine(newLine.toString().trim());
		newLine = new StringBuffer();
		cut = -1f;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			for (int i = 0; i < columnFrom; i++) {
				newLine.append(tokens[i] + "\t");
			}
			cut = -1.0f;
			for (int j = 0; j < numCuts; j++) {
				for (int i = columnFrom; i < tokens.length; i++) {
					if (tokens[i].equals("NA"))
						continue;
					if (Float.parseFloat(tokens[i]) < cut) {
						count_less++;
					} else if (Float.parseFloat(tokens[i]) > cut) {
						count_more++;
					}
				}
				newLine.append(count_less + "\t" + count_more + "\t");
				cut += 0.050f;
				count_less = 0;
				count_more = 0;
			}
			fm.writeLine(newLine.toString().trim());
			newLine = new StringBuffer();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar aCGHgetCounts.jar <in.txt> <columnFrom>");
		System.out.println("\tGet the counted value of each raw over/under cutline[-1~+1].");
		System.out.println("\t<in.txt>: sample\t...\tdata");
		System.out.println("\t<columnFrom>: [DEFAULT:0] Starting column to get mean value. The forma will be copied.");
	}

	static int columnFrom = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			columnFrom = Integer.parseInt(args[1]);
			new GetCounts().go(args[0], args[0].replace(".txt","_counts_binary.txt"));
		} else {
			new GetCounts().printHelp();
		}
	}

}
