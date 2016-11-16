package javax.arang.chain;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BedToAltPlacement extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int start;
		int end;
		int len;
		int endTmp;
		int startTail;
		int endTail;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[ChainBed.QUERY_START]);
			end = Integer.parseInt(tokens[ChainBed.QUERY_END]);
			len = Integer.parseInt(tokens[ChainBed.QUERY_SIZE]);
			
			// Change the start end coordinate to match the fasta idx
			if (tokens[ChainBed.QUERY_STRAND].equals("-")) {
				endTmp = len - start;
				start = len - end;
				end = endTmp;
			}
			startTail = start;
			start += 1;	// shift to 1-based
			endTail = len - end;
			fm.writeLine(query + "\t" + target + "\t" + tokens[ChainBed.QUERY] + "\tSCAFFOLD\t" + tokens[ChainBed.TARGET_CHR] + "\t"
					+ tokens[ChainBed.QUERY_STRAND] + "\t"
					+ start + "\t" + end + "\t" + (Integer.parseInt(tokens[ChainBed.TARGET_START])+1) + "\t" + tokens[ChainBed.TARGET_END] + "\t"
					+ startTail + "\t" + endTail);
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainBedToAltPlacement.jar <in.overchain.bed> <out.alt_placement.txt> [QUERY] [TARGET]");
		System.out.println("\t<in.overchain.bed>: generated with chainToBed.jar (with no -block option given)");
		System.out.println("\t<out.alt_placement.txt>: QUERY\tTARGET\t<QUERY name>\tSCAFFOLD\t<TARGET name>\t"
							+ "<ori>\t<alt_start>\t<alt_end>\t<parent_start>\t<parent_end>\t<alt_start_tail>\t<alt_stop_tail>");
		System.out.println("\t[QUERY]: DEFAULT=HaplotigB");
		System.out.println("\t[TARGET]: DEFAULT=HaplotigA");
		System.out.println("\t*This tool is for NCBI WGS submission.");
		System.out.println("Arang Rhie, 2016-09-30. arrhie@gmail.com");
	}
	
	private static String query = "HaplotigB";
	private static String target = "HaplotigA";

	public static void main(String[] args) {
		if (args.length == 2) {
			new BedToAltPlacement().go(args[0], args[1]);
		} else if (args.length == 4) {
			query = args[2];
			target = args[3];
			new BedToAltPlacement().go(args[0], args[1]);
		} else {
			new BedToAltPlacement().printHelp();
		}
	}

}
