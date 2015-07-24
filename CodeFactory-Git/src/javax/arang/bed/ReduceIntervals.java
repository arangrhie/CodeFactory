/**
 * 
 */
package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class ReduceIntervals extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		while (tokens.length < 3) {
			fm.writeLine(line);
			line = fr.readLine();
			tokens = line.split("\t");
		}
		
		Long prev_start = Long.parseLong(tokens[Bed.START]);
		Long prev_end = Long.parseLong(tokens[Bed.END]);
		String prev_chr = tokens[Bed.CHROM];
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length < 3) {
				fm.writeLine(line);
				continue;
			}
			Long start = Long.parseLong(tokens[Bed.START]);
			Long end = Long.parseLong(tokens[Bed.END]);
			String chr = tokens[Bed.CHROM];
			if (chr.equals(prev_chr) && start <= prev_end) {
//				System.out.println(":: DEBUG :: " + prev_chr + ":" + prev_start + "-" + prev_end);
//				System.out.println(":: DEBUG :: " + chr + ":" + start + "-" + end);
//				System.out.println();
				prev_end = end;
				prev_chr = tokens[Bed.CHROM];
			} else {
				fm.writeLine(prev_chr + "\t" + prev_start + "\t" + prev_end);
				prev_start = start;
				prev_end = end;
				prev_chr = tokens[Bed.CHROM];
			}
		}
		fm.writeLine(prev_chr + "\t" + prev_start + "\t" + prev_end);
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedReduceIntervals.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: not well formatted bed file, where end <= next line start.");
		System.out.println("\t<out.bed>: reduced interval bed file.");
		System.out.println("Arang Rhie, 2014-01-21. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ReduceIntervals().go(args[0], args[1]);
		} else {
			new ReduceIntervals().printHelp();
		}
	}

}
