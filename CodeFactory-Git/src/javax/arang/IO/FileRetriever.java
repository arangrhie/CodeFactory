/**
 * 
 */
package javax.arang.IO;

import java.io.File;
import java.util.Vector;

import javax.arang.IO.basic.FileReader;

/**
 * Retrieves files recursively from a given path and extension (e.g. .fna)
 * @author Arang
 *
 */
public class FileRetriever {
	String path;
	String ext;
	Vector<String> fileList = new Vector<String>();
	Vector<FileReader> files = new Vector<FileReader>();
	
	/***
	 * Retrieve all files from path
	 * @param path	directory path which sub-directories are subject to retrieve
	 */
	public FileRetriever(String path){
		this.path = path;
		this.ext = "";
	}
	
	/***
	 * Retrieve all files from path and given extension
	 * @param path	directory path which sub-directories are subject to retrieve
	 * @param extension File extension such as ".fna", ".qual"
	 */
	public FileRetriever(String path, String extension){
		this.path = path;
		this.ext = extension;
	}
	
	/***
	 * Retrieve files under path with a given extension.
	 * If extension was not specified by instantiation,
	 * this method returns all files under the path recursively.
	 * @return retrieved FileReader object vector
	 *  which are accessible to read with readLine method
	 */
	public Vector<FileReader> getFiles() {
		getSubDirs(path);
		return files;
	}
		
	/***
	 * Retrieve files under path with given extension.
	 * @param extension File extension such as ".fna", ".qual"
	 * @return	retrieved FileReader object vector
	 *  which are accessible to read with readLine method
	 */
	public Vector<FileReader> getFiles(String extension) {
		ext = extension;
		getSubDirs(path);
		return files;
	}
	
	private String[] getSubDirs(String path){
		String[] dir = null;
		try{
			File directory = new File(path);
			if (directory.isDirectory()){
				dir = directory.list();
				for (int i = 0; i < dir.length; i++) {
//					System.out.println(dir[i]);
					if (dir[i].contains(ext)){
//						fileList.add(dir[i]);
						FileReader file = new FileReader(directory.getAbsolutePath()+"\\"+dir[i]);
						files.add(file);
					}
					getSubDirs(path+"/"+dir[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dir;
	}
	
	/***
	 * Returns direct subdirectory from the current directory.
	 * Only folders are returned.
	 * @return	Array of String set with subdirectories
	 */
	public String[] getSubDirs() {
		return getSubDirs(path);
	}
	
	/***
	 * Get flies under the directory with the given extention.
	 * @param extention	extention to filter: "txt", "xml", "fna", etc.
	 * @return	Files with the given extention.
	 */
	public Vector<String> getDirectSubFiles(String extention) {
		Vector<String> fileList = new Vector<String>();
		String[] dir = null;
		File directory = new File(path);
		dir = directory.list();
		if (dir != null) {
			for (String file : dir) {
				if (file.endsWith("." + extention)) {
					fileList.add(path + "/" + file);
				}
			}
		}
		return fileList;
	}
	
	/***
	 * Only returns the direct sub-directories from the given path.
	 * @return the direct sub-directories
	 */
	public Vector<String> getDirectSubDirs() {
		Vector<String> dir = new Vector<String>();
		try{
			File directory = new File(path);
			if (directory.isDirectory()){
				String[] subDir = directory.list();
				for (String subDirectory : subDir) {
					File folder = new File(path+"/" + subDirectory);
					if(folder.isDirectory()) {
						dir.add(folder.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dir;
	}
}
