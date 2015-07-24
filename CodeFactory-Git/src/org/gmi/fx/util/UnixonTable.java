package org.gmi.fx.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class UnixonTable {
	
	public static volatile UnixonTable unixonTable;
	
	private static final String HD_FILE_DIR = "MetaData/";
	private static final String UNIXON_TABLE = "unixons.obj";
	private HashMap<Long, Integer[]> unixons;
	
	public static UnixonTable getInstance() {
		if (unixonTable == null) {
			synchronized(UnixonTable.class) {
				if (unixonTable == null) {
					unixonTable = new UnixonTable();
				}
			}
		}
		return unixonTable;
	}
	
	private UnixonTable() {
		unixons = loadUnixonsFromHDFS(HD_FILE_DIR + UNIXON_TABLE);
	}
 
	@SuppressWarnings("unchecked")
	public HashMap<Long, Integer[]>  loadUnixonsFromHDFS(String file) {
		HashMap<Long, Integer[]> table = null;
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(new Configuration());
			FSDataInputStream dis = hdfs.open(new Path(file));
			ObjectInputStream objIn = new ObjectInputStream(dis);
			table = (HashMap<Long, Integer[]>) objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * @return the unixons
	 */
	public HashMap<Long, Integer[]> getUnixons() {
		return unixons;
	}
}
