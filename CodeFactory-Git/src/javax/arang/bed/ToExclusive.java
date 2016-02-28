package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToExclusive extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		String chr = tokens[Bed.CHROM];
		int start = Integer.parseInt(tokens[Bed.START]);
		int end = Integer.parseInt(tokens[Bed.END]);
		int newStart = 0;
		fm.writeLine(chr + "\t" + newStart + "\t" + start);
		newStart = end;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			fm.writeLine(chr + "\t" + newStart + "\t" + start);
			newStart = end;
		}
		fm.writeLine(chr + "\t" + newStart + "\t" + genomeSize);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedToExclusive.jar <in.bed> <out.bed> <chr_size>");
		System.out.println("\tConvert the chromosome specific <in.bed> to an exclusive <out.bed>.");
		System.out.println("\tThe union of <in.bed> and <out.bed> will be the chr_size.");
		System.out.println("Arang Rhie, 2015-08-18. arrhie@gmail.com");
	}

	private static int genomeSize = 0;
	public static void main(String[] args) {
		if (args.length == 3) {
			genomeSize = Integer.parseInt(args[2]);
			new ToExclusive().go(args[0], args[1]);
		} else {
			new ToExclusive().printHelp();
		}
	}

}
