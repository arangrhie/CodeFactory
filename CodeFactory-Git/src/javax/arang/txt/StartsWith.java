/**
 * 
 */
package javax.arang.txt;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

/**
 * @author Arang Rhie
 *
 */
public class StartsWith extends R2wrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		HashSet<String> lookups = new HashSet<String>();
		String token;
		while (fr2.hasMoreLines()) {
			token = fr2.readLine();
			lookups.add(token);
		}
		System.err.println("Loaded " + String.format("%,d", lookups.size()) + " lines");
		
		String line;
		String key;
		int count = 0;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#"))	{
				System.out.println(line);
				continue;
			}
			key = line.split(RegExp.TAB)[colIdx];
			for (String lookup : lookups) {
				if (key.startsWith(lookup)) {
					System.out.println(line);
					count++;
				}
			}
		}
		System.err.println(count + " / " + lookups.size() + " found.");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtStartsWith.jar <in1.txt> <in2.txt> <col_num_of_in1.txt>");
		System.out.println("\tLooks up if <col_num_of_in1.txt> in <in1.txt> starts with anything in <in2.txt>.");
		System.out.println("\t<in1.txt>: any tab-delemited file");
		System.out.println("\t\tLines starting with # will be copied to stdout.");
		System.out.println("\t<in2.txt>: values to look up. 1-value 1-line.");
		System.out.println("\t<col_num_of_in1.txt>: INTEGER, 1-based column number to look up.");
		System.out.println("\t<stdout>: all <in1.txt> lines where <col_num_of_in1.txt> starting with <in2.txt>");
		System.out.println("Arang Rhie, 2017-11-27. arrhie@gmail.com");
	}
	
	private static int colIdx = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new StartsWith().go(args[0], args[1]);
		} else {
			new StartsWith().printHelp();
		}
	}

}
