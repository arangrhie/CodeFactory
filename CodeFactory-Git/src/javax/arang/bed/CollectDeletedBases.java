package javax.arang.bed;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

public class CollectDeletedBases extends I2Owrapper {

	@Override
	public void hooker(FileReader frSV, FileReader frDel, FileMaker fm) {
		Bed frSVbed = new Bed(frSV);
		Bed frDelBed = new Bed(frDel);
		
		ArrayList<Integer> svStarts;
		int start;
		int end;
		int svLen;
		int bases;
		for (String contig : frSVbed.getChrStringList()) {
			svStarts = 	frSVbed.getStarts(contig);
			for (int idx = 0; idx < svStarts.size(); idx++) {
				start = frSVbed.getStartFromIdx(contig, idx);
				end = frSVbed.getEndFromIdx(contig, idx);
				svLen = end - start;
				if (!frDelBed.getChrStringList().contains(contig)) {
					writeDeletions(frSVbed, fm, contig, start, end, idx, "UNPHASED\tUNPHASED");
				} else {
					bases = frDelBed.getBasesInRegion(contig, start, end);
					if (bases > svLen) {
						writeDeletions(frSVbed, fm, contig, start, end, idx, bases + "\t" + String.format("%,.1f", (((float)svLen * 100) / bases)));
					} else {
						writeDeletions(frSVbed, fm, contig, start, end, idx, bases + "\t" + String.format("%,.1f", (((float)bases * 100) / svLen)));
					}
				}
			}
		}
	}

	private void writeDeletions(Bed frSVbed, FileMaker fm, String contig, int start, int end, int idx, String numBases) {
		String notes = frSVbed.getNote(contig, idx);
		fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + notes + "\t" + numBases);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedCollectDeletedBases.jar <in.ins.bed> <in.del.bases.bed> <out.ins.bed>");
		System.out.println("\t<in.ins.bed>: SVs called compared to reference genome");
		System.out.println("\t\t*This file will be loaded into memory");
		System.out.println("\t<in.del.bases.bed>: deletions");
		System.out.println("\t<out.ins.bed>: CONTIG\tSTART\tEND\tTYPE\tTYPE:LEN\tSOURCE\t...\tDEL_BASES\tDEL_BASES%");
		System.out.println("\t\tDEL_BASES%: ((float) min(bases, svLen) * 100) / max(bases, svLen))");
		System.out.println("Arang Rhie, 2015-12-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new CollectDeletedBases().go(args[0], args[1], args[2]);
		} else {
			new CollectDeletedBases().printHelp();
		}
	}

}
