package javax.arang.mashmap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.MergeByDistance;
import javax.arang.bed.Sort;

public class GetRefQryCoverage extends IOwrapper {
	
	private static int DISTANCE = 100;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String qry = "";
		String ref = "";
		double qryLen = 0;
		double refLen = 0;
		double qryCoveredLen = 0;
		double refCoveredLen = 0;
		
		double refStart = 0;
		double refEnd = 0;
		double refRegionStart = 0;
		double refRegionEnd = 0;
		
		MergeByDistance merger = new MergeByDistance();
		merger.setDistance(DISTANCE);
		merger.setOutQuiet();
		
		FileMaker sortedBed = new FileMaker(".", "_tmp.sort");
		
		boolean isFirst = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			// Q_CHR != R_CHR
			if (!ref.equals(tokens[MashMapBed.R_CHR]) || !qry.equals(tokens[MashMapBed.Q_CHR])) {
				if (! isFirst) {
					// get refCoverage
					refCoveredLen += refRegionEnd - refRegionStart;
					System.out.print(ref + "\t" + qry + "\t"
							+ String.format("%.0f", refCoveredLen) + "\t" + String.format("%.1f", (refCoveredLen * 100) / refLen) + "\t");
				
					// sort the stored qry regions
					sortedBed.remove();
					new Sort().go("_tmp", "_tmp.sort");
					merger.go("_tmp.sort");
					qryCoveredLen = merger.getMergedLen();
					System.out.println(String.format("%.0f", qryCoveredLen) + "\t" + String.format("%.1f", (qryCoveredLen * 100) / qryLen));
				}
				isFirst = false;
				
				// initialize variables
				qry = tokens[MashMapBed.Q_CHR];
				ref = tokens[MashMapBed.R_CHR];
				qryLen = Double.parseDouble(tokens[MashMapBed.Q_LEN]);
				refLen = Double.parseDouble(tokens[MashMapBed.R_LEN]);
				refRegionStart = Double.parseDouble(tokens[MashMapBed.R_START]);
				refRegionEnd = Double.parseDouble(tokens[MashMapBed.R_END]);
				refCoveredLen = 0;
				qryCoveredLen = 0;
				
				
				fm.remove();
				fm = new FileMaker(fm.getDir(), "_tmp");
			}
			else
			{
				// Read ref coordinates and merge if distance < DISTANCE
				refStart = Double.parseDouble(tokens[MashMapBed.R_START]);
				refEnd = Double.parseDouble(tokens[MashMapBed.R_END]);

				// overlaps
				if (refStart - refRegionEnd < DISTANCE) {
					refRegionEnd = refEnd;
				}

				// does not overlap
				else {
					refCoveredLen += refRegionEnd - refRegionStart;
					refRegionStart = refStart;
					refRegionEnd = refEnd;
				}
			}
			
			fm.writeLine(qry + "\t" + tokens[MashMapBed.Q_START] + "\t" + tokens[MashMapBed.Q_END]);
			
		}
		
		// get refCoverage
		refCoveredLen += refRegionEnd - refRegionStart;
		System.out.print(ref + "\t" + qry + "\t"
				+ String.format("%.0f", refCoveredLen) + "\t" + String.format("%.1f", (refCoveredLen * 100) / refLen) + "\t");

		// sort the stored qry regions
		sortedBed.remove();
		new Sort().go("_tmp", "_tmp.sort");
		merger.go("_tmp.sort");
		qryCoveredLen = merger.getMergedLen();
		System.out.println(String.format("%.0f", qryCoveredLen) + "\t" + String.format("%.1f", (qryCoveredLen * 100) / qryLen));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: Pipe the ref sorted mashmap results to mashmapGetRefQryCoverage.jar <in.mashmap.bed> [DISTANCE]");
		System.out.println("Assign qry sequence to a reference based on maximum QRY coverage.");
		System.out.println("Assumes the qry is fixed. See bellow for usage.");
		System.out.println("\t<in.mashmap.bed>: Sorted out.map.bed per QRY with following code:");
		System.out.println("\t[DISTANCE]: Merge regions if distance is less than DISTANCE. DEFAULT=100");
		System.out.println();
		System.out.println("\tcat out.map | awk -F \" \" -v name=$name "
				+ "'{print $6\"\\t\"$8\"\\t\"$9+1\"\\t\"$1\"\\t\"$3\"\\t\"$4+1\"\\t\"$1\":\"$3\"-\"$4+1\":\"name\"\\t\"$NF\"\\t\"$5\"\\t\"$7\"\\t\"$2}' > out.map.bed\n"
				+ "\tcut -f4 out.map.bed | sort -u > tmp\n"
				+ "\tfor contig in $(cat tmp)\n"
				+ "\tdo\n"
				+ "\t\tawk -v contig=$contig '$4==contig' out.map.bed | bedtools sort -i - >> out.map.sort\n"
				+ "\tdone");
		System.out.println("\tjava -jar -Xmx1g ~/codes/mashmapGetQryRefCoverage.jar out.map.sort 100 > merged.out");
		System.out.println();
		System.out.println("\tmerged.out: REF\tQRY\tREF_COV\tQRY_QOV");
		System.out.println("Arang Rhie, 2018-12-30. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new GetRefQryCoverage().go(args[0], "_tmp");
		} else if (args.length == 2) {
			DISTANCE = Integer.parseInt(args[1]);
			new GetRefQryCoverage().go(args[0], "_tmp");
		} else {
			new GetRefQryCoverage().printHelp();
		}
	}

}
