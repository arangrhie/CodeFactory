package javax.arang.TenXG;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergedToScaffoldingLinks extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		HashMap<String, ArrayList<String>> bcToContigBEList = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<Integer>> bcToReadCountList = new HashMap<String, ArrayList<Integer>>();
		ArrayList<String> contigBEList;
		ArrayList<Integer> readCountList;
		String contigBE;
		String bc;
		int readCount;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contigBE = tokens[ContigBEMrg.COL_CONTIG_BE];
			bc = tokens[ContigBEMrg.COL_BC];
			readCount = Integer.parseInt(tokens[ContigBEMrg.COL_READ_COUNT]);
			if (readCount == 1) {
				// Remove singleton read alignments
				continue;
			}
			
			if (!bcToContigBEList.containsKey(bc)) {
				// new BC
				contigBEList = new ArrayList<String>();
				readCountList = new ArrayList<Integer>();
				
			} else {
				// known BC
				contigBEList = bcToContigBEList.get(bc);
				readCountList = bcToReadCountList.get(bc);
			}
			
			contigBEList.add(contigBE);
			readCountList.add(readCount);

			bcToContigBEList.put(bc, contigBEList);
			bcToReadCountList.put(bc, readCountList);
		}
		
		System.err.println("[DEBUG] :: " + bcToContigBEList.size() + " barcodes being processed and stored");
		int countNonSingletonBarcodes = 0;
		
		for (String barcode : bcToContigBEList.keySet()) {
			if (bcToContigBEList.get(barcode).size() > 1) {
				contigBEList = bcToContigBEList.get(barcode);
				readCountList = bcToReadCountList.get(barcode);
				for (int i = 0; i < contigBEList.size() - 1; i++) {
					for (int j = 1; j < contigBEList.size(); j++) {
						if (isSorted(contigBEList.get(i), contigBEList.get(j))) {
							System.out.println(contigBEList.get(i) + "\t" + contigBEList.get(j) + "\t" + readCountList.get(i) + "\t" + readCountList.get(j) + "\t" + barcode);
						} else {
							System.out.println(contigBEList.get(j) + "\t" + contigBEList.get(i) + "\t" + readCountList.get(j) + "\t" + readCountList.get(i) + "\t" + barcode);
						}
					}
				}
				countNonSingletonBarcodes++;
			}
		}
		System.err.println("[DEBUG] :: " + countNonSingletonBarcodes + " barcodes have >1 links");
		
	}
	
	private boolean isSorted(String contig1, String contig2) {
		int len = Math.min(contig1.length(), contig2.length());
		for (int i = 0; i < len; i++) {
			if (contig1.charAt(i) > contig2.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar tenXGMergedToScaffoldingLinks.jar <in.bc.BE.mrg>");
		System.out.println("\t<in.bc.BE.mrg>: Generated with bamToBedWi10Xbx.jar and bedMerge10Xbc.jar");
		System.out.println("\t<stdout>: C1:B/E\tC2:B/E\tNo.Reads of C1\tNo.Reads of C2\tBC");
		System.out.println("Arang Rhie, 2017-04-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new MergedToScaffoldingLinks().go(args[0]);
		} else {
			new MergedToScaffoldingLinks().printHelp();
		}
	}

}
