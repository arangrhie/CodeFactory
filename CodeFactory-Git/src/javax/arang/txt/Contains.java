/**
 * 
 */
package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

/**
 * @author Arang Rhie
 *
 */
public class Contains extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		ArrayList<String> lookups = new ArrayList<String>();
		String token;
		while (fr2.hasMoreLines()) {
			token = fr2.readLine();
			lookups.add(token);
		}
		
		String line;
		String[] tokens;
		String[] values;
		int count = 0;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#"))	{
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			values = tokens[colIdx].split(RegExp.COMMA);
			for (String value : values) {
				if (lookups.contains(value)) {
					fm.writeLine(line);
					count++;
				}
			}
		}
		System.out.println(count + " / " + lookups.size() + " found.");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtContains.jar <in1.txt> <in2.txt> <col_num_of_in1.txt> [out.txt]");
		System.out.println("\tLooks up for <in2.txt> if <col_num_of_in1.txt> in <in1.txt> contains it");
		System.out.println("\t and returns the line into [out.txt]");
		System.out.println("\t<in1.txt>: any tab-delemited file");
		System.out.println("\t\tLines starting with # will be copied into [out.txt].");
		System.out.println("\t\t*Values in column to lookup may contain commas as a seperator");
		System.out.println("\t<in2.txt>: values to look up. 1-value 1-line.");
		System.out.println("\t<col_num_of_in1.txt>: INTEGER, 1-based column number to look up. DEFAULT=1");
		System.out.println("\t[out.txt]: OPTIONAL. DEFAULT=<in1.txt>_in_<in2.txt>");
		System.out.println("Arang Rhie, 2016-02-13. arrhie@gmail.com");
	}
	
	private static int colIdx = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new Contains().go(args[0], args[1], args[0] + "_in_" + args[1]);
		} else if (args.length == 4) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new Contains().go(args[0], args[1], args[3]);
		} else {
			new Contains().printHelp();
		}
	}

}
