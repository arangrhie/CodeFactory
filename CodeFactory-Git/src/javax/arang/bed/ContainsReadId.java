/**
 * 
 */
package javax.arang.bed;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

/**
 * @author Arang Rhie
 *
 */
public class ContainsReadId extends R2wrapper {

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
		
		String line;
		String[] tokens;
		String[] values;
		int count = 0;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#"))	{
				System.out.println(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			values = tokens[colIdx].split(RegExp.SLASH);
			if (lookups.contains(values[0])) {
				System.out.println(line);
				count++;
			}
		}
		System.err.println(count + " / " + lookups.size() + " found.");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedContainsReadId.jar <in.bed> <readid.list> [col_num_of_in.bed=4]");
		System.out.println("\tLooks up for <in2.txt> if <col_num_of_in1.txt> in <in1.txt> contains it,");
		System.out.println("\t and returns the overlapping line to standard output.");
		System.out.println("\t<in.bed>: any tab-delemited file");
		System.out.println("\t\tLines starting with # will be copied to stdout.");
		System.out.println("\t\t*slash in specified cloumn are also treated as seperator.");
		System.out.println("\t<readid.list>: values to look up. 1-value 1-line.");
		System.out.println("\t[col_num_of_in.bed]: INTEGER, 1-based column number to look up. DEFAULT=4");
		System.out.println("Arang Rhie, 2018-01-03. arrhie@gmail.com");
	}
	
	private static int colIdx = 3;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ContainsReadId().go(args[0], args[1]);
		} else if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new ContainsReadId().go(args[0], args[1]);
		} else {
			new ContainsReadId().printHelp();
		}
	}

}
