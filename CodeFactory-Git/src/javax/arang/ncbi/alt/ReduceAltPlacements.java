package javax.arang.ncbi.alt;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReduceAltPlacements extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevAltScafName = "";
		String altScafName;
		ArrayList<Placement> placementList = new ArrayList<Placement>();
		ArrayList<Placement> mergedList = new ArrayList<Placement>();
		Placement placementToWrite = null;
		Placement prevPlacement;
		Placement placement;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			altScafName = tokens[Placement.ALT_SCAF_NAME];
			if (!prevAltScafName.equals(altScafName)) {
				// Is it first line?
				if (placementList.isEmpty()) {
					placementList.add(new Placement(tokens));
					prevAltScafName = altScafName;
					continue;
				}
				
				
				// Merge list
				prevPlacement = placementList.get(0); 
				if (placementList.size() > 1) {
					for (int i = 1; i < placementList.size(); i++) {
						placement = placementList.get(i);
						if (prevPlacement.isMergableWith(placement)) {
							System.err.println("Merging:");
							System.err.println("[DEBUG]\t" + prevPlacement.getLine());
							System.err.println("[DEBUG]\t" + placement.getLine());
							prevPlacement = prevPlacement.mergeWith(placement);
							System.err.println("To:");
							System.err.println("[DEBUG]\t" + prevPlacement.getLine());
							System.err.println();
						} else {
							mergedList.add(prevPlacement);
							prevPlacement = placement;
						}
					}
				}
				mergedList.add(prevPlacement);
				
				// Choose the best placement
				placementToWrite = mergedList.get(0);
				if (mergedList.size() > 1) {
					for (int i = 1; i < mergedList.size(); i++) {
						if (placementToWrite.getMatchedBases() < mergedList.get(i).getMatchedBases()) {
							placementToWrite = mergedList.get(i);
						}
					}
				}
				
				// Write out results
				fm.writeLine(placementToWrite.getLine());
				
				// Initialize variables
				placementList.clear();
				mergedList.clear();
			}
			
			// Create placement object
			placementList.add(new Placement(tokens));
			
			prevAltScafName = altScafName;
		}
		
		// Run this for the last placements
		if (placementList.size() > 0) {
			// Merge list
			prevPlacement = placementList.get(0); 
			if (placementList.size() > 1) {
				for (int i = 1; i < placementList.size(); i++) {
					placement = placementList.get(i);
					if (prevPlacement.isMergableWith(placement)) {
						System.err.println("Merging:");
						System.err.println("[DEBUT]\t" + prevPlacement.getLine());
						System.err.println("[DEBUT]\t" + placement.getLine());
						prevPlacement = prevPlacement.mergeWith(placement);
						System.err.println("To:");
						System.err.println("[DEBUT]\t" + prevPlacement.getLine());
						System.err.println();
					} else {
						mergedList.add(prevPlacement);
						prevPlacement = placement;
					}
				}
			}
			mergedList.add(prevPlacement);

			// Choose the best placement
			placementToWrite = mergedList.get(0);
			if (mergedList.size() > 1) {
				for (int i = 1; i < mergedList.size(); i++) {
					if (placementToWrite.getMatchedBases() < mergedList.get(i).getMatchedBases()) {
						placementToWrite = mergedList.get(i);
					}
				}
			}

			// Write out results
			fm.writeLine(placementToWrite.getLine());

			// Initialize variables
			placementList.clear();
			mergedList.clear();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ncbiAltReduceAltPlacements.jar <in.placements> <out.reduced.placements>");
		System.out.println("\tSelect the best placement when a single alt is placed to multiple scaffolds.");
		System.out.println("\t<in.placements>: Select lines with >1 alt_scaf_names occuring in the .placements file, sort it with sort -k3");
		System.out.println("\t<out.reduced.placements>: When an alt_scaf_name has several placements on the same parent_name with same orientation, it will be merged.");
		System.out.println("\t\tWhen an alt_scaf_name has several placements on different parent_name, the one with the longest placement will be chosen.");
		System.out.println("Arang Rhie, 2016-10-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ReduceAltPlacements().go(args[0], args[1]);
		} else {
			new ReduceAltPlacements().printHelp();
		}
	}

}
