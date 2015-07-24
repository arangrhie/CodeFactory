package javax.arang.star;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class FindFusionFromChimera extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, String> junctionMap = new HashMap<String, String>();
		HashMap<String, Integer> junctionCount = new HashMap<String, Integer>();
		Vector<String> junctionList = new Vector<String>();
		
		
		String donor;
		String acceptor;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (Junction.isChrM(tokens)) {
				continue;
			}
			donor = tokens[Junction.DONOR_CHR] + "\t" + tokens[Junction.DONOR_BASE];
			acceptor = tokens[Junction.ACCEPTOR_CHR] + "\t" + tokens[Junction.ACCEPTOR_BASE];
			if (junctionMap.containsKey(donor)) {
				junctionCount.put(donor, junctionCount.get(donor)+1);
			}
			if (junctionMap.containsKey(acceptor)) {
				junctionCount.put(acceptor, junctionCount.get(acceptor)+1);
			}
			if (!junctionMap.containsKey(donor) && !junctionMap.containsKey(acceptor)) {
				junctionMap.put(donor, acceptor);
				junctionCount.put(donor, 1);
				junctionList.add(donor);
			}
		}
		
		System.out.println("Total number of unique junctions: " + junctionList.size());
		for (int i = 0; i < junctionList.size(); i++) {
			donor = junctionList.get(i);
			if (junctionCount.get(donor) > numEvidence) {
				fm.writeLine(donor + "\t" + junctionMap.get(donor) + "\t" + junctionCount.get(donor));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar starFindFusionFromChimera.jar <Chimeric.out.junction> [N]");
		System.out.println("\t<Chimeric.out.junction>: --chimSegmentMin option output");
		System.out.println("\t<N>: >= number of evidence reads found are reported. DEFAULT=5");
		System.out.println("\tchrM will be discarded.");
	}

	static int numEvidence = 5;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			numEvidence = Integer.parseInt(args[1]);
			new FindFusionFromChimera().go(args[0], IOUtil.retrieveFileName(args[0]) + "_fusion_" + numEvidence);
		} else if (args.length == 1) {
			new FindFusionFromChimera().go(args[0], IOUtil.retrieveFileName(args[0]) + "_fusion");
		} else {
			new FindFusionFromChimera().printHelp();
		}
	}

}
