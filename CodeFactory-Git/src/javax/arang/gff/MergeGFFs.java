package javax.arang.gff;

import java.util.ArrayList;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeGFFs extends INwrapper {

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		
		String   line;
		String[] tokens;
		int      inFile;
		int      startL;
		int      endL;
		String   gene;
		String   biotype;
		
		// in GFF3
		int      start;
		int      end;
		String   type;
		
		// Iterate through the chose_from.bed file
		FileReader choseFromFr = frs.get(0);
		FileReader gffFr;
		while (choseFromFr.hasMoreLines()) {
			line = choseFromFr.readLine();
			tokens = line.split(RegExp.TAB);
			startL = Integer.parseInt(tokens[Bed.START]);
			endL   = Integer.parseInt(tokens[Bed.END]);
			gene   = tokens[3];
			biotype = tokens[5];
			inFile = Integer.parseInt(tokens[tokens.length - 1]);
			System.err.println("[[ DEBUG ]] :: Processing " + line);

			// Look for the gene line
			gffFr = frs.get(inFile);
			line = gffFr.readLine();

			// skip header
			while (line.startsWith("#")) line = gffFr.readLine();
			tokens = line.split(RegExp.TAB);

			// skip if we aren't at the gene of interest
			type = tokens[GFF.TYPE];
			// we know the first type is "gene"
			start = Integer.parseInt(tokens[GFF.START]);
			end = Integer.parseInt(tokens[GFF.END]);

			while (gffFr.hasMoreLines() && start < startL) {
				line = gffFr.readLine();
				while (line.startsWith("#")) line = gffFr.readLine();
				tokens = line.split(RegExp.TAB);
				type = tokens[GFF.TYPE];
				if (type.equals("gene")) {
					start = Integer.parseInt(tokens[GFF.START]);
					end = Integer.parseInt(tokens[GFF.END]);
					// System.err.println("[[ DEBUG ]] :: skipping start " + start + " end " + end);
				}
			}
				
			// System.err.println("[[ DEBUG ]] :: Reading in" + inFile + " " + line);
			if(start == startL && end == endL) {
				writeOutput(tokens, gene, biotype);
				while (gffFr.hasMoreLines()) {
					line = gffFr.readLine();
					while (line.startsWith("#")) line = gffFr.readLine();
					tokens = line.split(RegExp.TAB);
					type = tokens[GFF.TYPE];
					if (type.equals("gene")) break;
					writeOutput(tokens, gene, biotype);
				}
			}
			gffFr.reset();
		}
	}
	
	private void writeOutput(String[] tokens, String geneName, String biotype) {
		for (int i = 0; i < 8; i++) {
			System.out.print(tokens[i] + "\t");
		}
		
		String[] field = tokens[8].split(";");
		System.out.print(field[0]);
		for (int i = 1; i < field.length; i++) {
			if (field[i].startsWith("gene_name=")) {
				System.out.print(";gene_name=" + geneName);
			} else if(field[i].startsWith("gene_biotype=")) {
				System.out.print(";gene_biotype=" + biotype);
			} else {
				System.out.print(";" + field[i]);
			}
		}
		System.out.println();
		
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar gffMergeGFFs.jar chose_from.bed in1.gff in2.gff ...");
		System.err.println();
		System.err.println("chose_from.bed: Bed format, with in each col");
		System.err.println("  1    chr");
		System.err.println("  2    start (1-based, matching the gff \"gene\" line start");
		System.err.println("  3    end   (1-based, matching the gff \"gene\" line end");
		System.err.println("  4    gene name");
		System.err.println("  5    strand");
		System.err.println("  6    gene biotype");
		System.err.println("  ...  (ignored)");
		System.err.println("  last grab the annotation from this file N (INTEGER)");
		System.err.println("2022-06-14 Arang Rhie. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length >= 3) {
			new MergeGFFs().go(args);
		} else {
			new MergeGFFs().printHelp();
		}
	}

}
