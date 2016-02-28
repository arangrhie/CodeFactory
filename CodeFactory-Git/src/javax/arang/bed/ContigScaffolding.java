package javax.arang.bed;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ContigScaffolding extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, Integer> contigToScaffoldId = new HashMap<String, Integer>();
		HashMap<String, Integer> bacToScaffoldId = new HashMap<String, Integer>();
		HashMap<Integer, ArrayList<String>> scaffoldToContigs = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, ArrayList<String>> scaffoldToBacs = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, Integer> scaffoldToLen = new HashMap<Integer, Integer>();
		
		String contig;
		String bac;
		int len;
		int scaffoldId = 1;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			bac = tokens[Bed.NOTE];
			len = Integer.parseInt(tokens[Bed.NOTE + 5]);
			if (!contigToScaffoldId.containsKey(contig) && !bacToScaffoldId.containsKey(bac)) {
				// Contig is first presented, Bac is first presented
				contigToScaffoldId.put(contig, scaffoldId);
				bacToScaffoldId.put(bac, scaffoldId);
				ArrayList<String> contigList = new ArrayList<String>();
				contigList.add(contig);
				scaffoldToContigs.put(scaffoldId, contigList);
				ArrayList<String> bacList = new ArrayList<String>();
				bacList.add(bac);
				scaffoldToBacs.put(scaffoldId, bacList);
				scaffoldToLen.put(scaffoldId, len);
				scaffoldId++;
			} else if (contigToScaffoldId.containsKey(contig) && !bacToScaffoldId.containsKey(bac)){
				int id = contigToScaffoldId.get(contig);
				bacToScaffoldId.put(bac, id);
				ArrayList<String> bacList = scaffoldToBacs.get(id);
				bacList.add(bac);
			} else if (!contigToScaffoldId.containsKey(contig) && bacToScaffoldId.containsKey(bac)) {
				int id = bacToScaffoldId.get(bac);
				contigToScaffoldId.put(contig, id);
				ArrayList<String> contigList = scaffoldToContigs.get(id);
				contigList.add(contig);
				scaffoldToLen.put(id, scaffoldToLen.get(id) + len);
			} else {
				int contigId = contigToScaffoldId.get(contig);
				int bacId = bacToScaffoldId.get(bac);
				if (contigId != bacId) {
					// Merge to contigId: move contigs and bacs
					ArrayList<String> contigContigList = scaffoldToContigs.get(contigId);
					ArrayList<String> contigBacList = scaffoldToBacs.get(contigId);
					ArrayList<String> bacContigList = scaffoldToContigs.get(bacId);
					ArrayList<String> bacBacList = scaffoldToBacs.get(bacId);
					contigContigList.addAll(bacContigList);
					contigBacList.addAll(bacBacList);
					scaffoldToLen.put(contigId, scaffoldToLen.get(contigId) + scaffoldToLen.get(bacId));
					
					// move id from bacId to contigId
					for (String contigToMove : scaffoldToContigs.get(bacId)) {
						contigToScaffoldId.put(contigToMove, contigId);
					}
					
					for (String bacsToMove : scaffoldToBacs.get(bacId)) {
						bacToScaffoldId.put(bacsToMove, contigId);
					}
					
					scaffoldToContigs.remove(bacId);
					scaffoldToBacs.remove(bacId);
					scaffoldToLen.remove(bacId);
				}	// else do nothing
			}
		}
		
		System.out.println("Num. contigs: " + scaffoldToLen.size());
		
		int id;
		ArrayList<String> bacList;
		for (String contigToWrite : contigToScaffoldId.keySet()) {
			id = contigToScaffoldId.get(contigToWrite);
			fm.write(contigToWrite + "\t" + id + "\t" + scaffoldToLen.get(id) + "\t");
			// write BAC-IDs
			bacList = scaffoldToBacs.get(id);
			for (int i = 0; i < bacList.size(); i++) {
				fm.write(bacList.get(i) + ",");
			}
			fm.writeLine();
		}
		
		FileMaker fmScaffolds = new FileMaker(fm.getDir(), fm.getFileName() + ".unique");
		for (Integer scaffold : scaffoldToLen.keySet()) {
			fmScaffolds.writeLine(scaffold + "\t" + scaffoldToLen.get(scaffold));
		}
		fmScaffolds.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedContigScaffolding.jar <in.bed> <out.scaffolds>");
		System.out.println("\t<in.bed>: CONTIG\tSTART\tEND\tBAC-ID\t...");
		System.out.println("\t<out.scaffold>: CONTIG\tScaffold-ID\tScaffold-Simple-Sum-Len\tUSED_BACS");
		System.out.println("Arang Rhie, 2015-09-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ContigScaffolding().go(args[0], args[1]);
		} else {
			new ContigScaffolding().printHelp();
		}
	}

}
