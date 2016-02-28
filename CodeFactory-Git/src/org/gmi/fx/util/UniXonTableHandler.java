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
import java.util.Vector;

import javax.arang.genome.util.Util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

public class UniXonTableHandler {
	private static final String LOCAL_FILE_DIR = "FX/TABLES/";
	private static final String GENE_EXOME_TABLE = "geneExomeList.txt";
	private static final String HD_FILE_DIR = "MetaData/";
	private static final String UNIXON_GENE_TABLE = "unixonGeneMap.obj";
	private static final String UNIXON_TABLE = "unixons.obj";
	private static final String STARTS_TABLE = "starts.obj";
	private static final String ENDS_TABLE = "ends.obj";
	private static long id = 0;
	
//	private HashMap<String, String> table;
	HashMap<Long, Vector<String>> unixonMap = new HashMap<Long, Vector<String>>();
	HashMap<Long, Integer[]> unixons = new HashMap<Long, Integer[]>();
	HashMap<Integer, Vector<Long>> starts = new HashMap<Integer, Vector<Long>>();
	HashMap<Integer, Vector<Long>> ends = new HashMap<Integer, Vector<Long>>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UniXonTableHandler handler = new UniXonTableHandler();
		
//		System.out.println("Load from file, generate 4 tables");
//		handler.loadFromFile(LOCAL_FILE_DIR + GENE_EXOME_TABLE);
		System.out.println("========= TEST CODE ========");
//		printUnixonGeneEtc(handler.unixonMap, handler.starts, 1000001115);
//		printUnixonGeneEtc(handler.unixonMap, handler.starts, 1000004268);
//		
//		System.out.println("Make Serialized Objects");
//		handler.makeSeirializedUniXonObj(handler.unixonMap, LOCAL_FILE_DIR + UNIXON_GENE_TABLE);
//		handler.makeSeirializedUnixonObj(handler.unixons, LOCAL_FILE_DIR + UNIXON_TABLE);
//		handler.makeSeirializedObj(handler.starts, LOCAL_FILE_DIR + STARTS_TABLE);
//		handler.makeSeirializedObj(handler.ends, LOCAL_FILE_DIR + ENDS_TABLE);
		
//		System.out.println("Reconstruct the Serialized Object");
//		HashMap<Long, Vector<String>> uniXonMap = handler.reconstructSerializedUniXonObj(LOCAL_FILE_DIR + UNIXON_GENE_TABLE);
//		handler.reconstructSerializedUniXonObj(LOCAL_FILE_DIR + UNIXON_TABLE);
//		HashMap<Integer, Vector<Long>> starts = handler.reconstructSerializedObj(LOCAL_FILE_DIR + STARTS_TABLE);
//		printUnixonGeneEtc(uniXonMap, starts, 1000004268);
		
		/**	
		 *  ========= TEST CODE ========
		 *  Reconstruct the Serialized Object
		 *  Re-construction running time : 0m 11sec
		 *  Re-construction running time : 0m 6sec
		 *  Re-construction running time : 0m 7sec
		 *  1000004268 has unixonIDs
		 *  	5 in genes
		 *  		uc001aab.2:1:425:-
		 *  		uc009viq.1:1:425:-
		 *  		uc009vir.1:1:425:-
		 *  		uc001aac.2:1:425:-
		 *  		uc009vis.1:1:425:-
		 *  		uc009vit.1:1:425:-
		 *  		uc009viu.1:1:425:-
		 *  		uc001aae.2:1:425:- 
		 */
		
//		handler.hdfsPut(LOCAL_FILE_DIR + UNIXON_GENE_TABLE, HD_FILE_DIR + UNIXON_GENE_TABLE);
//		handler.hdfsPut(LOCAL_FILE_DIR + STARTS_TABLE, HD_FILE_DIR + STARTS_TABLE);
//		handler.hdfsPut(LOCAL_FILE_DIR + ENDS_TABLE, HD_FILE_DIR + ENDS_TABLE);
		
		System.out.println("Load from HDFS");
		HashMap<Long, Vector<String>>  uniXonMap = handler.loadUniXonFromHDFS(HD_FILE_DIR + UNIXON_GENE_TABLE);
		HashMap<Integer, Vector<Long>> starts = handler.loadFromHDFS(HD_FILE_DIR + STARTS_TABLE);
		printUnixonGeneEtc(uniXonMap, starts, 1000004268);
		
