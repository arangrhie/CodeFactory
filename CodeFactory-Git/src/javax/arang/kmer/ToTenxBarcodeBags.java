package javax.arang.kmer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;

public class ToTenxBarcodeBags extends R2wrapper {

	private static String pathR2;
	@Override
	public void hooker(FileReader frKmerList, FileReader frR1) {
		// read frKmerList and store to kmerQueryTable
		String line;
		KmerQryTable kmerQryTable = null;
		line = frKmerList.readLine();
		if (kmerQryTable == null) {
			kmerQryTable = new KmerQryTable(line.length());
		}
		frKmerList.reset();
		kmerQryTable.readKmerFile(frKmerList);
		System.err.println(kmerQryTable.getTablePrintableSize() + " kmers loaded to table");
		
		// read frR1 and frR2 to query & bag by barcodes
		FileReader frR2 = new FileReader(pathR2);
		
		int lineNum = 0;
		String bc;
		
		while (frR1.hasMoreLines()) {
			line = frR1.readLine();
			lineNum++;
			if (lineNum %4 == 2) {
				bc = line.substring(0, 24);
			}
			
			if (lineNum %4 == 0) {
				lineNum = 0;
			}
		}
		
		// report num. kmers found in each barcodes: BARCODE\tNUM.KMERS
		
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerToTenxBarcodeBags.jar <kmer.list> <10x_R1.fastq> <10x_R2.fastq>");
		System.out.println("\tPrint num. barcodes containing the <kmer.list>");
		System.out.println("\tPut kmers from the same barcodes into bags");
		System.out.println("\tIteratively, look for 'mergeable' bags by reporting ovelapping kmers among bags");
		System.out.println("*Experimental.");
		System.out.println("Arang Rhie, 2017-10-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			pathR2 = args[2];
			new ToTenxBarcodeBags().go(args[0], args[1]);
		} else {
			new ToTenxBarcodeBags().printHelp();
		}
	}

}
