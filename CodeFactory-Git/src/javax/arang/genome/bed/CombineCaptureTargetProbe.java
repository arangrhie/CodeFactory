/**
 * 
 */
package javax.arang.genome.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class CombineCaptureTargetProbe extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		int CHR = 0;
		int TARGET = 2;
		int START = 3;
		int STOP = 4;
		
		String prevChr = "";
		int prevStart = 0;
		int prevStop = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (line.startsWith("#") || !tokens[TARGET].contains("capture")) {
				continue;
			}
			String chr = tokens[CHR];
			int start = Integer.parseInt(tokens[START]);
			int stop = Integer.parseInt(tokens[STOP]);

			if (prevChr.equals("")) {
				prevChr = chr;
				prevStart = start;
				prevStop = stop;
				continue;
			}
			
			if (!chr.equals(prevChr) || (start - prevStop > offset)) {
				fm.writeLine(prevChr + "\t" + prevStart + "\t" + prevStop);
				prevChr = chr;
				prevStart = start;
			}
			prevStop = stop;
		}
		fm.writeLine(prevChr + "\t" + prevStart + "\t" + prevStop);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedCombineCaptureTargetProbe.jar <Nim provided.gff> [offset=10]");
		System.out.println("\t<Nim provided.gff>: chr\tplatform\tcapture/primary target\tstart\tstop\t...");
		System.out.println("\t<output_offset_capture.bed>: combined probe region");
		System.out.println("\t\t(overlapping, flanking but closer than <10bp are combined as one probe)");
	}

	static int offset = 10;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (0 < args.length && args.length <= 2) {
			if (args.length == 2) {
				offset = Integer.parseInt(args[1]);
			}
			new CombineCaptureTargetProbe().go(args[0], args[0].replace(".gff", "_" + offset + "_capture.bed"));
		} else {
			new CombineCaptureTargetProbe().printHelp();
		}
	}

}
