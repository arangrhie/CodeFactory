package javax.arang.genome.snp;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.snp.SNP;
import javax.arang.vcf.VCF;

public class MergeSNPs extends I2Owrapper {
	
	private static final boolean IS_FILE1 = true;
	private static final boolean IS_FILE2 = false;
	
	boolean isReady1 = true;
	boolean isReady2 = true;
	
	String line1;
	String line2;
	String[] tokens1;
	String[] tokens2;
	String chr1;
	String chr2;
	int start1;
	int start2;
	
	int countMerged = 0;
	int count1 = 0;
	int count2 = 0;
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {

		FileMaker fm1 = new FileMaker(fr1.getFileName().replace(".snp", "_only.snp"));
		FileMaker fm2 = new FileMaker(fr2.getFileName().replace(".snp", "_only.snp"));
		FileMaker fmMulti = new FileMaker(fm.getFileName().replace(".snp", ".mnp"));

		boolean isToCompare = false;
		line1 = fr1.readLine();
		tokens1 = line1.split("\t");
		line2 = fr2.readLine();
		tokens2 = line2.split("\t");
		writeLine(line1, line2, tokens1, tokens2, fm, fmMulti);
		
		// fmMulti header string
		fmMulti.write(VCF.getHeaderString());
		for (int i = SNP.SAMPLE_START; i < tokens1.length; i++) {
			fmMulti.write("\t" + tokens1[i]);
		}
		for (int i = SNP.SAMPLE_START; i < tokens2.length; i++) {
			fmMulti.write("\t" + tokens2[i]);
		}
		fmMulti.writeLine();

		fm1.writeLine(line1);
		fm2.writeLine(line2);
		
		readLine(fr2, IS_FILE2);
		
		FR1_LOOP : while (fr1.hasMoreLines()) {
			readLine(fr1, IS_FILE1);

			// chr1 != chr2
			if (SNP.getChrIntVal(chr1) < SNP.getChrIntVal(chr2)) {
				writeLine(line1, fm1, IS_FILE1);
				continue FR1_LOOP;
			} else if (SNP.getChrIntVal(chr1) > SNP.getChrIntVal(chr2)) {
				writeLine(line2, fm2, IS_FILE2);
				FR2_LOOP : while (fr2.hasMoreLines()) {
					readLine(fr2, IS_FILE2);
					if (SNP.getChrIntVal(chr1) < SNP.getChrIntVal(chr2)) {
						writeLine(line1, fm1, IS_FILE1);
						continue FR1_LOOP;
					}
					if (SNP.getChrIntVal(chr1) == SNP.getChrIntVal(chr2)) {
						isToCompare = true;
						break FR2_LOOP;
					}
					writeLine(line2, fm2, IS_FILE2);
				}
				if (!fr2.hasMoreLines() && !isToCompare) {
					writeLine(line1, fm1, IS_FILE1);
					writeLine(line2, fm2, IS_FILE2);
					break FR1_LOOP;
				}
			}
			
			// chr1 == chr2
			if (start1 < start2) {
				writeLine(line1, fm1, IS_FILE1);
				continue FR1_LOOP;
			} else if (start1 > start2) {
				writeLine(line2, fm2, IS_FILE2);
				while (fr2.hasMoreLines()) {
					readLine(fr2, IS_FILE2);
					if (start1 == start2) {
						writeLine(line1, line2, tokens1, tokens2, fm, fmMulti);
						if (fr2.hasMoreLines()) {
							readLine(fr2, IS_FILE2);
							continue FR1_LOOP;
						} else {
							break FR1_LOOP;
						}
					} else if (start1 < start2) {
						writeLine(line1, fm1, IS_FILE1);
						continue FR1_LOOP;
					}
					writeLine(line2, fm2, IS_FILE2);
				}
				if (!fr2.hasMoreLines()) {
					writeLine(line1, fm1, IS_FILE1);
					writeLine(line2, fm2, IS_FILE2);
					break FR1_LOOP;
				}
			} else {	// Integer.parseInt(tokens1[SNP.START]) == Integer.parseInt(tokens2[SNP.START])
				writeLine(line1, line2, tokens1, tokens2, fm, fmMulti);
				if (fr2.hasMoreLines()) {
					readLine(fr2, IS_FILE2);
					continue FR1_LOOP;
				} else {
					break FR1_LOOP;
				}
			}
		}
		
		//System.out.println("[DEBUG]\t" + line1 + "\t" + line2 + "\t <-- END of join. start writing each file's remainings.");
		
		if (isReady1) {
			fm1.writeLine(line1);
			count1++;
		}
		
		if (isReady2) {
			fm2.writeLine(line2);
			count2++;
		}
		
		while (fr1.hasMoreLines()) {
			line1 = fr1.readLine();
			fm1.writeLine(line1);
			count1++;
		}
		
		while (fr2.hasMoreLines()) {
			line2 = fr2.readLine();
			fm2.writeLine(line2);
			count2++;
		}
		
		System.out.println("Total number of merged SNPs: " + String.format("%,d", (countMerged-1)));
		System.out.println("Total number of SNPs in " + fm1.getFileName() + ": " + String.format("%,d", count1));
		System.out.println("Total number of SNPs in " + fm2.getFileName() + ": " + String.format("%,d", count2));
		
	}
	
