package javax.arang.phasing.snp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class Intersect extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		FileMaker fm1Only = new FileMaker(fr1.getFileName().replace(".snp", "_only.snp"));
		FileMaker fm2Only = new FileMaker(fr2.getFileName().replace(".snp", "_only.snp"));
		FileMaker fmShared = new FileMaker(fr1.getFileName().replace(".snp", "_") + fr2.getFileName());
		
		int fr1OnlyCount = 0;
		int fr2OnlyCount = 0;
		int frSharedCount = 0;
		String line;
		String[] tokens;
		
		HashMap<Integer, String> fr1PosToSNP = new HashMap<Integer, String>();
		ArrayList<Integer> allPosList = new ArrayList<Integer>();
		HashMap<Integer, String> fr2PosToSNP = new HashMap<Integer, String>();
		int pos;
		ArrayList<Integer> sharedPosList = new ArrayList<Integer>();
		int fr1Len = 0;
		int fr2Len = 0;
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			fr1PosToSNP.put(pos, line);
			allPosList.add(pos);
			fr1Len = tokens.length;
		}
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			fr2PosToSNP.put(pos, line);
			if (fr1PosToSNP.containsKey(pos)) {
				sharedPosList.add(pos);
				frSharedCount++;
			} else {
				allPosList.add(pos);
			}
			fr2Len = tokens.length;
		}
		
		Collections.sort(allPosList);
		
	
		fmShared.write("#CHR_1\tPOS_1\tHapA_1\tHapB_1");
		for (int i = 1; i <= (fr1Len - 4); i++) {
			fmShared.write("\tNOTE_" + i);
		}
		fmShared.writeLine("\t#CHR_2\tPOS_2\tHapA_2\tHapB_2\tPS_2");
		for (int i = 0; i < allPosList.size(); i++) {
			pos = allPosList.get(i);
			if (!sharedPosList.contains(pos)) {
				if (fr1PosToSNP.containsKey(pos)) {
					if (writeAll) fmShared.writeLine(fr1PosToSNP.get(pos) + "\t" + padd(fr2Len - 1) );
					fm1Only.writeLine(fr1PosToSNP.get(pos));
					fr1OnlyCount++;
				} else if (fr2PosToSNP.containsKey(pos)) {
					if (writeAll) fmShared.writeLine(padd(fr1Len - 1) + "\t" + fr2PosToSNP.get(pos));
					fm2Only.writeLine(fr2PosToSNP.get(pos));
					fr2OnlyCount++;
				}
			} else {
				fmShared.writeLine(fr1PosToSNP.get(pos) + "\t" + fr2PosToSNP.get(pos));
			}
		}
		
		System.out.println(fr1.getFileName() + " only: " + fr1OnlyCount);
		System.out.println(fr2.getFileName() + " only: " + fr2OnlyCount);
		System.out.println("Shared: " + frSharedCount);
		
		fm1Only.closeMaker();
		fm2Only.closeMaker();
		fmShared.closeMaker();
	}

	private String padd(int count) {
		String padding = "";
		for (int i = 0; i < count; i++) {
			padding += ".\t";
		}
		padding += ".";
		return padding;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSnpIntersect.jar <in1.snp> <in2.snp> [WRITE_ALL]");
		System.out.println("\t<out>: <in1_only.snp>, <in2_only.snp>, <in1_in2.snp>");
		System.out.println("\t<in1_in2.snp>: snps having same position.");
		System.out.println("\t[WRITE_ALL]: Write all the SNPs, including <in1_only.snp> and <in2_only.snp> into <in1_in2.snp>");
		System.out.println("\t\tDEFUALT=false");
		System.out.println("Arang Rhie, 2015-08-12. arrhie@gmail.com");
	}
	
	private static boolean writeAll = false;

	public static void main(String[] args) {
		if (args.length == 2) {
			new Intersect().go(args[0], args[1]);
		} else if (args.length == 3) {
			writeAll = Boolean.parseBoolean(args[2]);
			new Intersect().go(args[0], args[1]);
		} else {
			new Intersect().printHelp();
		}
	}

}
