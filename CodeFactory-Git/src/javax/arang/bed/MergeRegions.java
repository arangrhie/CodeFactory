package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeRegions extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		String chr = tokens[Bed.CHROM];
		int start = Integer.parseInt(tokens[Bed.START]);
		int end = Integer.parseInt(tokens[Bed.END]);
		String bacID = tokens[Bed.NOTE];
		String pool1 = tokens[Bed.NOTE + 1];
		String pool2 = tokens[Bed.NOTE + 2];
		boolean isMerged = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (chr.equals(tokens[Bed.CHROM]) && bacID.equals(tokens[Bed.NOTE])
					&& pool1.equals(tokens[Bed.NOTE + 1]) && pool2.equals(tokens[Bed.NOTE + 2])) {
				start = Math.min(start, Integer.parseInt(tokens[Bed.START]));
				end =  Math.max(end, Integer.parseInt(tokens[Bed.END]));
				fm.writeLine(chr + "\t" + start	+ "\t" + end
						+ "\t" + bacID + "\t" + pool1 + "\t" + pool2);
				isMerged = true;
			} else {
				if (!isMerged) {
					fm.writeLine(chr + "\t" + start + "\t" + end + "\t" + bacID + "\t" + pool1 + "\t" + pool2);
				}
				chr = tokens[Bed.CHROM];
				start = Integer.parseInt(tokens[Bed.START]);
				end = Integer.parseInt(tokens[Bed.END]);
				bacID = tokens[Bed.NOTE];
				pool1 = tokens[Bed.NOTE + 1];
				pool2 = tokens[Bed.NOTE + 2];
				isMerged = false;
			}
		}
		fm.writeLine(chr + "\t" + start + "\t" + end + "\t" + bacID + "\t" + pool1 + "\t" + pool2);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar <in.bed> <in.merged.bed>");
		System.out.println("\tFor AK1 BAC deconvolution.");
		System.out.println("\t\tMerge regions with same BAC_ID");
		System.out.println("\t<in.bed>: CHR\tSTART\tEND\tBAC_ID\tpool1\tpool2");
		System.out.println("\t\tOutput generated with bedRegionsToPools.jar");
		System.out.println("Arang Rhie, 2015-06-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeRegions().go(args[0], args[1]);
		} else {
			new MergeRegions().printHelp();
		}
	}

}
