package javax.arang.txt.tab;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ChiSquareTest extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		double a;
		double b;
		double c;
		double d;
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar tabChiSquareTest.jar <in.contingency.table> <out.table>");
		System.out.println("Add p-value at the end of each line");
		System.out.println("\t<in.contingency.table>: generated with tabToContingencyTable.jar");
		System.out.println("\t<out.table>: p-value added at the end");
		System.out.println("Arang Rhie, 2016-05-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ChiSquareTest().go(args[0], args[1]);
		} else {
			new ChiSquareTest().printHelp();
		}
	}

}