		/***
		 * ========= TEST CODE ========
		 * Load from HDFS
		 * 11/04/27 16:32:52 INFO security.Groups: Group mapping impl=org.apache.hadoop.security.ShellBasedUnixGroupsMapping; cacheTimeout=300000
		 * 11/04/27 16:32:52 WARN conf.Configuration: mapred.task.id is deprecated. Instead, use mapreduce.task.attempt.id
		 * Re-construction running time : 0m 3sec 389
		 * Re-construction running time : 0m 1sec 139
		 * 1000004268 uniXonID=4,	uniXonGene=uc001aab.2:1:425:-,uc009viq.1:1:425:-,uc009vir.1:1:425:-,uc001aac.2:1:425:-,uc009vis.1:1:425:-,uc009vit.1:1:425:-,uc009viu.1:1:425:-,uc001aae.2:1:425:-
		 */
	}
	
	private static void printUnixonGeneEtc(HashMap<Long, Vector<String>> unixonMap, HashMap<Integer, Vector<Long>> starts, Integer genomePos) {
		Vector<Long> uniXonIDs = starts.get(genomePos);
		System.out.println(genomePos + " has unixonIDs");
		for (int i = 0; i < uniXonIDs.size(); i++) {
			System.out.println("\t" + uniXonIDs.get(i) + " in genes");
			Vector<String> uniMap = unixonMap.get(uniXonIDs.get(i));
			for (int j = 0; j < uniMap.size(); j++)
				 System.out.println("\t\t" + uniMap.get(j) + " ");
		}
		System.out.println();
	}
	
	public void hdfsPut(String localPath, String hdfsPath) {
		FileSystem hdfs = null;
		FSDataOutputStream out = null;
		
		try {
			hdfs = FileSystem.get(new Configuration());
			out = hdfs.create(new Path(hdfsPath));
			ObjectInput objIn = new ObjectInputStream(new FileInputStream(localPath));
			while(objIn.available() > 0) {
				out.write(objIn.readByte());
			}
			out.close();
			hdfs.close();
			objIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long getNextUniXonID() {
		return id++;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, Vector<Long>> loadFromHDFS(String file) {
		HashMap<Integer, Vector<Long>> table = null;
		FileSystem hdfs = null;
		try {
			long startTime = System.currentTimeMillis();
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<Integer, Vector<Long>>) objIn.readObject();
			long runningTime = (System.currentTimeMillis() - startTime);
			System.out.println("Re-construction running time : " + (runningTime/60000) + "m " + ((runningTime/1000)%60) + "sec " + (runningTime%1000));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Long, Vector<String>> loadUniXonFromHDFS(String file) {
		HashMap<Long, Vector<String>> table = null;
		FileSystem hdfs = null;
		try {
			long startTime = System.currentTimeMillis();
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<Long, Vector<String>>) objIn.readObject();
			long runningTime = (System.currentTimeMillis() - startTime);
			System.out.println("Re-construction running time : " + (runningTime/60000) + "m " + ((runningTime/1000)%60) + "sec " + (runningTime%1000));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private HashMap<Integer, Vector<Long>> reconstructSerializedObj(String file) {
		HashMap<Integer, Vector<Long>> table = null;
		try {
			long startTime = System.currentTimeMillis();
			ObjectInput objIn = new ObjectInputStream(new FileInputStream(file)); 
			table = (HashMap<Integer, Vector<Long>>) objIn.readObject();
			long runningTime = (System.currentTimeMillis() - startTime) / 1000;
			System.out.println("Re-construction running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
			objIn.close();
		} catch(IOException e) {
			System.out.println("IO Exception occure while reading...");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("Class not found: reconstruct it");
//			makeSeirializedObj(LOCAL_FILE_DIR + EXON_TABLE);
		} finally {
//			System.out.println("Reading again done successfully!! :D");
		}
		return table;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private HashMap<Long, Vector<String>> reconstructSerializedUniXonObj(String file) {
		HashMap<Long, Vector<String>> table = null;
		try {
			long startTime = System.currentTimeMillis();
			ObjectInput objIn = new ObjectInputStream(new FileInputStream(file)); 
			table = (HashMap<Long, Vector<String>>) objIn.readObject();
			long runningTime = (System.currentTimeMillis() - startTime) / 1000;
			System.out.println("Re-construction running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
			objIn.close();
		} catch(IOException e) {
			System.out.println("IO Exception occure while reading...");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("Class not found: reconstruct it");
//			makeSeirializedObj(LOCAL_FILE_DIR + EXON_TABLE);
		} finally {
//			System.out.println("Reading again done successfully!! :D");
		}
		return table;
	}
	

	@SuppressWarnings("unused")
	private void makeSeirializedUniXonObj(HashMap<Long, Vector<String>> map, String path) {
		long startTime = System.currentTimeMillis();
		try {
			ObjectOutput objOut = new ObjectOutputStream(new FileOutputStream(path));
			objOut.writeObject(map);
			objOut.flush();
			objOut.close();
		} catch (IOException e) {
			System.out.println("IO Exception occurred...!!");
			e.printStackTrace();
		} finally {
			System.out.println("Serialization finished successfully!!");
		}
		long runningTime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Serialization running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
	}
	
	@SuppressWarnings("unused")
	private void makeSeirializedUnixonObj(HashMap<Long, Integer[]> map, String path) {
		long startTime = System.currentTimeMillis();
		try {
			ObjectOutput objOut = new ObjectOutputStream(new FileOutputStream(path));
			objOut.writeObject(map);
			objOut.flush();
			objOut.close();
		} catch (IOException e) {
			System.out.println("IO Exception occurred...!!");
			e.printStackTrace();
		} finally {
			System.out.println("Serialization finished successfully!!");
		}
		long runningTime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Serialization running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
	}
	
	@SuppressWarnings("unused")
	private void makeSeirializedObj(HashMap<Integer, Vector<Long>> map, String path) {
		long startTime = System.currentTimeMillis();
		try {
			ObjectOutput objOut = new ObjectOutputStream(new FileOutputStream(path));
			objOut.writeObject(map);
			objOut.flush();
			objOut.close();
		} catch (IOException e) {
			System.out.println("IO Exception occurred...!!");
			e.printStackTrace();
		} finally {
			System.out.println("Serialization finished successfully!!");
		}
		long runningTime = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Serialization running time : " + (runningTime/60) + "m " + (runningTime%60) + "sec");
	}
	
	
	/***
	 * Load file in the following form:
	 * geneId		geneSize	exonCount	chrom	strand	exonCount	exonStarts		exonEnds
	 * uc001aaa.2		2122		3		chr1		+		3		1115,2475,3083,	2090,2584,4121,
	 * 
	 * Reconstruct 4 tables "unixonGeneMap", "unixons", "starts", "ends" with the following format
	 * 
	 * unixonGeneMap <Long, Vector<String>>
	 * uniXonID					geneId:exonCount:exonLength:strand (uniXonVal)
	 * <generated long number>	uc001aaa.2:1:2090-1115+1:+(,...)
	 * 
	 * unixons <Long, Integer[]>
	 * <generated long number>	genomeStartPos	genomeEndPos
	 * 
	 * starts <Integer, Vector<Long>>
	 * genomeStartPos		uniXonID
	 * 1,000001115			<generated long number>
	 * 1,000002475			<generated long number>
	 * 1,000003083			<generated long number>
	 * 
	 * ends <Integer, Vector<Long>>
	 * genomeEndPos			uniXonID
	 * 1,000002090			<generated long number>
	 * 1,000002584			<generated long number>
	 * 1,000004121			<generated long number>
	 */
	@SuppressWarnings("unused")
	private void loadFromFile(String file) {
//		HashMap<String, String> exonMap = new HashMap<String, String>();
		BufferedReader in = null;
		int count = 0;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = in.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "\t");
				String geneId = st.nextToken();	// geneId
				st.nextToken();					// skip geneSize
				st.nextToken(); 				// skip first exonCount
				String chr = st.nextToken();
				String strand = st.nextToken();	// skip strand
				int exonCount = Integer.parseInt(st.nextToken());
				String startRegions = st.nextToken();
				String endRegions = st.nextToken();
				StringTokenizer startTok = new StringTokenizer(startRegions, ",");
				StringTokenizer endTok = new StringTokenizer(endRegions, ",");
				int[] exonStarts = new int[exonCount];
				int[] exonEnds = new int[exonCount];
				
				for(int i = 0; i < exonCount; i++) {
					exonStarts[i] = Integer.parseInt(startTok.nextToken());
					exonEnds[i] = Integer.parseInt(endTok.nextToken());
					Integer startKey = Util.getChromIntVal(chr)*1000000000 + exonStarts[i];
					Integer endKey = Util.getChromIntVal(chr)*1000000000 + exonEnds[i];
					String uniXonVal = geneId + ":" + (i+1) + ":" + (exonEnds[i] - exonStarts[i] + 1) + ":" + strand;
					
					if (starts.containsKey(startKey) && ends.containsKey(endKey)) {
						Long uniXonId = getSameUniXonId(starts.get(startKey), ends.get(endKey)); 
						if (uniXonId > 0) {
							unixonMap.get(uniXonId).add(uniXonVal);
						} else {
							putNewUnixonGeneMap(startKey, endKey, uniXonVal);
						}
					} else {
						putNewUnixonGeneMap(startKey, endKey, uniXonVal);
					}
				}
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
			System.out.println("Loading is DONE!! Loaded " + count + " lines from " + file + " :D");
		}
	}
	
	private void putNewUnixonGeneMap(Integer startKey, Integer endKey, String unixonGene) {
		Long newID = getNextUniXonID();
		if (starts.get(startKey) == null) {
			Vector<Long> unixonGeneVal = new Vector<Long>();
			unixonGeneVal.add(newID);
			starts.put(startKey, unixonGeneVal);
		} else {
			starts.get(startKey).add(newID);
		}

		if (ends.get(endKey) == null) {
			Vector<Long> unixonGeneVal = new Vector<Long>();
			unixonGeneVal.add(newID);
			ends.put(endKey, unixonGeneVal);
		} else {
			ends.get(endKey).add(newID);
		}
		
		Vector<String> unixonGeneVal = new Vector<String>();
		unixonGeneVal.add(unixonGene);
		unixonMap.put(newID, unixonGeneVal);
		
		unixons.put(newID, new Integer[]{startKey, endKey});
	}


	private Long getSameUniXonId(Vector<Long> startUnixonIDs, Vector<Long> endUnixonIDs) {
		for (Long uniXonID : startUnixonIDs) {
			if (endUnixonIDs.contains(uniXonID)) {
				return uniXonID;
			}
		}
		return -1l;
	}
}
