package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeBACCESregion extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig1;
		String contig2;
		int contig1start;
		int contig1end;
		int contig2start;
		int contig2end;
		String bacid;
		String paired;
		FileMaker fmNoMatch = new FileMaker(fm.getDir(), fm.getFileName().replace(".bed", ".unmapped.list"));
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig1 = tokens[Bed.CHROM];
			contig2 = tokens[3];
			bacid = tokens[9] + "\t" + tokens[10];
			if (tokens[7].length() == 1) {
				paired = "Unpaired";
			} else {
				paired = "Paired";
			}
			if (!contig1.equals("NULL")) {
				contig1start = Integer.parseInt(tokens[Bed.START]);
				contig1end = Integer.parseInt(tokens[Bed.END]);
				if (contig1.equals(contig2)) {
					contig2start = Integer.parseInt(tokens[3 + 1]);
					contig2end = Integer.parseInt(tokens[3 + 2]);
					fm.writeLine(contig1 + "\t" + Math.min(contig1start, contig2start) + "\t" + Math.max(contig1end, contig2end) + "\t" + bacid + "\t" + paired);
				} else if (contig2.equals("NULL")) {
					fm.writeLine(contig1 + "\t" + contig1start + "\t" + contig1end + "\t" + bacid + "\t" + paired);
				} else {
					// contig1 != contig2
					contig2start = Integer.parseInt(tokens[3 + 1]);
					contig2end = Integer.parseInt(tokens[3 + 2]);
					fm.writeLine(contig1 + "\t" + contig1start + "\t" + contig1end + "\t" + bacid + "\tTransloc");
					fm.writeLine(contig2 + "\t" + contig2start + "\t" + contig2end + "\t" + bacid + "\tTransloc");
				}
			} else {
				// contig1 == NULL
				if (!contig2.equals("NULL")) {
					contig2start = Integer.parseInt(tokens[3 + 1]);
					contig2end = Integer.parseInt(tokens[3 + 2]);
					fm.writeLine(contig2 + "\t" + contig2start + "\t" + contig2end + "\t" + bacid + "\t" + paired);
				} else {
					// contig1 == NULL && contig2 == NULL
					fmNoMatch.writeLine(bacid);
				}
			}
		}
		fmNoMatch.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeBACCESregion.jar <in.bed> <out.bed>");
		System.out.println("\tMerge if contigs are same between pairs");
		System.out.println("\t<in.bed>: made by Sangjin");
		System.out.println("\t*This code may not be re-usable...");
		System.out.println("Arang Rhie, 2015-11-19. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeBACCESregion().go(args[0], args[1]);
		} else {
			new MergeBACCESregion().printHelp();
		}
	}

}
