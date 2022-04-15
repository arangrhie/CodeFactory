package javax.arang.falcon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BlockBedContigPath extends I2Owrapper {

	private static final short CONTIG_FROM = 0;
	private static final short CONTIG_NEXT = 1;
	
	@Override
	public void hooker(FileReader frContigOrder, FileReader frSharedPreads, FileMaker fm) {
		String line;
		String[] tokens;
		
		frContigOrder.readLine();	// header line
		
		String[] contigs = new String[2];
		contigs[CONTIG_FROM] = "";
		String contig;
		int contigOrderIdx = 0;
		ArrayList<String[]> contigOrder = new ArrayList<String[]>(); // <Contig, Next_Contig>
		HashMap<String, ArrayList<Integer>> contigIdxMap = new HashMap<String, ArrayList<Integer>>();	// <Contig, idx list where contig exists>
		ArrayList<Integer> idxList = new ArrayList<Integer>();	// idx list that will be put into contigIdxMap. contig : pair contig idx = 1 : N
		HashMap<Integer, String> idxOut = new HashMap<Integer, String>();
		
		while (frContigOrder.hasMoreLines()) {
			line = frContigOrder.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[0].contains(":")) {
				contig = tokens[0].split(":")[0];
			} else {
				contig = tokens[0];
			}
			contigs[CONTIG_NEXT] = contig;
			contigOrder.add(contigs);
			contigs = new String[2];
			contigs[CONTIG_FROM] = contig;
			idxOut.put(contigOrderIdx, line);
			contigOrderIdx++;
			if (!contigIdxMap.containsKey(contig)) {
				idxList = new ArrayList<Integer>();
				contigIdxMap.put(contig, idxList);
			} else {
				idxList = contigIdxMap.get(contig);
			}
			idxList.add(contigOrderIdx);
		}
		
		for (String[] contigsInSuperScaffolds : contigOrder) {
			System.out.println(contigsInSuperScaffolds[CONTIG_FROM] + "\t" + contigsInSuperScaffolds[CONTIG_NEXT] + "\t" + contigIdxMap.get(contigsInSuperScaffolds[CONTIG_FROM]));
		}
		
		line = frSharedPreads.readLine();	// header line
		
		ArrayList<String> contigList;
		StringBuffer preadInfo = new StringBuffer();
		while (frSharedPreads.hasMoreLines()) {
			line = frSharedPreads.readLine().trim();
			tokens = line.split(RegExp.TAB);
			preadInfo = new StringBuffer(tokens[0]);
			contigList = new ArrayList<String>();

			// Collect contigs
			for (int i = 1; i < tokens.length; i+=3) {
				contig = tokens[i];
				contigList.add(contig);
			}
			
			// Check if pair exists in collected contigs
			for (String ctg : contigList) {
				if (contigIdxMap.containsKey(ctg)) {
					idxList = contigIdxMap.get(ctg);	// idx list where contig exists
					for (int idx : idxList) {
						// Does next contig pair also exists in contigList? 
						if (contigList.contains(contigOrder.get(idx)[CONTIG_NEXT])) {
							int i = contigList.indexOf(ctg) * 3 + 1;
							preadInfo.append("\t" + tokens[i] + ":" + tokens[i + 1] + tokens[i + 2]);
							i = contigList.indexOf(contigOrder.get(idx)[CONTIG_NEXT]) * 3 + 1;
							preadInfo.append("\t" + tokens[i] + ":" + tokens[i + 1] + tokens[i + 2]);
							idxOut.put(idx, idxOut.get(idx) + "\t" + preadInfo.toString());
						}
					}
				}
			}
		}

		for (contigOrderIdx = 0; contigOrderIdx < contigOrder.size(); contigOrderIdx++) {
			fm.writeLine(idxOut.get(contigOrderIdx));
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconBlockBedContigPath.jar <contig_order.tds> <shared_preads.tds> <out.tds>");
		System.out.println("\t<contig_order.tds>: From SuperContigs_AGP, format: <contig> <orientation>");
		System.out.println("\t<shared_preads.tds>: From Branching, format: pread	contig1	Pos	F/R	contig2	Pos	F/R ...");
		System.out.println("\t<out.tds>: <contig1> <orientation> <pread> <evidence from shared_preads.tds>");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new BlockBedContigPath().go(args[0], args[1], args[2]);
		} else {
			new BlockBedContigPath().printHelp();
		}
	}

}
