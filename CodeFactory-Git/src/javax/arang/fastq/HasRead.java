package javax.arang.fastq;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.IO.basic.RegExp;

public class HasRead extends R2wrapper {

	private static String colName = "";
	
	@Override
	public void hooker(FileReader frList, FileReader frFastq) {
		String line = frList.readLine();
		if (line.startsWith("#")) {
			System.out.println(line + "\t" + colName);
		} else {
			System.out.println("#List\t" + colName);
			frList.reset();
		}
		
		String readid;
		int lineNum = 0;
		HashSet<String> fastqList = new HashSet<String>();
		
		while (frFastq.hasMoreLines()) {
			line = frFastq.readLine();
			lineNum++;
			if (lineNum %4 == 1) {
				readid = line.substring(1);
				readid = readid.split(RegExp.WHITESPACE)[0];
				fastqList.add(readid);
			}
			if (lineNum %4 == 0) {
				lineNum = 0;
			}
		}
		System.err.println("Loaded " + String.format("%,d", fastqList.size()) + " reads");
		
		String[] tokens;
		int numContains = 0;
		while (frList.hasMoreLines()) {
			line = frList.readLine();
			tokens = line.split(RegExp.TAB);
			if (fastqList.contains(tokens[0])) {
				System.out.println(line + "\tO");
				numContains++;
			} else {
				System.out.println(line + "\tX");
			}
		}
		System.err.println("Contains " + String.format("%,d", numContains) + " reads");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqHasRead.jar <in.list> <in.fastq> [COL_NAME]");
		System.out.println("\t<in.list>: list of read ids");
		System.out.println("\t<in.fastq>: fastq");
		System.out.println("\t[COL_NAME]: DEFAULT=<in.fastq>. Name of the column.");
		System.out.println("\t<stdout>: <in.list> appended with \\t[O/X], with header starting with #\t[COL_NAME]");
		System.out.println("\t\twhere O means \'has\' and X means \'has not\'.");
		System.out.println("Arang Rhie, 2017-11-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			colName = IOUtil.retrieveFileName(args[1]);
			new HasRead().go(args[0], args[1]);
		} else if (args.length == 3) {
			colName = args[2];
			new HasRead().go(args[0], args[1]);
		} else {
			new HasRead().printHelp();
		}
	}

}
