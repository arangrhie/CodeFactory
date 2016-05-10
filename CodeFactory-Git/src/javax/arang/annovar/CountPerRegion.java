package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CountPerRegion extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int from = 0;
		int to = window;
		int pos;
		int count = 0;
		String chr = "";
		String ref;
		String alt;
		
		int countIns = 0;
		int countDel = 0;
		int countSub = 0;
		int countSnp = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[ANNOVAR.CHR];
			pos = Integer.parseInt(tokens[ANNOVAR.POS_FROM]);
			ref = tokens[ANNOVAR.REF];
			alt = tokens[ANNOVAR.ALT];
			
			while (pos > to) {
				fm.writeLine(chr + "\t" + from + "\t" + to + "\t" + count);
				from = to;
				to = to + window;
				count = 0;
			}
			
			if (ref.equals("-")) {
				count += alt.length();
				countIns++;
			} else if (alt.equals("-")) {
				count += ref.length();
				countDel++;
			} else {
				count += alt.length();
				if (alt.length() == 1) {
					countSnp++;
				} else {
					countSub++;
				}
			}
			//count++;
			
		}
		
		if (count>0) {
			fm.writeLine(chr + "\t" + from + "\t" + to + "\t" + count);
		}
		
		count = 0;
		while (to < chromosome_len) {
			from = to;
			to = to + window;
			fm.writeLine(chr + "\t" + from + "\t" + to + "\t" + count);
		}
		
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarCountPerRegion.jar <in.annovar.out> <out.txt> <chr_len> [window=10]");
		System.out.println("\tCounts number of variants in a kb window");
		System.out.println("\t<in.annovar.out>: annovar annotation format");
		System.out.println("\t<out.txt>: CHR\tPOS_FROM\tPOS_TO\tNum_Variations");
		System.out.println("\t<chr_len>: chromosome length. Last region for writing counts.");
		System.out.println("\t[window=10]: window size in kb. DEFAULT=10. INTEGER only.");
		System.out.println("\t*Run this script per chromosome.");
		System.out.println("Arang Rhie, 2016-03-21. arrhie@gmail.com");
	}

	private static int window = 10000;
	private static int chromosome_len = 0;
	public static void main(String[] args) {
		if (args.length == 3) {
			chromosome_len = Integer.parseInt(args[2]);
			new CountPerRegion().go(args[0], args[1]);
		} else if (args.length == 4) {
			chromosome_len = Integer.parseInt(args[2]);
			window = Integer.parseInt(args[3]) * 1000;
			if (window == 0) {
				System.out.println("[ERROR] Window size is 0.");
				System.exit(-1);
			}
			new CountPerRegion().go(args[0], args[1]);
		} else {
			new CountPerRegion().printHelp();
		}
	}

}
