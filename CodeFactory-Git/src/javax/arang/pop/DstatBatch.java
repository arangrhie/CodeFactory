package javax.arang.pop;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class DstatBatch extends I2Owrapper implements Runnable {

	static String h1Start;
	static String h2Start;
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line = fr2.readLine();
		String[] tokens = line.split("\\s+");

		int h1StartIdx = 0;
		int h2StartIdx = 0;
		
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(h1Start)) {
				h1StartIdx = i;
			}
			if (tokens[i].equals(h2Start)) {
				h2StartIdx = i;
			}
		}
		
		if (h1StartIdx * h2StartIdx == 0) {
			System.out.println("Check your sample name " + h1Start + " " + h2Start);
			System.exit(-1);
		}
		
		fm.write("H1|H2");
		for (int j = h2StartIdx; j < tokens.length; j++) {
			fm.write("\t" + tokens[j] + "_BABA" + "\t" + tokens[j] + "_ABBA" + "\t" + tokens[j] + "_D");
		}
		fm.writeLine();
		
		for (int i = h1StartIdx; i < h2StartIdx - 1; i++) {
			fm.write(tokens[i]);
			for (int j = h2StartIdx; j < tokens.length; j++) {
				Dstat dStat = new Dstat();
				dStat.h1 = tokens[i];
				dStat.h2 = tokens[j];
				dStat.go(fr1.getFullPath(), fr2.getFullPath(), "temp.txt");
				fm.write("\t" + dStat.getnBABA() + "\t" + dStat.getnABBA() + "\t" + String.format("%,.2f", dStat.getD()));
			}
			fm.writeLine();
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar popDstatBatch.jar <AA_derived.snp> <present_derived.snp> <H1_start_name> <H2_start_name>");
		System.out.println("<_derived.snp>: files generated from snpToDerived.jar");
		System.out.println("\t<H1_start_name>: Starting idx of H1 in <present_derived.snp>");
		System.out.println("\t<H2_start_name>: Starting idx of H2 in <present_derived.snp>");
		System.out.println("\tAssumes H1 to be <H1_start_idx> ~ <H2_start_idx> - 1");
		System.out.println("\t\tand H2 to be <H2_start_idx> ~ <last_col_idx>");
		System.out.println("Arang Rhie, 2014-12-22. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			h1Start = args[2];
			h2Start = args[3];
			new DstatBatch().go(args[0], args[1], args[0].replace(".snp", "_") + args[1].replace(".snp", ".d"));
		} else {
			new DstatBatch().printHelp();
		}
	}

	@Override
	public void run() {
		
	}

}
