package javax.arang.snp;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class ToDerived extends Compare {
	
	boolean isAlt = true;
	int nonBiallelic = 0;

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		compareTwoFiles(fr1, fr2, fm);
		System.out.println("Non-Biallelic Sites: " + nonBiallelic);
	}
	
	private StringBuffer writeDerivedAllele(StringBuffer sb, String[] snpTokens, String aa, int derived) {
		sb = new StringBuffer(snpTokens[SNP.CHR] + "\t"
				+ snpTokens[SNP.START] + "\t"
				+ snpTokens[SNP.STOP] + "\t"
				+ aa + "\t"
				+ snpTokens[derived] + "\t"
				+ snpTokens[SNP.ID]);
		return sb;
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpToDerived.jar <AA.bed> <in.snp> [DerivedOnly=TRUE]");
		System.out.println("\t<AA.bed>: chr\tstart-1\tend\tAA");
		System.out.println("\t<in.snp>: chr\tstart\tend\tref\talt\tGT(0/1/2)");
		System.out.println("\t<DerivedOnly>: if TRUE, does not print \'0\'");
		System.out.println("\t<in_derived.snp>: convert ref to AA, hence the GT.");
		System.out.println("Arang Rhie, 2014-12-19. arrhie@gmail.com");
	}

	private static boolean isDerivedOnly = true; 
	public static void main(String[] args) {
		if (args.length == 2) {
			new ToDerived().go(args[0], args[1], IOUtil.retrieveFileName(args[1]).replace(".snp", "_derived.snp"));
		} else if (args.length == 3) {
			isDerivedOnly = Boolean.parseBoolean(args[2]);
			new ToDerived().go(args[0], args[1], IOUtil.retrieveFileName(args[1]).replace(".snp", "_derived.snp"));
		} else {
			new ToDerived().printHelp();
		}
	}

	@Override
	protected void fr1IsLessThanFr2(String[] tokens1, String[] tokens2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void fr2IsMoreThanFr1(String[] tokens1, String[] tokens2) {
		// TODO Auto-generated method stub
		
	}

	StringBuffer derived = null;

	@Override
	protected void fr1IsEqualToFr2(String[] tokens1, String[] tokens2) {
		if (tokens1[SNP.REF].equals(tokens2[SNP.REF])) {
			derived = writeDerivedAllele(derived, tokens2, tokens1[SNP.REF], SNP.ALT);
			isAlt = true;
		} else if (tokens1[SNP.REF].equals(tokens2[SNP.ALT])) {
			derived = writeDerivedAllele(derived, tokens2, tokens1[SNP.REF], SNP.REF);
			isAlt = false;
		} else {
			nonBiallelic++;
			return;
		}
		
		boolean hasGT = false;
		for (int i = SNP.SAMPLE_START; i < tokens2.length; i++) {
			if (tokens2[i].equals("0")) {
				if (isAlt) {
					derived.append("\t0");
				}
				else {
					derived.append("\t2");
					hasGT = true;
				}
			}
			if (tokens2[i].equals("1")) {
				derived.append("\t1");
				hasGT = true;
			}
			if (tokens2[i].equals("2")) {
				if (isAlt) {
					derived.append("\t2");
					hasGT = true;
				}
				else {
					derived.append("\t0");
				}
			}
		}
		if (isDerivedOnly && hasGT) {
			fm.writeLine(derived.toString());
		} else if (!isDerivedOnly){
			fm.writeLine(derived.toString());
		}
	}

	@Override
	protected void fr1ToTheEnd(String line1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void fr2ToTheEnd(String line2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isFr1ProceedToEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isFr2ProceedToEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void chrLine1(String line1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void chrLine2(String line) {
		fm.writeLine(line.replace("REF", "AA").replace("ALT", "DERIVED"));		
	}

}
