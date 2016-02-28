package javax.arang.base;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.TotalDepth;

public class MergeTotalDepth extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {

		String line1 = fr1.readLine();
		if (line1.startsWith("#") || line1.startsWith("Chromosome")) {
			line1 = fr1.readLine();
		}
		String[] tokens1 = line1.split(RegExp.TAB);
		String chr1 = tokens1[TotalDepth.CONTIG];
		int pos1 = Integer.parseInt(tokens1[TotalDepth.POS]);
		
		String line2 = fr2.readLine();
		if (line2.startsWith("#") || line2.startsWith("Chromosome")) {
			line2 = fr2.readLine();
		}
		String[] tokens2 = line2.split(RegExp.TAB);
		String chr2 = tokens2[TotalDepth.CONTIG];
		int pos2 = Integer.parseInt(tokens2[TotalDepth.POS]);
		
		if (!chr1.equals(chr2)) {
			System.out.println("[ERROR] :: This code runs only on same chromosomes!");
			System.exit(-9);
		}
		
		int d;
		
		while (fr1.hasMoreLines() && fr2.hasMoreLines()) {
			if (pos1 < pos2) {
				fm.writeLine(line1);
				line1 = fr1.readLine();
				tokens1 = line1.split(RegExp.TAB);
				pos1 = Integer.parseInt(tokens1[TotalDepth.POS]);
			} else if (pos1 > pos2) {
				fm.writeLine(line2);
				line2 = fr2.readLine();
				tokens2 = line2.split(RegExp.TAB);
				pos2 = Integer.parseInt(tokens2[TotalDepth.POS]);
			} else if (pos1 == pos2) {
				d = Integer.parseInt(tokens1[TotalDepth.DEPTH]) + Integer.parseInt(tokens2[TotalDepth.DEPTH]);
				fm.writeLine(chr1 + "\t" + pos1 + "\t" + d);
				line1 = fr1.readLine();
				tokens1 = line1.split(RegExp.TAB);
				pos1 = Integer.parseInt(tokens1[TotalDepth.POS]);
				line2 = fr2.readLine();
				tokens2 = line2.split(RegExp.TAB);
				pos2 = Integer.parseInt(tokens2[TotalDepth.POS]);
			}
		}
		
		if (pos1 < pos2) {
			fm.writeLine(line1);
		} else if (pos1 > pos2) {
			fm.writeLine(line2);
		} else if (pos1 == pos2) {
			d = Integer.parseInt(tokens1[TotalDepth.DEPTH]) + Integer.parseInt(tokens2[TotalDepth.DEPTH]);
			fm.writeLine(chr1 + "\t" + pos1 + "\t" + d);
		}
		
		while (fr1.hasMoreLines()) {
			fm.writeLine(fr1.readLine());
		}
		
		while (fr2.hasMoreLines()) {
			fm.writeLine(fr2.readLine());
		}
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseMergeTotalDepth.jar <in1.depth> <in2.depth> <out.depth>");
		System.out.println("\tMerge <in1.depth> and <in2.depth>");
		System.out.println("\t<in1.depth> and <in2.depth>: Output of baseToTotalDepth.jar");
		System.out.println("\t\t*Assumes to be in same chromosome. RUN ONLY PER CHROMOSOMES!!");
		System.out.println("\t<out.depth>: merged <in1.depth> and <in2.depth>.");
		System.out.println("Arang Rhie, 2015-12-12. arrhie@gmail.com");
	}
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new MergeTotalDepth().go(args[0], args[1], args[2]);
		} else {
			new MergeTotalDepth().printHelp();
		}
	}

}
