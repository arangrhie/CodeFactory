package javax.arang.bed;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Merge10Xbc extends Rwrapper {
	
	private static final int CONTIG_IDX = 0;
	private static final int READ_IDX = 3;
	private static final int BC_IDX = 7;
	private static final int BE_IDX = 11;
	
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		ArrayList<String> contigOrientationBCList = new ArrayList<String>();	// Contig:Orientation
		HashMap<String, Integer> contigOrientationBC2ReadCount = new HashMap<String, Integer>();	// Contig:Orientation counts with unique reads
		HashMap<String, ArrayList<String>> bc2Read = new HashMap<String, ArrayList<String>>();	// to keep track of unique read ids
		
		String contig;
		String prevContig = "";
		String contigOrientationBC = "";
		String readId = "";
		String barcode = "";
		
		ArrayList<String> readIdList;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			barcode = tokens[BC_IDX];
			contig = tokens[CONTIG_IDX];
			if (!prevContig.equals("") && !contig.equals(prevContig)) {
				for (int i = 0; i < contigOrientationBCList.size(); i++) {
					System.out.println(contigOrientationBCList.get(i) + "\t" + contigOrientationBC2ReadCount.get(contigOrientationBCList.get(i)));
				}
				contigOrientationBCList.clear();
				contigOrientationBC2ReadCount.clear();
			}
			contigOrientationBC = contig + ":" + tokens[BE_IDX] + "\t" + barcode;
			readId = tokens[READ_IDX];
			prevContig = contig;
			if (bc2Read.containsKey(barcode)) {
				if (bc2Read.get(barcode).contains(readId)) {
					// do nothing for the same read with different alignments
				} else {
					// new read-id for a known-barcode
					bc2Read.get(barcode).add(readId);
					if (contigOrientationBCList.contains(contigOrientationBC)) {
						contigOrientationBC2ReadCount.put(contigOrientationBC, contigOrientationBC2ReadCount.get(contigOrientationBC)+1);
					} else {
						contigOrientationBCList.add(contigOrientationBC);
						contigOrientationBC2ReadCount.put(contigOrientationBC, 1);
					}
				}
			} else {
				 // new barcode, first read
				readIdList = new ArrayList<String>();
				readIdList.add(readId);
				bc2Read.put(barcode, readIdList);
				if (contigOrientationBCList.contains(contigOrientationBC)) {
					contigOrientationBC2ReadCount.put(contigOrientationBC, contigOrientationBC2ReadCount.get(contigOrientationBC)+1);
				} else {
					contigOrientationBCList.add(contigOrientationBC);
					contigOrientationBC2ReadCount.put(contigOrientationBC, 1);
				}
			}
			
		}
		
		for (int i = 0; i < contigOrientationBCList.size(); i++) {
			System.out.println(contigOrientationBCList.get(i) + "\t" + contigOrientationBC2ReadCount.get(contigOrientationBCList.get(i)));
		}
		contigOrientationBCList.clear();
		contigOrientationBC2ReadCount.clear();
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMerge10Xbc.jar <in.bed>");
		System.out.println("\t<in.bed>: Contig\tStart\tEnd\tReadID\tFlag\tMQ\tCIGAR\tBC\tContig\tStart\tEnd\tB/E");
		System.out.println("\t\tGenerated with");
		System.out.println("awk '{if($2 < 200000) {print $1\"\\t0\\t\"int($2/2)\"\\tB\\n\"$1\"\\t\"int($2/2)\"\\t\"$2\"\\tE\"} "
				+ "else {print $1\"\\t0\\t100000\\tB\\n\"$1\"\\t\"($2-100000)\"\\t\"$2\"\\tE\"}}' asm.contigs.fasta.len_only > asm.contigs.fasta.len_only.BE");
		System.out.println("bedtools intersect -wo -a intermediate.sorted.bc.bed.gz -b asm.contigs.fasta.len_only.BE > intermediate.bc.BE");
		System.out.println("\t<stdout>: Contig:B/E\tBarcode\tNum.Reads");
		System.out.println("Arang Rhie, 2017-04-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Merge10Xbc().go(args[0]);
		} else {
			new Merge10Xbc().printHelp();
		}
	}

}
