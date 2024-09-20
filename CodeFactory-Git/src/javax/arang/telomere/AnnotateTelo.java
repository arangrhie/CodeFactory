package javax.arang.telomere;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class AnnotateTelo extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String   line;
		String[] tokens;
		
		boolean foundStart = false;
		boolean foundEnd   = false;
				
		String seq = "";
		
		while (fr.hasMoreLines()) {
			
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			// starting with a new sequence?
			if (!seq.equals("") && !tokens[Bed.CHROM].equals(seq)) {
				
				// print
				System.out.println(seq + "\t" + found(foundStart, foundEnd));
				
				// initialize
				foundStart = false;
				foundEnd = false;
				
			}
			seq = tokens[Bed.CHROM];
			if (tokens[Bed.START].equals("0")) foundStart = true;
			if (tokens[Bed.END].equals(tokens[Bed.NOTE])) foundEnd = true;
			
		}
		
		// print last sequence
		System.out.println(seq + "\t" + found(foundStart, foundEnd));
	}

	private String found(boolean start, boolean end) {
		if (start) {
			if (end)    return "Both";
			else        return "Start";
		} else if (end) return "End";
		
		return "Elsewhere";
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar telomereAnnotate.jar in.bed");
		System.err.println();
		System.err.println("Annotate telomeres (start, end) found per Sequence");
		System.err.println("  in.bed    output from seqtk telo");
		System.err.println("            Format: Sequence <tab> Start <tab> End <tab> Size");
		System.err.println("  sysout    Sequence <tab> Found");
		System.err.println("            Found: \'Start\' \'End\' \'Both\' \'None\' or \'Elsewhere\'.");
		System.err.println("            Elsewhere - Start is not 0, End does not matches Size.");
		System.err.println("Arang Rhie. 2023-06-23");
		
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new AnnotateTelo().go(args[0]);
		} else {
			new AnnotateTelo().printHelp();
		}
	}

}
