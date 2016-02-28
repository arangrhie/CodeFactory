package javax.arang.agp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToStartEndScaffold extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevScaffold = "";
		boolean isFirst = true;
		String scaffold;
		String start = "";
		String end = "";
		int len;
		boolean isBeginning = true;
		String beginContig = "";
		String endContig = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			scaffold = tokens[AGP.OBJ_NAME];
			if (!prevScaffold.equals(scaffold)) {
				// New scaffold object inserted
				if (isFirst) {
					isFirst = false;
				} else {
					// Write out
					len = Integer.parseInt(end.replace(",", "")) - Integer.parseInt(start.replace(",", "")); 
					fm.writeLine(beginContig + "\t" + prevScaffold + "\t" + len);
					if (!beginContig.equals(endContig)) {
						fm.writeLine(endContig + "\t" + prevScaffold + "\t" + len);
					}
					
				}
				isBeginning = true;
			}
			
			if (tokens[AGP.COMPONENT_TYPE].equals("W")) {
				if (isBeginning) {
					isBeginning = false;
					start = tokens[AGP.OBJ_START];
					beginContig = tokens[AGP.COMPONENT_ID];
				}
				
				end = tokens[AGP.OBJ_END];
				endContig = tokens[AGP.COMPONENT_ID];
			}
			prevScaffold = scaffold;
		}
		// Write out
		len = Integer.parseInt(end.replace(",", "")) - Integer.parseInt(start.replace(",", "")); 
		fm.writeLine(beginContig + "\t" + prevScaffold + "\t" + len);
		if (!beginContig.equals(endContig)) {
			fm.writeLine(endContig + "\t" + prevScaffold + "\t" + len);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar agpToStartEndScaffold.jar <in.agp> <out.scaffold>");
		System.out.println("\tConvert BioNano superscaffold .agp into .scaffold");
		System.out.println("\tOnly beginning and ending contigs will be listed, with Ns at the beginning and the end trimmed.");
		System.out.println("\t<out.scaffold>: CONTIG\tScaffold_ID\tLEN");
		System.out.println("Arang Rhie, 2015-09-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToStartEndScaffold().go(args[0], args[1]);
		} else {
			new ToStartEndScaffold().printHelp();
		}
	}

}
