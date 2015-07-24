/**
 * 
 */
package javax.arang.gene.refseq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class FindGeneName extends I2Owrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new FindGeneName().go(args[0], args[1], args[1].replace(".bed", ".list"));
		} else {
			new FindGeneName().printHelp();
		}

	}

	HashMap<Integer, HashMap<String, Integer>> startGenes = new HashMap<Integer, HashMap<String, Integer>>();
	HashMap<Integer, HashMap<Integer, String>> startGenesMap = new HashMap<Integer, HashMap<Integer, String>>();
	HashMap<Integer, HashMap<String, Integer>> endGene = new HashMap<Integer, HashMap<String, Integer>>();
	HashMap<Integer, PriorityQueue<Integer>> startsQ = new HashMap<Integer, PriorityQueue<Integer>>();
	HashMap<Integer, LinkedList<Integer>> startsList = new HashMap<Integer, LinkedList<Integer>>();

	@Override
	public void hooker(FileReader frRefGene, FileReader frTarget, FileMaker fm) {
		String line;
		String[] tokens;
		
		for (int i = 1; i < 25; i++) {
			startGenes.put(i, new HashMap<String, Integer>());
			startGenesMap.put(i, new HashMap<Integer, String>());
			startsQ.put(i, new PriorityQueue<Integer>());
			endGene.put(i, new HashMap<String, Integer>());
			startsList.put(i, new LinkedList<Integer>());
		}
		
		// var for refGene.txt
		int CHR = 2;
		int START = 4;
		int END = 5;
		int GENE_NAME = 12;
		
		while(frRefGene.hasMoreLines()) {
			line = frRefGene.readLine();
			tokens = line.split("\t");
			if (tokens[CHR].contains("_") || tokens[CHR].startsWith("chrUn")) continue;
			Integer chr = getChrInt(tokens[CHR]);
			int start = Integer.parseInt(tokens[START]);
			int end = Integer.parseInt(tokens[END]);
			String geneName = tokens[GENE_NAME];
			HashMap<String, Integer> startGene = startGenes.get(chr);
			HashMap<Integer, String> startGeneMap = startGenesMap.get(chr);
			PriorityQueue<Integer> startQ = startsQ.get(chr);
			
			if (startGene.containsKey(geneName)) {
				int prevStart = startGene.get(geneName);
				if (start < prevStart) {
					startGene.put(geneName, start);	// startGene has been replaced.
					if (startGeneMap.get(prevStart).equals(geneName)) {
						startGeneMap.remove(prevStart);
						startQ.remove(prevStart);
					} else {	// startGeneMap.get(prevStart).contains(geneName)
						String prevGene = startGeneMap.get(prevStart);
						String[] prevGenes = prevGene.split(",");
						String newGene = "";
						for (int i = 0; i < prevGenes.length; i++) {
							if (prevGenes[i].equals(geneName)) {
								continue;
							}
							newGene = newGene + "," + prevGenes[i];
						}
						
						if (newGene.length() > 0 && newGene.charAt(0) == ',') {
							newGene = newGene.substring(1);
						}
						if (newGene.equals("")) {
							startGeneMap.remove(prevStart);
							startQ.remove(prevStart);
						} else {
							startGeneMap.put(prevStart, newGene);
						}
					}
					
					if (startGeneMap.containsKey(start)) {
						startGeneMap.put(start, geneName + "," + startGeneMap.get(start));	
					} else {
						startGeneMap.put(start, geneName);
						startQ.add(start);
					}
				} else {
					// ignore the new start
				}
			} else {
				
				startGenes.get(chr).put(geneName, start);
				if (startGeneMap.containsKey(start)) {
					startGeneMap.put(start, geneName + "," + startGeneMap.get(start));	
				} else {
					startGeneMap.put(start, geneName);
					startQ.add(start);
				}
			}
			
			
			if (endGene.get(chr).containsKey(geneName)) {
				int prevEnd = endGene.get(chr).get(geneName);
				if (prevEnd < end) {
					endGene.get(chr).put(geneName, end);		
				}
			} else {
				endGene.get(chr).put(geneName, end);
			}
			
		}
		
		for (int i = 1; i < 25; i++) {
			FileMaker fmGene = new FileMaker(fm.getDir(), "chr" + i + "_gene.list");
			for (int j = 0; j < startGenesMap.get(i).size(); j++) {
				int start = startsQ.get(i).remove();
				startsList.get(i).add(start);
				String gene = startGenesMap.get(i).get(start);
				if (gene.contains(",")) {
					String[] genes = gene.split(",");
					for (int k = 0; k < genes.length; k++) {
						fmGene.writeLine(genes[k] + "\t" + start + "\t" + endGene.get(i).get(genes[k]));
					}
				} else {
					fmGene.writeLine(gene + "\t" + start + "\t" + endGene.get(i).get(gene));
				}
			}
			fmGene.closeMaker();
		}
		
		CHR = 0;
		START = 1;
		END = 2;
		
		// header
		fm.writeLine("chr\tstart\tstop\tname");
		
		while (frTarget.hasMoreLines()) {
			line = frTarget.readLine();
			tokens = line.split("\t");
			if (tokens.length != 3) {
				continue;
			}
			int chr = getChrInt(tokens[CHR]);
			int start = Integer.parseInt(tokens[START]);
			int end = Integer.parseInt(tokens[END]);
			
			fm.writeLine(line + "\t" + getGeneName(chr, start, end));
		}
		
	}
	
	/**
	 * @param chr
	 * @param start
	 * @param end
	 * @return
	 */
	private String getGeneName(int chr, int start, int end) {

		LinkedList<Integer> startList = startsList.get(chr);
		for (int i = 0; i < startList.size(); i++) {
			int startL = startList.get(i);
//			if (start < startL - 20000) {
//				break;
//			}
			String gene = startGenesMap.get(chr).get(startL);
			if (gene.contains(",")) {
				String[] genes = gene.split(",");
				for (int j = 0; j < genes.length; j++) {
					int endR = endGene.get(chr).get(genes[j]);
					if (startL <= start && start < endR
							|| startL + 1000 > start && startL < end
							|| startL < end && end < endR
							|| start < startL && endR < end) {
						return  genes[j]; // + "\t" + startL + "-" + endR;
					}
				}
			} else {
				int endR = endGene.get(chr).get(gene);
				if (startL <= start && start < endR
						|| startL + 1000 > start && startL < end
						|| startL < end && end < endR
						|| start < startL && endR < end) {
					return  gene; // + "\t" + startL + "-" + endR;
				}
			}
		}
		return "";
	}

	private Integer getChrInt(String chr) {
		chr = chr.replace("chr", "");
		if (chr.equals("X")) {
			return 23;
		} else if (chr.equals("Y")) {
			return 24;
		} else {
			return Integer.parseInt(chr);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar findGeneName.jar <refGene.txt> <target.bed>");
		System.out.println("\t<refGene.txt>: refGene list downloaded from UCSC");
		System.out.println("\t<target.bed>: list of file with chr\tstart\tend");
		System.out.println("\t<output>: <target.list> containing chr\tstart\tend\tname (gene)");
	}
	

}
