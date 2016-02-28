package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToEdge extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fmEdge) {
		String line;
		String[] tokens;
		boolean isFirst = true;
		String nextScaffoldName;
		String scaffoldName = "";
		String contig1 = "";
		String contig2 = "";
		String contig1Ort = "";
		String contig2Ort = "";
		int cCount = 0;
//		String orientation1 = "=";
//		String orientation2 = "=";
//		String support = "10000";
		int reads1 = 0;
		int reads2 = 0;
		int start1 = 0;
		int end1 = 0;
		int len = 0;
		int contigLen = 0;
		int contigLen2 = 0;
		int start2 = 0;
		int end2 = 0;
		int len2 = 0;
		
		FileMaker fmMiddle = new FileMaker(fmEdge.getDir(), fmEdge.getFileName() + ".middle");
		FileMaker fmLink = new FileMaker(fmEdge.getDir(), fmEdge.getFileName() + ".link");
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				continue;
			}
			tokens = line.split(RegExp.TAB);
			
			if (isFirst) {
				isFirst = false;
			} else {
				nextScaffoldName = tokens[Bed.NOTE];
				if (scaffoldName.equals(nextScaffoldName)) {
					contig2 = tokens[Bed.CHROM];
					start2 = Integer.parseInt(tokens[Bed.START]);
					end2 = Integer.parseInt(tokens[Bed.END]);
					len2 = end2 - start2;
					contigLen2 = Integer.parseInt(tokens[Bed.NOTE + 5]);
					reads2 = Integer.parseInt(tokens[Bed.NOTE + 3]);
					
					if (((100 * len2) / contigLen2) > 90 && (start2 < 50000 && (contigLen2 - end2) < 50000) && Math.abs(start2 - (contigLen2 - end2)) < 1000) {
						// A: Almost all region is covered
						contig2Ort = "A";
						//orientation2 = "=";
					} else {
						if (start2 < 50000 && start2 < (contigLen2 - end2)) {
							contig2Ort = "[H]";
							//orientation2 = "+"; 
						} else if ((contigLen2 - end2) < 50000 && (contigLen2 - end2) < start2) {
							contig2Ort = "[T]";
							//orientation2 = "-";
						} else {
							contig2Ort = "D" + (cCount + 1);
							//orientation2 = "=";
						}
					}
//					if (contig1Ort.equals("A") && contig2Ort.equals("A")) {
//						fm.writeLine(contig1 + "\t" + "+" + "\t" + contig1 + ":E" + "\t" + contig2 + ":E" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig1 + "\t" + "-" + "\t" + contig1 + ":B" + "\t" + contig2 + ":E" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig1 + "\t" + "+" + "\t" + contig1 + ":E" + "\t" + contig2 + ":B" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig1 + "\t" + "-" + "\t" + contig1 + ":B" + "\t" + contig2 + ":B" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "-" + "\t" + contig2 + ":E" + "\t" + contig1 + ":E" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "+" + "\t" + contig2 + ":B" + "\t" + contig1 + ":E" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "-" + "\t" + contig2 + ":E" + "\t" + contig1 + ":B" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "+" + "\t" + contig2 + ":B" + "\t" + contig1 + ":B" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//					} else if (contig1Ort.equals("A") && !contig2Ort.equals("A")) {
//						fm.writeLine(contig1 + "\t" + "+" + "\t" + contig1 + ":E" + "\t" + contig2 + ":" + contig2Ort + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig1 + "\t" + "-" + "\t" + contig1 + ":B" + "\t" + contig2 + ":" + contig2Ort + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + orientation2 + "\t" + contig2 + ":" + contig2Ort + "\t" + contig1 + ":E" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + orientation2 + "\t" + contig2 + ":" + contig2Ort + "\t" + contig1 + ":B" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//					} else if (!contig1Ort.equals("A") && contig2Ort.equals("A")) {
//						fm.writeLine(contig1 + "\t" + orientation1 + "\t" + contig1 + ":" + contig1Ort + "\t" + contig2 + ":E" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig1 + "\t" + orientation1 + "\t" + contig1 + ":" + contig1Ort + "\t" + contig2 + ":B" + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "-" + "\t" + contig2 + ":E" + "\t" + contig1 + ":E" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + "+" + "\t" + contig2 + ":B" + "\t" + contig1 + ":B" + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//					} else if (!contig1Ort.equals("A") && !contig2Ort.equals("A")) {
//						fm.writeLine(contig1 + "\t" + orientation1 + "\t" + contig1 + ":" + contig1Ort + "\t" + contig2 + ":" + contig2Ort + "\t" + support + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + scaffoldName);
//						fm.writeLine(contig2 + "\t" + orientation2 + "\t" + contig2 + ":" + contig2Ort + "\t" + contig1 + ":" + contig1Ort + "\t" + support + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
//					}
					if ((contig1Ort.endsWith("[H]") || contig1Ort.endsWith("[T]")) && (contig2Ort.endsWith("[H]") || contig2Ort.endsWith("[T]"))) {
						fmEdge.writeLine(contig1 + contig1Ort + "\t" + contig2 + contig2Ort + "\t\t" + Math.min(reads1, reads2) + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
						fmLink.writeLine(contig1 + contig1Ort + "\t" + contig2 + contig2Ort + "\t\t" + Math.min(reads1, reads2) + "\t" + scaffoldName);
					} else {
						//fm.writeLine(contig1 + contig1Ort + "\t" + contig2 + contig2Ort + "\t" + Math.min(reads1, reads2) + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
						fmMiddle.writeLine(contig1 + contig1Ort + "\t" + contig2 + contig2Ort + "\t\t" + Math.min(reads1, reads2) + "\t" + start1 + "\t" + end1 + "\t" + contigLen + "\t" + start2 + "\t" + end2 + "\t" + contigLen2 + "\t" + scaffoldName);
					}
				}
			}
			
			scaffoldName = tokens[Bed.NOTE];
			contig1 = tokens[Bed.CHROM];
			start1 = Integer.parseInt(tokens[Bed.START]);
			end1 = Integer.parseInt(tokens[Bed.END]);
			len = end1 - start1;
			reads1 = Integer.parseInt(tokens[Bed.NOTE + 3]);
			contigLen = Integer.parseInt(tokens[Bed.NOTE + 5]);
			contig1Ort = "";
			contig2Ort = "";
			
			if (((100 * len) / contigLen) > 90 && (start1 < 50000 && (contigLen - end1) < 50000)  && Math.abs(start1 - (contigLen - end1)) < 1000) {
				// A: Almost all region is covered
				contig1Ort = "A";
				//orientation1 = "="; 
			} else {
				if (start1 < 50000 && start1 < (contigLen - end1)) {
					contig1Ort = "[H]";
					//orientation1 = "-";
				} else if ((contigLen - end1) < 50000 && (contigLen - end1) < start1) {
					contig1Ort = "[T]";
					//orientation1 = "+";
				} else {
					cCount++;
					contig1Ort = "D" + cCount;
					//orientation1 = "="; 
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedToEdge.jar <in.bed> <out.edge>");
		System.out.println("\t<in.bed>: generated with bedSort.jar and bedSortByReadName.jar");
		System.out.println("\t\t*Filter occurrence of READ_NAME==2 before running this code.");
		System.out.println("\t<out.edge>: Use READ_NAME field as evidence for bridging (edging) two contigs");
		System.out.println("\t\t<out.edge.link> and <out.edge.middle> will be also generated.");
		System.out.println("Arang Rhie, 2015-10-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToEdge().go(args[0], args[1]);
		} else {
			new ToEdge().printHelp();
		}
	}

}
