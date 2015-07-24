package org.gmi.fx.util;

import java.util.HashMap;

/***
 * Usage: ExomeTable.getInstance()
 * Returns an ExomeTable. get gene specific Exome by get(geneID) method from ExomeTable instance.
 * @author ¾Æ¶û
 *
 */
public class ExomeTable {

//	private static final String DEFAULT_PATH = "/home/hadoop/filelist.txt";
	private static final String DEFAULT_PATH = "C://Documents and Settings/¾Æ¶û/¹ÙÅÁ È­¸é/CloudSNP/sample/tables/exomeTable.obj";
	private static final String FILE_PATH = "MetaData/exomeTable.obj";
	private static volatile ExomeTable exomeTable;
	private HashMap<String, Exome> exomeMap;
	
	private ExomeTable() {
//		exomeMap = new ExomeTableHandler().loadFromHDFS(FILE_PATH);
		exomeMap = new ExomeTableHandler().reconstructSerializedObj(DEFAULT_PATH);
	}
	
	public static ExomeTable getInstance() {
		if (exomeTable == null) {
			synchronized(ExomeTable.class) {
				if (exomeTable == null) {
					exomeTable = new ExomeTable();
				}
			}
		}
		return exomeTable;
	}
	
	public Exome get(String geneID) {
		return exomeMap.get(geneID);
	}
	
	public HashMap<String, Exome> getExomeMap() {
		return exomeMap;
	}
}
