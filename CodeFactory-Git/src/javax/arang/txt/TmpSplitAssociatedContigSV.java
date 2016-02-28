package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class TmpSplitAssociatedContigSV extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig;
		int start;
		int end;
		String type;
		int len;
		String note;
		String assoc;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			len = end - start;
			note = tokens[Bed.NOTE];
			tokens = note.split("-");
			type = tokens[tokens.length - 4];
			assoc = tokens[0];
			for (int i = 1; i < tokens.length - 6; i++) {
				assoc = assoc + "-" + tokens[i];
			}
			fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + type + "\t" + type + ":" + len + "\t" + assoc);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtTmpSplitAssociatedContigSV.jar <in.sv.bed> <out.sv.bed>");
		System.out.println("\t<in.sv.bed>: CONTIG\tSTART\tEND\tASSOC_CONTIG-START-END-TYPE-LEN-LEN-LEN?");
		System.out.println("\t<out.sv.bed>: CONTIG\tSTART\tEND\tTYPE:LEN\tASSOC_CONTIG");
		System.out.println("Arang Rhie, 2015-12-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new TmpSplitAssociatedContigSV().go(args[0], args[1]);
		} else {
			new TmpSplitAssociatedContigSV().printHelp();
		}
	}

}
