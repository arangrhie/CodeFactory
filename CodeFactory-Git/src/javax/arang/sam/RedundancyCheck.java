package javax.arang.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RedundancyCheck extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String prevPos = "";
		HashMap<String, String> pair2table = new HashMap<String, String>();
		
		int beforeNumLines = 0;
		int afterNumLines = 0;
		
		while (fr.hasMoreLines()) {
			String line = fr.readLine();
			if (line.length() < 5)	continue;
			if (line.equals(""))	continue;
			if (line.startsWith("@"))	continue;
			beforeNumLines++;
			String[] tokens = line.split("\t");
			if (prevPos.equals(tokens[Sam.POS])) {
				if (pair2table.containsKey(tokens[Sam.PNEXT])) {
					// if end position are equal, check quality
					String prevStr = pair2table.get(tokens[Sam.PNEXT]);
					String[] prevStrTokens = prevStr.split("\t");
					if (getQualSum(prevStrTokens[Sam.QUAL]) < getQualSum(tokens[Sam.QUAL])) {
						pair2table.put(tokens[Sam.PNEXT], line);
					}
				} else {
					// if new end position, put in table
					pair2table.put(tokens[Sam.PNEXT], line);
				}
			} else {
				// write all reads starting w/ prevPos
				for (String key : pair2table.keySet()) {
					fm.writeLine(pair2table.get(key));
					afterNumLines++;
				}
				
				// initialize other vars
				pair2table.clear();
				prevPos = tokens[Sam.POS];
			}
		}
		System.out.println(fr.getFileName() + "\t" + beforeNumLines + "\t->\t" + afterNumLines + "\t" + String.format("%,.2f", (((float)(beforeNumLines - afterNumLines)* 100) / beforeNumLines)));
	}

	private int getQualSum(String qualString) {
		int sum = 0;
		for (int i = 0; i < qualString.length(); i++) {
			sum += qualString.charAt(i);
		}
		return sum;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samRedundancyCheck.jar <in_sorted.sam>");
		System.out.println("\t<in_sorted.sam.red>: redundancy filtered reads");
		System.out.println("\t\tRead alignment positions are compared at both ends,");
		System.out.println("\t\t and the best read with high quality is choosen.");
		System.out.println("\t\tRead id of pair1 and pair2 may not match.");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new RedundancyCheck().go(args[0], args[0] + ".red");
		} else {
			new RedundancyCheck().printHelp();
		}

	}

}
