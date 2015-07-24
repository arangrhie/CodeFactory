package javax.arang.bed;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class IdentifySingleWellResidingBacs extends IOwrapper {

	//CHR	START	END	MERGED_FRAGMENTS	LEN	POOL_FRAG_ID	POOL_H	POOL_V	H_V
	static final int LEN = 4;
	static final int POOL_FRAG_ID = 5;
	static final int WELL = 8;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String location;
		String poolFragID;
		String wellID;
		
		HashMap<String, Integer> wellIdCountMap = new HashMap<String, Integer>();
		HashMap<String, Vector<String>> wellPoolFragIdMap = new HashMap<String, Vector<String>>();
		HashMap<String, String> poolFragIdLocMap = new HashMap<String, String>();
		HashMap<String, Vector<String>> poolFragIdWellIdMap = new HashMap<String, Vector<String>>();

		// Read and construct each maps
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			location = tokens[Bed.CHROM] + "\t" + tokens[Bed.START] + "\t" + tokens[Bed.END] + "\t" + tokens[LEN] + "\t" + tokens[POOL_FRAG_ID];

			wellID = tokens[WELL];
			poolFragID = tokens[POOL_FRAG_ID];
			if (!wellIdCountMap.containsKey(wellID)) {
				wellIdCountMap.put(wellID, 1);
				Vector<String> tmpVec = new Vector<String>();
				tmpVec.add(poolFragID);
				wellPoolFragIdMap.put(wellID, tmpVec);
			} else {
				wellIdCountMap.put(wellID, wellIdCountMap.get(wellID) + 1);
				Vector<String> tmpVec = wellPoolFragIdMap.get(wellID);
				tmpVec.add(poolFragID);
				wellPoolFragIdMap.put(wellID, tmpVec);
			}
			
			if (!poolFragIdLocMap.containsKey(poolFragID)) {
				poolFragIdLocMap.put(poolFragID, location);
			} else {
				if (!poolFragIdLocMap.get(poolFragID).equals(location)) {
					String[] locTokens = poolFragIdLocMap.get(poolFragID).split("\t");
					if (Integer.parseInt(tokens[LEN]) > Integer.parseInt(locTokens[3])) {
						System.out.println("[DEBUG] :: Updating " + poolFragID + " from " + poolFragIdLocMap.get(poolFragID) + " to " + location);
						poolFragIdLocMap.put(poolFragID, location);
					}
				}
			}
			
			if (!poolFragIdWellIdMap.containsKey(poolFragID)) {
				Vector<String> tmpVec = new Vector<String>();
				tmpVec.add(wellID);
				poolFragIdWellIdMap.put(poolFragID, tmpVec);
			} else {
				Vector<String> tmpVec = poolFragIdWellIdMap.get(poolFragID);
				if (!tmpVec.contains(wellID)) {
					tmpVec.add(wellID);
					poolFragIdWellIdMap.put(poolFragID, tmpVec);
				}
			}
		}
		
		HashMap<String, Integer> wellIdCycleMap = new HashMap<String, Integer>();
		HashMap<String, String> wellLocMap = new HashMap<String, String>();
		
		Vector<String> singleWellIds = new Vector<String>();
		for (String well : wellIdCountMap.keySet()) {
			if (wellIdCountMap.get(well) == 1) {
				singleWellIds.add(well);
			}
		}
		
		int cycle = 0;
		
		System.out.println();
		System.out.println("[DEBUG] :: poolFragIdLocMap.size() = " + poolFragIdLocMap.size());
		System.out.println("[DEBUG] :: wellPoolFragIdMap.size() = " + wellPoolFragIdMap.size());
		
		boolean hasMoreSingleWells = true;
		while (hasMoreSingleWells) {
			Vector<String> updatedSingleWellIds = new Vector<String>();
			for (String well : singleWellIds) {
				System.out.println("[DEBUG] :: well, cycle, singleWellIds.size() : " + well + "\t" + cycle + "\t" + singleWellIds.size());
				wellIdCycleMap.put(well, cycle);
//				if (well.equals("pool119_pool315")) {
//					System.out.println(well);
//				}
				Vector<String> poolIDs = wellPoolFragIdMap.get(well);
				String poolID = poolIDs.get(0);
				wellLocMap.put(well, poolFragIdLocMap.get(poolID));
				//poolFragIdLocMap.remove(poolID);
				Vector<String> wellsContainingPoolID = poolFragIdWellIdMap.get(poolID);
				for (String well2 : wellsContainingPoolID) {
					if (!well2.equals(well)) {
						// Remove poolID from wellsContainingPoolID's well
						Vector<String> well2PoolIDs = wellPoolFragIdMap.get(well2);
						// leave wells containing single poolID => wells may contain same BACs
						if (well2PoolIDs.size() > 1) {
							well2PoolIDs.remove(poolID);
							int countUpdate = wellIdCountMap.get(well2) - 1;
							wellIdCountMap.put(well2, countUpdate);
							if (countUpdate == 1) {
								updatedSingleWellIds.add(well2);
							}
							wellPoolFragIdMap.put(well2, well2PoolIDs);
						}
					}
				}
			}
			singleWellIds = updatedSingleWellIds;
			cycle++;
			if (singleWellIds.size() == 0) {
				hasMoreSingleWells = false;
			}
		}
		
		System.out.println("[DEBUG] :: Wells containing single BACs = wellLocMap.size() = " + wellLocMap.size());
		System.out.println("[DEBUG] :: Wells containing multiple BACs = poolFragIdLocMap.size() = " + poolFragIdLocMap.size());
		for (String well : wellLocMap.keySet()) {
			fm.writeLine(wellLocMap.get(well) + "\t" + wellIdCycleMap.get(well) + "\t" + 1 + "\t" + well);
		}
		for (String poolID : poolFragIdLocMap.keySet()) {
			Vector<String> wells = poolFragIdWellIdMap.get(poolID);
			for (String well : wells) {
				if (!wellLocMap.keySet().contains(well)) {
					fm.writeLine(poolFragIdLocMap.get(poolID) + "\t" + cycle + "\t" + wells.size() + "\t" + well);
				}
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedIdentifySingleWellResidingBacs.jar <poolAll.bed> <poolAllFiltered.bed>");
		System.out.println("\t<poolAll.bed>: cat pool*.reduced.bed > poolAll.bed (made by bedReduceTwoPooled.jar pool$poolV.bed pool$poolV.reduced.bed)");
		System.out.println("\t\tCHR	START	END	MERGED_FRAGMENTS	LEN	POOL_FRAG_ID	POOL_H	POOL_V	H_V");
		System.out.println("\t<poolAllFiltered.bed>: CHR\tSTART\tEND\tLEN\tPOOL_FRAG_ID\tCycle\tNUM_RESIDING_WELLs\tH_Vs (multiple values are ; seperated)");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new IdentifySingleWellResidingBacs().go(args[0], args[1]);
		} else {
			new IdentifySingleWellResidingBacs().printHelp();
		}
	}

}
