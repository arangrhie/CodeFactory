package javax.arang.genome.gsnap;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Summary extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gsnapSummary.jar <*.gsnap.out>");
		System.out.println("Parse the gsnap alignment format, to get the unpaired / paired / concordants.");
		System.out.println("paired will be: inversion, toolong, scramble.");
		System.out.println("Output file will be writeen to gsnap_summary.stat");
	}

	
	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String line;
		int[] numReads = new int[5];

		String type;
		StringTokenizer st;

		fm.writeLine("Sample\tTotal\tUnpaired\tPaired\tConcordant\tPaired:Inversion\tPaired:Toolong\tPaired:Scramble");
		for (FileReader fr : frs) {
			fm.write(fr.getFileName());
			for (int i = 0; i < 5; i++) {
				numReads[i] = 0;
			}
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("<") || line.startsWith(">")) {
					st = new StringTokenizer(line, "[ \t]");
					st.nextToken();
					st.nextToken();
					type = st.nextToken();
					numReads[getReadType(type)]++;
				}
			}
			int paired = numReads[INVERSION] + numReads[TOOLONG] + numReads[SCRAMBLE];
			int total = numReads[UNPAIRED] + paired + numReads[CONCORDANT];
			fm.writeLine("\t" + String.format("%,d", total) + "\t" + String.format("%,d", numReads[UNPAIRED]) + "\t" + String.format("%,d", paired) + "\t" + String.format("%,d", numReads[CONCORDANT]) 
					+ "\t" + String.format("%,d",numReads[INVERSION]) + "\t" + String.format("%,d",numReads[TOOLONG]) + "\t" + String.format("%,d",numReads[SCRAMBLE]));
		}
	}
	
	static final short UNPAIRED = 0;
	static final short CONCORDANT = 1;
	static final short INVERSION = 2;
	static final short TOOLONG = 3;
	static final short SCRAMBLE = 4;
	
	private int getReadType(String type) {
		if (type.equals("unpaired"))	return UNPAIRED;
		if (type.equals("concordant"))	return CONCORDANT;
		if (type.equals("inversion"))	return INVERSION;
		if (type.equals("toolong"))	return TOOLONG;
		if (type.equals("scramble"))	return SCRAMBLE;
		else {
			System.out.println("Error in type: " + type);
			return -1;
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			new Summary().printHelp();
		} else {
			new Summary().go(args, "./gsnap_summary.stat");
		}
	}

}
