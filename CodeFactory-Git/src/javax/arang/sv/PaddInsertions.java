package javax.arang.sv;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class PaddInsertions extends IOwrapper {

	public static void main(String[] args) {
		if (args.length == 2) {
			new PaddInsertions().go(args[0], args[1]);
		} else {
			new PaddInsertions().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		int start;
		int end;
		int half_len;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[SV.TYPE].equals("INS")) {
				start = Integer.parseInt(tokens[SV.START]);
				end = Integer.parseInt(tokens[SV.END]);
				half_len = Integer.parseInt(tokens[SV.LEN]) / 2;
				start = Math.max(0, start - half_len);
				end = end + half_len;
				fm.write(tokens[SV.CHR] + "\t" + start + "\t" + end);
				for (int i = SV.CONTIG; i < tokens.length; i++) {
					fm.write("\t" + tokens[i]);
				}
				fm.writeLine();
			} else {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar svPaddInsertions.jar <in.sv.txt> <out.sv.txt>");
		System.out.println("\t<in.sv.txt>: hg19 or hg38 coordinate based list of sv.");
		System.out.println("\t\tFormat: CHR\tSTART\tEND\tCONTIG\tSTART\tEND\tTYPE\tLEN\tDT\tDQ\tSOURCE");
		System.out.println("\t<out.sv.txt>: for insertions, coordinates for START and END will be replaced to:");
		System.out.println("\t\tSTART=START-LEN/2 and END=END+LEN/2");
		System.out.println("Arang Rhie, 2016-06-23. arrhie@gmail.com");
	}

}
