package org.gmi.fx.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class ExomeTableHandler {
	private static final String FILE_PATH = "MetaData/exomeTable.obj";
	static HashMap<String, Exome> table;
	
	public static void main (String[] args) {

		ExomeTableHandler handler = new ExomeTableHandler();
		System.out.println("Make Serialized Object");
		handler.makeSeirializedObj();
		System.out.println("Reconstruct the Serialized Object");
		HashMap<String, Exome> table = handler.reconstructSerializedObj("FX/TABLES/exomeTable.obj");
		System.out.println("========= TEST CODE ========");
		Exome exome = table.get("uc003nrn.1");
		System.out.println(exome.getChrom());
		System.out.println(exome.getExonCount());
		System.out.println(exome.getExonStarts());
		System.out.println(exome.getExonEnds());
//		System.out.println("Reconstruct from HDFS");
//		table = handler.loadFromHDFS(FILE_PATH);
//		System.out.println("========= TEST CODE ========");
//		printExomes("uc003lsd.1");
//		printExomes("uc003lsd.1");
//		printExomes("uc003lsc.1");
//		printExomes("NM_004355");
//		printExomes("NM_001025159");
	}
	
	private static void printExomes(String geneName) {
		Exome exome = table.get(geneName);
		System.out.println(exome.getChrom());
		System.out.println("Counts: " + exome.getExonCount());
		for (int i = 0; i < exome.getExonCount(); i ++) {
			System.out.print(exome.getExonStarts()[i] + ", ");
			
		}
		System.out.println();
		for (int i = 0; i < exome.getExonCount(); i ++) {
			System.out.print(exome.getExonEnds()[i] + ", ");
		}
		System.out.println();
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Exome> loadFromHDFS(String file) {
		HashMap<String, Exome> table = null;
		FileSystem hdfs = null;
		try {
//			long startTime = System.currentTimeMillis();
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<String, Exome>) objIn.readObject();
//			long runningTime = (System.currentTimeMillis() - startTime);
//			System.out.println("Re-construction running time : " + (runningTime/60000) + "m " + ((runningTime/1000)%60) + "sec " + (runningTime%1000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	private void makeSeirializedObj() {
		long startTime = System.currentTimeMillis();
		try {
			HashMap<String, Exome> table = ExomeTable.getInstance().getExomeMap();
			ObjectOutput objOut = new ObjectOutputStream(new FileOutputStream("exomeTable.obj"));
			objOut.writeObject(table);
			objOut.flush();
		} catch (IOException e) {
			System.out.println("IO Exception occurred...!!");
			e.printStackTrace();
		} finally {
			System.out.println("Serialization finished successfully!!");
		}
		long runningTime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Serialization running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");

	}

	public HashMap<String, Exome> reconstructSerializedObj(String path) {
		HashMap<String, Exome> table = null;
		try {
			long startTime = System.currentTimeMillis();
			ObjectInput objIn = new ObjectInputStream(new FileInputStream(path)); 
			table = (HashMap<String, Exome>) objIn.readObject();
			long runningTime = (System.currentTimeMillis() - startTime) / 1000;
			System.out.println("Re-construction running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
		} catch(IOException e) {
			System.out.println("IO Exception occure while reading...");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("Class not found: reconstruct it");
			makeSeirializedObj();
		} finally {
			System.out.println("Reading again done successfully!! :D");
		}
		return table;
	}
	
	/***
	 * gene_id		geneSize	exonCount	chrom	strand	exonCount	exonStarts		exonEnds
	 * uc001aaa.2		2122		3		chr1		+		3		1115,2475,3083,	2090,2584,4121,
	 */
	@SuppressWarnings("unused")
	private HashMap<String, Exome> loadFromFile(String file) {
		HashMap<String, Exome> exomeMap = new HashMap<String, Exome>();
		BufferedReader in = null;
		int count = 0;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "\t");
				String key = st.nextToken();	// geneID
				int geneLen = Integer.parseInt(st.nextToken());	// skip geneSize
				st.nextToken(); // skp first exonCount
				String chrom = st.nextToken();
				st.nextToken();	// skip strand
				int exonCount = Integer.parseInt(st.nextToken());
				String starts = st.nextToken();
				String ends = st.nextToken();
				StringTokenizer startTok = new StringTokenizer(starts, ",");
				StringTokenizer endTok = new StringTokenizer(ends, ",");
				int[] exonStarts = new int[exonCount];
				int[] exonEnds = new int[exonCount]; 
				for(int i = 0; i < exonCount; i++) {
					exonStarts[i] = Integer.parseInt(startTok.nextToken());
					exonEnds[i] = Integer.parseInt(endTok.nextToken());
				}
				exomeMap.put(key, new Exome(key, chrom, exonCount, exonStarts, exonEnds, geneLen));
				count++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("DEBUG :: file " + file + " does not exists.");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("DEBUG :: Number format exception occured!!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("DEBUG :: IOexception occured!!");
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(in);
			System.out.println("Loading is DONE!! Loaded " + count + " lines in total. :D");
		}
		return exomeMap;
	}
	
}
