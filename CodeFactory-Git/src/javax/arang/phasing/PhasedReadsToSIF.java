package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedRead;

public class PhasedReadsToSIF extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		String alleles;
		char allele;
		String pos;
		String node1;
		String node2;
		String edgeKey;
		
		int alleleIdx;
		int i;
		
		HashMap<String, Integer> edges = new HashMap<String, Integer>();	// allele1.pos1-allele2.pos2	num.reads
		ArrayList<String> edgeKeyList = new ArrayList<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			alleles = tokens[PhasedRead.HAPLOTYPE];
			alleleIdx = 0;
			i = PhasedRead.SNP_POS_LIST;
			allele = alleles.charAt(alleleIdx);
			pos = tokens[i];
			node1 = allele + "." + pos;
			
			for (i = PhasedRead.SNP_POS_LIST + 1; i < tokens.length; i++) {
				alleleIdx++;
				allele = alleles.charAt(alleleIdx);
				pos = tokens[i];
				node2 = allele + "." + pos;
				edgeKey = node1 + "-" + node2;
				if (!edges.containsKey(edgeKey)) {
					edges.put(edgeKey, 1);
					edgeKeyList.add(edgeKey);
				} else {
					edges.put(edgeKey, edges.get(edgeKey) + 1);
				}
				node1 = node2;
			}
		}
		
		for (i = 0; i < edgeKeyList.size(); i++) {
			edgeKey = edgeKeyList.get(i);
			tokens = edgeKey.split("-");
			fm.writeLine(tokens[0] + "\t" + edges.get(edgeKey) + "\t" + tokens[1]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedReadsToSIF.jar <in.read> <out.sif>");
		System.out.println("\t<in.read>: Generated .read file with phasingSubreadBasedPhasing.jar");
		System.out.println("\t<out.sif>: Cytoscape input format");
		System.out.println("\t\t<Apos>\tAA(num.reads)\t<Bpos>");
		System.out.println("\t\t<Bpos>\tBB(num.reads)\t<Bpos>");
		System.out.println("\t\t<Apos>\tAB(num.reads)\t<Bpos>");
		System.out.println("\t\t<Opos>\t...");
		System.out.println("\tReduces the edges (reads) into one edge, with the total number of reads (reduced edges)");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new PhasedReadsToSIF().go(args[0], args[1]);
		} else {
			new PhasedReadsToSIF().printHelp();
		}
	}

}