	private void readLine(FileReader fr, boolean isFile1) {
		if (isFile1) {
			line1 = fr.readLine();
			tokens1 = line1.split("\t");
			chr1 = tokens1[SNP.CHR];
			start1 = Integer.parseInt(tokens1[SNP.START]);
			isReady1 = true;
		} else {
			line2 = fr.readLine();
			tokens2 = line2.split("\t");
			chr2 = tokens2[SNP.CHR];
			start2 = Integer.parseInt(tokens2[SNP.START]);
			isReady2 = true;
		}
	}
	
	private void writeLine(String line1, String line2, String[] tokens1, String[] tokens2, FileMaker fm, FileMaker fmMulti) {
		//System.out.println("[DEBUG]\t" + line1 + "\t" + line2 + "\t<--Merged");
		if (tokens1[SNP.ALT].equals(tokens2[SNP.ALT])) {
			fm.write(tokens1[SNP.CHR] + "\t" + tokens1[SNP.START] + "\t" + tokens1[SNP.STOP] + "\t" + tokens1[SNP.REF] + "\t" + tokens1[SNP.ALT] + "\t" + tokens1[SNP.ID]);
		} else {
			String alt;
			if (!tokens1[SNP.ALT].equals(".")) {
				if (!tokens2[SNP.ALT].equals(".")) {
					fmMulti.write(tokens1[SNP.CHR] + "\t" + tokens1[SNP.START] + "\t" + tokens1[SNP.ID] + "\t" + tokens1[SNP.REF] + "\t" + tokens1[SNP.ALT] + "," + tokens2[SNP.ALT] + "\t.\t.\t.\t.");
					for (int i = SNP.SAMPLE_START; i < tokens1.length; i++) {
						fmMulti.write("\t" + (tokens1[i].equals("1") ? "0/1" : (tokens1[i].equals("2") ? "1/1" : (tokens1[i].equals("0") ?"0/0" : "./."))));
					}
					for (int i = SNP.SAMPLE_START; i < tokens2.length; i++) {
						fmMulti.write("\t" + (tokens2[i].equals("1") ? "0/2" : (tokens2[i].equals("2") ? "2/2" : (tokens2[i].equals("0") ?"0/0" : "./."))));
					}
					fmMulti.writeLine();
				}
				alt = tokens2[SNP.ALT];
			} else {
				alt = tokens1[SNP.ALT];
			}
			fm.write(tokens1[SNP.CHR] + "\t" + tokens1[SNP.START] + "\t" + tokens1[SNP.STOP] + "\t" + tokens1[SNP.REF] + "\t" + alt + "\t" + tokens1[SNP.ID]);
		}
		for (int i = SNP.SAMPLE_START; i < tokens1.length; i++) {
			fm.write("\t" + tokens1[i]);
		}
		for (int i = SNP.SAMPLE_START; i < tokens2.length; i++) {
			fm.write("\t" + tokens2[i]);
		}
		fm.writeLine();
		isReady1 = false;
		isReady2 = false;
		countMerged++;
	}
	
	private void writeLine(String line, FileMaker fm, boolean isFm1) {
		if (isFm1 && isReady1) {
			//System.out.println("[DEBUG]\t" + line + "\t<--" + (isFm1?"fm1":"fm2"));
			fm.writeLine(line);
			isReady1 = false;
			count1++;
		} else if (isReady2){
			//System.out.println("[DEBUG]\t" + line + "\t<--" + (isFm1?"fm1":"fm2"));
			fm.writeLine(line);
			isReady2 = false;
			count2++;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar snpMergeSNPs.jar <in1.snp> <in2.snp> <out.snp>");
		System.out.println("\tGenerates 3 files: <out.snp>, <in1_only.snp>, <in2_only.snp>");
		System.out.println("\t<in1.snp> and <in2.snp> MUST BE sorted.");
		System.out.println("\tSNPs with different ALT alleles will be discarded.");
		System.out.println("Arang Rhie, 2014-11-14. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new MergeSNPs().go(args[0], args[1], args[2]);
		} else {
			new MergeSNPs().printHelp();
		}
	}

}
