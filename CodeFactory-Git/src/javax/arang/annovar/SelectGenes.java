package javax.arang.annovar;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SelectGenes extends I2Owrapper {

	@Override
	public void hooker(FileReader frAnnovar, FileReader frList, FileMaker fm) {
		ArrayList<String> geneList = new ArrayList<String>();
		while (frList.hasMoreLines()) {
			geneList.add(frList.readLine());
		}
		
		String line;
		String[] tokens;
		String[] genes;
		while (frAnnovar.hasMoreLines()) {
			line = frAnnovar.readLine();
			tokens = line.split(RegExp.TAB);
			genes = tokens[geneNameIdx].split(",");
			for (String gene : genes) {
				if (geneList.contains(gene)) {
					writeAnnotation(fm, tokens, gene);
				}
			}
		}
	}
	
	private void writeAnnotation(FileMaker fm, String[] tokens, String gene) {
		for (int i = 0; i < geneNameIdx; i++) {
			fm.write(tokens[i] + "\t");	
		}
		
		fm.write(gene);
		for (int i = geneNameIdx + 1; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtSelectGenes.jar <in.annovar> <in.genes.list> <out.annovar> [gene_name_column_idx]");
		System.out.println("\t<in.annovar>: annotated with wgEncodeGencodeBasicV19 or other gene-based annotation database");
		System.out.println("\t<in.genes.list>: list of genes to select");
		System.out.println("\t<out.annovar>: selected genes will be reported. 1 pos with multiple gene annotation will be written per gene / per line.");
		System.out.println("\t[gene_name_column_idx]: column containing gene name. 1-based. DEFAULT=8");
		System.out.println("Arang Rhie, 2015-12-28. arrhie@gmail.com");
	}

	private static int geneNameIdx = 7;
	public static void main(String[] args) {
		if (args.length == 3) {
			new SelectGenes().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			geneNameIdx = Integer.parseInt(args[3]) - 1;
			new SelectGenes().go(args[0], args[1], args[2]);
		} else {
			new SelectGenes().printHelp();
		}
	
	}

}
