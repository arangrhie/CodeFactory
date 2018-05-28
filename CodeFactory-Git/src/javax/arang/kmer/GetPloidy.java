package javax.arang.kmer;

import java.util.ArrayList;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetPloidy extends R2wrapper {

	private static String histFilePath;
	private static int k = 21;
	private ArrayList<Integer> ploidyDepth = null;
	private ArrayList<Integer> ploidyBound = null;
	private KmerCountQryTable kmerCntQryTable = null;
	private int estimatedEffectiveCov = 0;
	private int repeatPloidy = Integer.MAX_VALUE;
	
	@Override
	public void hooker(FileReader frKmerCount, FileReader frAsmFasta) {
		// read kmerHist to get ploidy bounaries
		FileReader frHist = new FileReader(histFilePath);
		HistToPloidyDepth ploidyTable = new HistToPloidyDepth();

		ploidyDepth = ploidyTable.getPloidyDepthTable(frHist);
		ploidyBound = ploidyTable.getPloidyBoundary();
		frHist.closeReader();
		
		estimatedEffectiveCov = ploidyTable.getEffectiveMinDepth();
		int maxBound = ploidyBound.get(ploidyBound.size() - 1);
		ploidyTable.printErrPloidyBounds();
		repeatPloidy = ploidyDepth.size() + 1;
		
		String line;
		String[] tokens;
		String kmer;
		int count;
		
		// load kmer table
		kmerCntQryTable = new KmerCountQryTable(k);
		while (frKmerCount.hasMoreLines()) {
			line = frKmerCount.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			kmer = tokens[0];
			count = Integer.parseInt(tokens[1]);
			if (count > estimatedEffectiveCov && count < maxBound) {
				kmerCntQryTable.addTable(kmer, count);
			}
		}
		System.err.println(kmerCntQryTable.getTableSize() + " kmers Loaded.");
		
		// read contig fasta
		String contig = null;
		StringBuffer contigSeq = null;
		while (frAsmFasta.hasMoreLines()) {
			line = frAsmFasta.readLine();
			if (line.startsWith(">")) {
				// print kmers
				if (contig != null) {
					printPloidy(contig, contigSeq);
				}
				contig = line.substring(1);
				contigSeq = new StringBuffer();
				continue;
			}
			contigSeq.append(line);

		}
		if (contig != null) {
			printPloidy(contig, contigSeq);
		}
		
		System.err.println("Ploidy 0: No kmers found in " + frKmerCount.getFileName());
		System.err.println("Ploidy " + repeatPloidy + ": Any kmer found with count > " + ploidyBound.get(ploidyBound.size() - 1) + " is considered as repeat");
	}
	
	public void printPloidy(String contig, StringBuffer contigSeq) {
		long seqLen = Integer.toUnsignedLong(contigSeq.length());
		int count;
		boolean isInBound = false;
		int start = 0;
		int end = 0;
		int prevPloidy = 0;
		int ploidy = 0;
		boolean isFirstCheck = true;
		for (int i = 0; i + k <= seqLen; i++) {
			count = kmerCntQryTable.queryTableCount(contigSeq.substring(i, i+k));
			if (isFirstCheck) {
				start = i;
				end = i + k;
			}
			// System.err.print(contig + "\t" + start + "\t" + end + "\t" + count);
			if (count > 0) {
				if (count > estimatedEffectiveCov) {
					isInBound = false;
					PLOIDY_CHECK : for (int ploidyIdx = 0; ploidyIdx < ploidyDepth.size(); ploidyIdx++) {
						if (count <= ploidyBound.get(ploidyIdx)) {
							ploidy = ploidyIdx + 1;
							// System.err.print("\t" + ploidy);
							isInBound = true;
							break PLOIDY_CHECK;
						}
					}
					if (!isInBound) {
						//System.err.print("\t" + repeatPloidy);	
						// TODO : extend the poissonDist to infinite ploidy
						ploidy = repeatPloidy;
					}
				}
			} else {
				//System.err.print("\t0");
				ploidy = 0;
			}
			//System.err.println();
			if (isFirstCheck) {
				prevPloidy = ploidy;
			}
			
			if (prevPloidy != ploidy) {
				// write out here and initialize
				System.out.println(contig + "\t" + start + "\t" + end + "\t" + prevPloidy);
				start = i;
			}
			end = i + k;
			prevPloidy = ploidy;
			isFirstCheck = false;
		}
		System.out.println(contig + "\t" + start + "\t" + end + "\t" + prevPloidy);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerGetPloidy.jar <kmer.count> <kmer.hist> <contig.fasta> [k=21]");
		System.out.println("\tReport estimated ploidy from the given kmer count per region.");
		System.out.println("\t<kmer.count>: kmer count of reads, preferably illumina reads. Accepts format: kmer count");
		System.out.println("\t<kmer.hist>: jellyfish or meryl kmer histogram.");
		System.out.println("\t<contig.fasta>: assembly.");
		System.out.println("\t<stdout>: bed format of <contig.fasta>, with regions annotated with estimated ploidy");
		System.out.println("\t\tCONTIG(or scaffold)\tSTART\tEND\tPLOIDY");
		System.out.println("\t\tPloidy 0 : no kmers found in the given kmer.count");
		System.out.println("\t\tPloidy MAX: any kmer beyond the boundary is considered as repeat");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			histFilePath = args[1];
			new GetPloidy().go(args[0], args[2]);
		} else if (args.length == 4) {
			histFilePath = args[1];
			k = Integer.parseInt(args[3]);
			new GetPloidy().go(args[0], args[2]);
		} else {
			new GetPloidy().printHelp();
		}
	}

}
