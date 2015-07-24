package javax.arang.genome.snp;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.LineBucket;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AddAnnotations extends I2Owrapper {

	private static final short SNP_POS = 1;
	private static final short ANNO_POS = 3;
	private static final short ANNO_NAME = 0;
	private static final short ANNO_RS = 1;
	
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		fm.write(fr1.readLine() + "\t");
		boolean hasSetHeader = false;
		LineBucket snpTable = new LineBucket(fr1);
		LineBucket annoTable = new LineBucket(fr2);
		while (snpTable.hasNext() && annoTable.hasNext()) {
			int pos1 = Integer.parseInt(snpTable.getColumn(SNP_POS));
			if (!hasSetHeader) {
				fm.writeLine(annoTable.getColumn(ANNO_NAME));
				hasSetHeader = true;
			}
			int pos2 = Integer.parseInt(annoTable.getColumn(ANNO_POS));
			while (pos1 < pos2) {
				fm.writeLine(snpTable.getLine() + "\t-");
				if (!snpTable.hasNext()) {
					break;
				}
				pos1 = Integer.parseInt(snpTable.getColumn(SNP_POS));
			}
			if (pos1 == pos2) {
				fm.writeLine(snpTable.getLine() + "\t" + annoTable.getColumn(ANNO_RS));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Add annotations droped from dbSNP filtering to the snp table file");
		System.out.println("Usage: java -jar addAnnotations.jar <snpTableFile> <snpTableFile.hgN_snpN_dropped> <outFile>");
		System.out.println("\tsnpTableFile: snp table file");
		System.out.println("\tsnpTableFile.hgN_snpN_dropped: generated with annovar filter-based method");
		System.out.println("\toutFile: snp table file with an annotation added");
	}
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new AddAnnotations().go(args[0], args[1], args[2]);
		} else {
			new AddAnnotations().printHelp();
		}
	}

}
