package javax.arang.pop;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.snp.Compare;
import javax.arang.snp.SNP;

public class Dstat extends Compare {

	public Dstat() {
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar popDstat.jar <ancient_derived.snp> <present_pops_derived.snp> <H1_Name> <H2_Name>");
		System.out.println("\tComputes the D-statistics for <ancient.snp>, compared to <present_pops> (populations).");
		System.out.println("\t<ancient_derived.snp>: chr\tstart\tstop\tref\talt\tindividual");
		System.out.println("\t<present_pops_derived.snp>: chr\tstart\tstop\tref\talt\tpop1\tpop2\t...\tpopN");
		System.out.println("\t<H1_Name>: Name of the first present human population to compare");
		System.out.println("\t<H2_Name>: Name of the second present human population to compare");
		System.out.println("\t<out>.d: nBABA\tnABBA\tD");
		System.out.println("\t\tD is calculated as (nBABA - nABBA)/(nBABA + nABBA)");
		System.out.println("Arang Rhie, 2014-12-19. arrhie@gmail.com");
	}
	
	public static float getDstat(int nBABA, int nABBA) {
		float d = (float) (nBABA - nABBA) / (nBABA + nABBA); 
		return d;
	}

	int h1Idx = 0;
	int h2Idx = 0;
	int nBABA = 0;
	int nABBA = 0;
	int nOthers = 0;
	float d = 0.0f;
	int nAA = 0;
	int nP1 = 0;
	
	public int getnBABA() {
		return nBABA;
	}
	
	public int getnABBA() {
		return nABBA;
	}
	
	public float getD() {
		return d;
	}
	

	@Override
	public void hooker(FileReader ancientFr, FileReader presentFr, FileMaker fm) {
		compareTwoFiles(ancientFr, presentFr, fm);
		d = getDstat(nBABA, nABBA);
		fm.writeLine(h1 + "\t" + h2 + "\t" + nBABA + "\t" + nABBA + "\t" + String.format("%,.5f", d));
		System.out.println("[DEBUG] " + h1 + "\t" + h2 + "\t" + nBABA + "\t" + nABBA + "\t" + String.format("%,.5f", d));
	}

	public String h1;
	public String h2;
	public static void main(String[] args) {
		if (args.length == 4) {
			Dstat dStat = new Dstat();
			dStat.h1 = args[2];
			dStat.h2 = args[3];
			dStat.go(args[0], args[1], IOUtil.retrieveFileName(args[0]).replace(".snp", "_") + IOUtil.retrieveFileName(args[1]).replace(".snp", ".d"));
		} else {
			new Dstat().printHelp();
		}

	}

	@Override
	protected void fr1IsLessThanFr2(String[] tokens1, String[] tokens2) {
	}

	@Override
	protected void fr2IsMoreThanFr1(String[] tokens1, String[] tokens2) {
	}

	@Override
	protected void fr1IsEqualToFr2(String[] tokens1, String[] tokens2) {
		if (tokens1.length <= SNP.SAMPLE_START) {
			System.out.print("[DEBUG] AA is short?? :: " + tokens1.length);
			for (int i = 0; i < tokens1.length; i++) {
				System.out.print("\t" + tokens1[i]);
				System.out.println();
			}
			return;
		}
		String aaGT = tokens1[SNP.SAMPLE_START];
		try {
			// nBABA ? (A genotype is presented as "ref" in tokens1)
			if (aaGT.equals(tokens2[h1Idx]) && tokens2[h2Idx].equals("0"))	nBABA++;
			// nABBA ?
			else if (aaGT.equals(tokens2[h2Idx]) && tokens2[h1Idx].equals("0"))	nABBA++;
			else {
				//System.out.println(tokens2[SNP.CHR] + "\t" + tokens2[SNP.STOP] + "\t" + tokens2[h1Idx] + "\t" + tokens2[h2Idx] + "\t" + aaGT + "\t" + tokens2[SNP.REF]);
				nOthers++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.print("[DEBUG] Present is short?? :: " + h1 + "\t" + h2 + "\t" + tokens2.length);
			for (int i = 0; i < tokens2.length; i++) {
				System.out.print("\t" + tokens2[i]);
			}
			System.out.println();
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	protected void fr1ToTheEnd(String line1) {
	}

	@Override
	protected void fr2ToTheEnd(String line2) {
	}

	@Override
	protected boolean isFr1ProceedToEnd() {
		return false;
	}

	@Override
	protected boolean isFr2ProceedToEnd() {
		return false;
	}

	@Override
	protected void chrLine1(String line) {
	}

	@Override
	protected void chrLine2(String line) {
		String[] tokens = line.split("\\s+");
		for (int i = SNP.SAMPLE_START; i < tokens.length; i++) {
			if (tokens[i].equals(h1)) {
				h1Idx = i;
			}
			if (tokens[i].equals(h2)) {
				h2Idx = i;
			}
		}
		
		if (h1Idx == 0) {
			System.out.println("<H1_Name> : " + h1 + " does not exist. Check your input file.");
			System.exit(-1);
		} else if (h2Idx == 0) {
			System.out.println("<H2_Name> : " + h2 + " does not exist. Check your input file.");
			System.exit(-1);
		}
	}

	public String call() throws Exception {
		// TODO Auto-generated method stub
		return h1 + "\t" + h2 + "\t" + nBABA + "\t" + nABBA + "\t" + d;
	}

}
