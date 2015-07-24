package javax.arang.chain;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.agp.AGP;

public class BedSuperContigToContig extends I2Owrapper {

	@Override
	public void hooker(FileReader frChainBed, FileReader frAgp, FileMaker fm) {
		// Contig, SuperScaffold
		HashMap<String, SuperScaffold> superScaffolds = new HashMap<String, SuperScaffold>();
		
		String line;
		String[] tokens;
		String[] contigTokens = new String[3];
		String contig;
		int start;
		int end;
		int size;
		String orientation;
		
		if (hasHeader) {
			frAgp.readLine();
		}
		
		// Add .agp to hashmap
		while (frAgp.hasMoreLines()) {
			line = frAgp.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[AGP.OBJ_NAME];
			// N
			if (tokens[AGP.COMPONENT_TYPE].equals("N")) {
				start = Integer.parseInt(tokens[AGP.OBJ_START]) - 1;
				end = Integer.parseInt(tokens[AGP.OBJ_END]);
				size = Integer.parseInt(tokens[AGP.COMPONENT_SIZE]);
				if (!superScaffolds.containsKey(contig)) {
					superScaffolds.put(contig, new SuperScaffold());
				}
				superScaffolds.get(contig).addPosition(
						start, end,
						contig + ":N", start, end, size, "+");
				continue;
			}
			// Contig
			size = Integer.parseInt(tokens[AGP.COMPONENT_SIZE]);
			orientation = tokens[AGP.ORIENTATION];
			if (tokens[AGP.COMPONENT_ID].contains(":")) {
				contigTokens = tokens[AGP.COMPONENT_ID].split(":");
				tokens[AGP.COMPONENT_ID] = contigTokens[0];
				contigTokens = contigTokens[1].split("-");
				start = Integer.parseInt(contigTokens[0]) - 1;
				end = Integer.parseInt(contigTokens[1]);
			} else {
				start = Integer.parseInt(tokens[AGP.COMPONENT_START]) - 1;
				end = Integer.parseInt(tokens[AGP.COMPONENT_END]);
			}
			if (!superScaffolds.containsKey(contig)) {
				superScaffolds.put(contig, new SuperScaffold());
			}
			superScaffolds.get(contig).addPosition(
					Integer.parseInt(tokens[AGP.OBJ_START]) - 1,
					Integer.parseInt(tokens[AGP.OBJ_END]),
					tokens[AGP.COMPONENT_ID],
					start, end, size, orientation);
		}
		
		
		String[] superScaffoldContigInfo;
		int tmp1;
		int tmp2;
		
		// Convert Super-Scaffolds to contigs
		while (frChainBed.hasMoreLines()) {
			line = frChainBed.readLine();
			tokens = line.split(RegExp.TAB);

			if (superScaffolds.containsKey(tokens[ChainBed.CONTIG])) {
				start = Integer.parseInt(tokens[ChainBed.CONTIG_START]);
				end = Integer.parseInt(tokens[ChainBed.CONTIG_END]);
				size = Integer.parseInt(tokens[ChainBed.CONTIG_SIZE]);
				if (tokens[ChainBed.CONTIG_STRAND].equals("-")) {
					tmp1 = size - end;
					tmp2 = size - start;
					start = tmp1;
					end = tmp2;
					// For debugging
					if (tokens[ChainBed.CONTIG].equals("Super-Scaffold_2")) {
						System.out.println("[DEBUG] :: Super-Scaffold_2 " + start + " " + end);
					}
				}
				superScaffoldContigInfo = superScaffolds.get(tokens[ChainBed.CONTIG]).getComponent(start, end);
				contig = superScaffoldContigInfo[SuperScaffold.COMPONENT_CONTIG];
				start = Integer.parseInt(superScaffoldContigInfo[SuperScaffold.COMPONENT_START]);
				end = Integer.parseInt(superScaffoldContigInfo[SuperScaffold.COMPONENT_END]);
				size = Integer.parseInt(superScaffoldContigInfo[SuperScaffold.COMPONENT_SIZE]);
				
				if (superScaffoldContigInfo[SuperScaffold.COMPONENT_ORIENTATION].equals("-")
						|| tokens[ChainBed.CONTIG_STRAND].equals("-") && superScaffoldContigInfo[SuperScaffold.COMPONENT_ORIENTATION].equals("+")) {
					tmp1 = size - end;
					tmp2 = size - start;
					start = tmp1;
					end = tmp2;
				}
				if (!tokens[ChainBed.CONTIG_STRAND].equals(superScaffoldContigInfo[SuperScaffold.COMPONENT_ORIENTATION])) {
					orientation = "-";
				} else {
					orientation = "+";
				}
				for (int i = 0; i < ChainBed.CONTIG; i++) {
					fm.write(tokens[i] + "\t");
				}
				fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + orientation + "\t" + size + "\t" + tokens[ChainBed.CONTIG_BLOCK]);
				// For debugging
				if (tokens[ChainBed.CONTIG].equals("Super-Scaffold_2")) {
					System.out.println("[DEBUG] :: contig " + contig + " " + start + " " + end);
				}
			} else {
				fm.writeLine(line);
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainBedSuperContigsToContig.jar <chain.sort.block.bed> <Super-Scaffold.agp> <out.bed> [header=TRUE]");
		System.out.println("Convert Super-Scaffold_NN to contigs, and it's corresponding position");
		System.out.println("\t<chain.sort.block.bed>: generated with chainToBed.jar");
		System.out.println("\t\tAlignedContig	CHR	Start	End	Strand	Contig	Start	End	Strand	Len	ContigSize	AlignmentScore	id");
		System.out.println("\t<Super-Scaffold.agp>: Super-Scafoolds and it's contig position file, with the complete component length to handle splitted contigs");
		System.out.println("\t\tObj_Name	 Obj_Start 	 Obj_End 	PartNum	Compnt_Type	CompntId_GapLength	CompntStart_GapType	 CompntEnd_Linkage 	Orientation_LinkageEvidence	Compnt_Len");
		System.out.println("\t[header]: Contains first line as a header line in .agp? DEFAULT=TRUE");
		System.out.println("Arang Rhie, 2015-07-08. arrhie@gmail.com");
	}

	static boolean hasHeader = true;
	public static void main(String[] args) {
		if (args.length == 3) {
			new BedSuperContigToContig().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			hasHeader = Boolean.parseBoolean(args[3]);
			new BedSuperContigToContig().go(args[0], args[1], args[2]);
		} else {
			new BedSuperContigToContig().printHelp();
		}
	}

}
