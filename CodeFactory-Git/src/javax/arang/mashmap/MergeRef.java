package javax.arang.mashmap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.MergeByDistance;
import javax.arang.bed.Sort;

public class MergeRef extends Rwrapper{

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String ref = "";
		String qry = "";
		
		double refStart = 0;
		double refEnd = 0;
		double end;
		
		boolean isFirst = true;
		
		FileMaker fmTmpF = new FileMaker(".", "_forward");
		FileMaker fmSortF = new FileMaker(".", "_forward.sort");
		
		FileMaker fmTmpR = new FileMaker(".", "_reverse");
		FileMaker fmSortR = new FileMaker(".", "_reverse.sort");
		
		MergeByDistance merger = new MergeByDistance();
		merger.setDistance(100);
		merger.setOutQuiet();

		double qryCoveredFowardLen = 0;
		double qryCoveredReverseLen = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (!ref.equals(tokens[MashMapBed.R_CHR]) || !qry.equals(tokens[MashMapBed.Q_CHR])) {
				if (!isFirst) {
					// write down results
					fmSortF.remove();
					fmSortR.remove();
					new Sort().go("_forward", "_forward.sort");
					new Sort().go("_reverse", "_reverse.sort");
					merger.go("_forward.sort");
					qryCoveredFowardLen = merger.getMergedLen();
					
					merger.go("_reverse.sort");
					qryCoveredReverseLen = merger.getMergedLen();
					
					System.out.print(ref + "\t" + String.format("%.0f", refStart) + "\t" + String.format("%.0f", refEnd) + "\t" + qry + "\t");
					if (qryCoveredFowardLen < qryCoveredReverseLen) {
						System.out.println("-");
					} else {
						System.out.println("+");
					}
					
				}
				isFirst = false;
				
				// initialize
				ref = tokens[MashMapBed.R_CHR];
				qry = tokens[MashMapBed.Q_CHR];
				refStart = Double.parseDouble(tokens[MashMapBed.R_START]);
				refEnd = Double.parseDouble(tokens[MashMapBed.R_END]);
				fmTmpF.remove();
				fmTmpR.remove();
				fmTmpF = new FileMaker(".", "_forward");
				fmTmpR = new FileMaker(".", "_reverse");
			}
			
			// collect ref coords: We are assuming the input is sorted by reference, so just add up the refEnd.
			end = Double.parseDouble(tokens[MashMapBed.R_END]);
			if (refEnd < end) {
				refEnd = end;
			}
			
			// get QRY region to sort
			if (tokens[MashMapBed.Q_STRAND].equals("+")) {
				fmTmpF.writeLine(qry + "\t" + tokens[MashMapBed.Q_START] + "\t" + tokens[MashMapBed.Q_END]); 
			} else {
				fmTmpR.writeLine(qry + "\t" + tokens[MashMapBed.Q_START] + "\t" + tokens[MashMapBed.Q_END]); 
			}
		}
		
		// write down results
		fmSortF.remove();
		fmSortR.remove();
		new Sort().go("_forward", "_forward.sort");
		new Sort().go("_reverse", "_reverse.sort");
		merger.go("_forward.sort");
		qryCoveredFowardLen = merger.getMergedLen();
		
		merger.go("_reverse.sort");
		qryCoveredReverseLen = merger.getMergedLen();
		
		System.out.print(ref + "\t" + String.format("%.0f", refStart) + "\t" + String.format("%.0f", refEnd) + "\t" + qry + "\t");
		if (qryCoveredFowardLen < qryCoveredReverseLen) {
			System.out.println("-");
		} else {
			System.out.println("+");
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mashmapMergeRef.jar <out.map.sort>");
		System.out.println("Merge reference coordinates, min start to max end, and get the major strand.");
		System.out.println("This script simply helps to assign QRY sequence and orientation.");
		System.out.println("Get the QRY sequence filtered.");
		System.out.println("Example:");
		System.out.println("\tcat out.map | awk -F \" \" -v name=$name "
				+ "'{print $6\"\\t\"$8\"\\t\"$9+1\"\\t\"$1\"\\t\"$3\"\\t\"$4+1\"\\t\"$1\":\"$3\"-\"$4+1\":\"name\"\\t\"$NF\"\\t\"$5\"\\t\"$7\"\\t\"$2}' > out.map.bed\n"
				+ "\tcut -f4 out.map.bed | sort -u > tmp\n"
				+ "\tfor contig in $(cat tmp)\n"
				+ "\tdo\n"
				+ "\t\tawk -v contig=$contig '$4==contig' out.map.bed | bedtools sort -i - >> out.map.sort\n"
				+ "\tdone");
		System.out.println("\t"
				+ "\tfor contig in $(cat merged.out | cut -f2 | sort -u);\n"
				+ "\tdo\n"
				+ "\tawk -v contig=$contig '$2==contig' merged.out | sort -k6,6 -gr | head -n1 >> merged.out.top;\n"
				+ "\tdone");
		System.out.println();
		System.out.println("Arang Rhie, 2018-12-31. arrhie@gmail.com");
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			new MergeRef().go(args[0]);
		} else {
			new MergeRef().printHelp();
		}
	}

}
