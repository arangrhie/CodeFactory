package javax.arang.gene.exp;

import java.util.ArrayList;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileReader;

public class SortUIgenesByChr extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Sort the uig files by each chr");
		System.out.println("Usage: java -jar sortUIgenes.jar <inFile1> (<inFile2> .. <inFileN>)");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		for (FileReader fr : frs) {
			String fileName = fr.getFullPath();
			String outFile = fileName.replace(fileName.substring(fileName.lastIndexOf(".")), ".ui");
			new SortUIgenes().go(fileName, outFile);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SortUIgenesByChr sorter = new SortUIgenesByChr();
		if (args.length < 1) {
			sorter.printHelp();
		} else {
			sorter.go(args);
		}

	}

}
