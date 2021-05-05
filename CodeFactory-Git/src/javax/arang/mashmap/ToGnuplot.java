package javax.arang.mashmap;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToGnuplot extends R2wrapper {

	private static final short REF = 0;
	private static final short SIZE = 1;
	private static boolean printOffset = false;
	private static boolean flip = false;
	
	@Override
	public void hooker(FileReader fRefSizes, FileReader fMashMap) {
		
		HashMap<String, Double> refOffset = new HashMap<String, Double>();
		Vector<String> order = new Vector<String>();
		
		String line;
		String[] tokens;
		String ref;
		Double size;
		Double offset = 0d;
		StringBuffer ticks = new StringBuffer("");
		
		// Store ref size
		while (fRefSizes.hasMoreLines()) {
			tokens = fRefSizes.readLine().split(RegExp.WHITESPACE);
			ref = tokens[REF];
			size = Double.parseDouble(tokens[SIZE]);
			order.add(ref);
			refOffset.put(ref, offset);
			ticks.append("\"" + ref + "\" " + offset + ", ");
			offset += size;
		}
		
		if (printOffset) {
			FileMaker fmRefOffset = new FileMaker(fRefSizes.getFileName() + ".offset");
			printTicks(fmRefOffset, ticks);
			fmRefOffset.closeMaker();
		}
		// Get offsets for each start / end positions
		Double offsetRef = 0d;
		Double offsetQry = 0d;
		Double rStart;
		Double rEnd;
		Double qStart;
		Double qEnd;
		
		// Output mashmap hits in gnu input style
		while (fMashMap.hasMoreLines()) {
			line = fMashMap.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			if (tokens.length != 10) {
				System.err.println(line);
			}
			
			if (! refOffset.containsKey(tokens[MashMap.R_CHR]) || ! refOffset.containsKey(tokens[MashMap.Q_CHR])) {
				continue;
			}
			offsetRef = refOffset.get(tokens[MashMap.R_CHR]);
			offsetQry = refOffset.get(tokens[MashMap.Q_CHR]);

			// forward
			if (tokens[MashMap.Q_STRAND].equals("+")) {
				rStart = offsetRef + Double.parseDouble(tokens[MashMap.R_START]);
				rEnd = offsetRef + Double.parseDouble(tokens[MashMap.R_END]);
				qStart = offsetQry + Double.parseDouble(tokens[MashMap.Q_START]);
				qEnd = offsetQry + Double.parseDouble(tokens[MashMap.Q_END]);
			} else {
				rStart = offsetRef + Double.parseDouble(tokens[MashMap.R_START]);
				rEnd = offsetRef + Double.parseDouble(tokens[MashMap.R_END]);
				qStart = offsetQry + Double.parseDouble(tokens[MashMap.Q_END]);
				qEnd = offsetQry + Double.parseDouble(tokens[MashMap.Q_START]);
			}
			printGnustyle(rStart, rEnd, qStart, qEnd);
		}
		
	}
	
	private void printTicks(FileMaker fmOut, StringBuffer ticks) {
		String gnuTicks = ticks.substring(0, ticks.length() - 2);	// Remove the trailing ", "
		gnuTicks = gnuTicks.replace("_", ".");	// replace all '_' to '.'
		fmOut.writeLine("set xtics (" + gnuTicks + ")");
		fmOut.writeLine("set ytics (" + gnuTicks + ")");
	}

	private void printGnustyle(Double r_start, Double r_end, Double q_start, Double q_end) {
		if (flip && r_start > q_start) {
			Double tmp = q_start;
			q_start = r_start;
			r_start = tmp;
			
			tmp = q_end;
			q_end = r_end;
			r_end = tmp;
		}
		System.out.println(String.format("%.0f", r_start) + " " + String.format("%.0f", q_start));
		System.out.println(String.format("%.0f", r_end) + " " + String.format("%.0f", q_end));
		System.out.println();
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar mashmapToGnuplot.jar <ref.ordered.sizes> <mashmap.out> [--printOffset] [--flip]");
		System.out.println("\t<ref.ordered.sizes>: Order to display, with sizes.");
		System.out.println("\t\tExample: ref1 100000");
		System.out.println("\t\t         ref3 80000");
		System.out.println("\t\t         ref2 50000");
		System.out.println("\t\twill display results for ref1, ref2, ref3.");
		System.out.println("\t<mashmap.out>: ");
		System.out.println("\t[--printOffset]: Set to generate ticks in gnuplot language.");
		System.out.println("\t\tRun only once if not already generated.");
		System.out.println("\t[--flip]: Set to flip coordinates and show data to the upper triangle only.");
		System.out.println("Arang Rhie, 2020-04-30. arrhie@gmail.com");
		
	}

	public static void main(String[] args) {
		if (args.length > 2) {
			for (int i=2; i < args.length; i++) {
				if (args[i].endsWith("printOffset")) printOffset = true;
				if (args[i].endsWith("flip"))	flip = true;
			}
		}
		if (args.length >= 2) {
			new ToGnuplot().go(args[0], args[1]);
		} else {
			new ToGnuplot().printHelp();
		}

	}

}
