package javax.arang.phasing.snp;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.bed.util.Region;
import javax.arang.phasing.util.PhasedSNP;

public class RemoveSNPsInRegion extends I2Owrapper {

	@Override
	public void hooker(FileReader frSNP, FileReader frBed, FileMaker fm) {
		String line;
		String[] tokens;
		
		ArrayList<Integer> startList = new ArrayList<Integer>();
		HashMap<Integer, Integer> startToEnd = new HashMap<Integer, Integer>();
		int start;
		int end;
		while (frBed.hasMoreLines()) {
			line = frBed.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]) + 1;
			end = Integer.parseInt(tokens[Bed.END]);
			startList.add(start);
			startToEnd.put(start, end);
		}
		
		int pos;
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			if (!Region.isInRegion(pos, startList, startToEnd)) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSnpRemoveSNPsInRegion.jar <in.phased.snp> <in.bed> <out.phased.snp>");
		System.out.println("\t<in.phased.snp>: Any snp with CHR\tPOS, by chromosome.");
		System.out.println("\t<in.bed>: for phasing, put the deletion regions called with any SV caller.");
		System.out.println("\t\tBut this tool works for any purpose, to delete sites falling in <in.bed>.");
		System.out.println("\t<out.phased.snp>: <in.phased.snp> with no sites falling in <in.bed>.");
		System.out.println("Arang Rhie, 2015-09-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new RemoveSNPsInRegion().go(args[0], args[1], args[2]);
		} else {
			new RemoveSNPsInRegion().printHelp();
		}
	}

}
