package javax.arang.bed;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class SimpleLiftover extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		String line;
		String[] tokens;
		
		HashMap<String, String> liftContig = new HashMap<String, String>();
		HashMap<String, Integer> liftPos = new HashMap<String, Integer>();
		
		System.err.println("Reading " + fr2.getFileName() + "...");
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			liftContig.put(tokens[tokens.length - 1], tokens[Bed.CHROM]);
			liftPos.put(tokens[tokens.length - 1], Integer.parseInt(tokens[Bed.START]));
		}
		
		System.err.println(liftContig.size() + " contigs to lift over");
		
		String contig;
		int start;
		int end;
		int lift;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			lift = liftPos.get(contig);
			System.out.print(liftContig.get(contig) + "\t" + (start + lift) + "\t" + (end + lift));
			for (int i = Bed.NOTE; i < tokens.length; i++) {
				System.out.print("\t" + tokens[i]);
			}
			System.out.println();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedSimpleLiftover.jar <target.bed> <new_coords.bed>");
		System.out.println("\tLift over the <target.bed> to the new coordinate space.");
		System.out.println("\tAssumes <target.bed> is contained in <new_coords.bed> last column.");
		System.out.println("\t<target.bed>: CONTIG1\tSTART\tEND\tNOTE(s)");
		System.out.println("\t<new_coords.bed>: generated from fastaConcatinate.jar. CONTIG2\tSTART\tEND\t...\tCONTIG1");
		System.out.println("\t<sysout>: <target.bed> lifted over to CONTIG2 space.");
		System.out.println("\t\tAssumes the orientation are matching that of <new_coords.bed>.");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new SimpleLiftover().go(args[0], args[1]);
		} else {
			new SimpleLiftover().printHelp();
		}
	}

}
