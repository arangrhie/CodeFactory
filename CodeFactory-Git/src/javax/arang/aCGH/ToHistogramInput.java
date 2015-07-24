package javax.arang.aCGH;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToHistogramInput extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		line = fr.readLine();
		tokens = line.split("\t");
		for (int i = 0; i < idx; i++) {
			fm.write(tokens[i] + "\t");
		}
		fm.writeLine("data");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			for (int j = idx; j < tokens.length; j++) {
				for (int i = 0; i < idx; i++) {
					fm.write(tokens[i] + "\t");
				}
				fm.writeLine(tokens[j]);
			}
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		System.out.println("Usage: java -jar aCGHtoHistogram.jar <in.txt> <out.txt> <idx>");
		System.out.println("Converts the data into one column. The data label will be replaced as \"data\".");
		System.out.println("\t<in.txt>: tab delimited file, with <group_1>\t<group_2>..<group_idx>\t..<data1>..");
		System.out.println("\t<out.txt>: <group_1>\t<group2>\t..\t<group_idx>\t<data>");
		System.out.println("\t<idx>: 1-based. DEFAULT=2");
		System.out.println("2014-09-29, Arang Rhie. arrhie@gmail.com");
	}

	static int idx = 2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		idx = Integer.parseInt(args[2]);
		new ToHistogramInput().go(args[0], args[1]);
	}

}
