package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MatrixToColRawTabs extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		String[] colNames = new String[tokens.length - 1];
		for (int i = 0; i < colNames.length; i++) {
			colNames[i] = tokens[i + 1]; 	// tokens[0] is empty
		}
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens.length == 0)	continue;
			for (int i = 1; i < tokens.length; i++) {
				fm.writeLine(tokens[0] + "\t" + colNames[i-1] + "\t" + tokens[i]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtMatrixToColRawTabs <in.tab> <out.tab>");
		System.out.println("Convert a matrix to raw name\tcolumn name\tdata file.");
		System.out.println("\t<in.tab>: column names starting with tab. (first column must be empty, for raw names)");
		System.out.println("\t<out.tab>: raw name\tcolumn name\tdata");
		System.out.println("Arang Rhie, 2015-05-13. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MatrixToColRawTabs().go(args[0], args[1]);
		} else {
			new MatrixToColRawTabs().printHelp();
		}
	}

}
