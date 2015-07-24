package javax.arang.falcon;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class IntegratePrimaryWiAssocContig extends INOwrapper {


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconIntegratePrimaryWiAssocContig.jar <a_ctg.fa> <p_ctg.fa> <a_cgt_coorinate.txt>");
		System.out.println("\tAdd overlapping regions of primary with associated contigs for building super contigs");
		System.out.println("\t<a_ctg.fa>: Associated Contigs");
		System.out.println("\t<p_ctg.fa>: Priamry Contigs");
		System.out.println("\t<a_cgt_coorinate.txt>: output of fc_ovlp_to_graph.py written by Jason Chin");
		System.out.println("\t<out: a_ctg_full.fa>");
		System.out.println("Arang Rhie, 2015-06-09. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new IntegratePrimaryWiAssocContig().go(args, "a_ctg_full.fa");
		} else {
			new IntegratePrimaryWiAssocContig().printHelp();
		}
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		FileReader aCtgFr = frs.get(0);
		FileReader aCtgCoordFr = frs.get(1);
		FileReader pCtgFr = frs.get(2);
		
		
	}

}
