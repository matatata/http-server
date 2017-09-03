package de.infomac.webserver.http.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;


public class ContentTypeMap {
	private HashMap map = new HashMap();
	
	// reads the mapping-information from a mime.types File
	
	private void setupDefaults(){
		this.put("txt","text/plain");
		this.put("htm","text/html");
		this.put("html","text/html");
		this.put("gif","iamge/gif");
		this.put("jpg","image/jpeg");
		this.put("jpeg","image/jpeg");
	}
	
	/**
	 * initializes the Map with contents from a file
	 */
	
	public ContentTypeMap(String fileName) throws IOException, java.io.FileNotFoundException{
		readFromFile(fileName);
	}
	
	/**
	 * @param with_defaults if true the map will be initialised witsh some common defaults
	 */
	public ContentTypeMap(boolean with_defaults){
		if(with_defaults)
		    setupDefaults();
	}

	
	
	/**
     * 
     * @param fileName
     *            (ignores the case)
     * @return content-type
     */
    public String get(String fileName) {
        fileName = fileName.toLowerCase();

        String contentType = null;

        int p = fileName.lastIndexOf('.');
        if (p != -1) { //file has a suffix
            String suffix = fileName.substring(p);
            if (suffix.length() > 0)
                suffix = suffix.substring(1);

            contentType = (String) map.get(suffix);
        }

        return (contentType != null) ? contentType : "";
    }
    
	/**
     * 
     * @param suffix
     *            (ignores the case)
     * @return content-type
     */
    public String getForSuffix(String suffix) {
        return (String) map.get(suffix.toLowerCase()); 
    }
	
	/**
	 * 
	 * @param suffix (ignores the case)
	 * @param contenttype
	 */
	public void put(String suffix,String contenttype){
		map.put(suffix.toLowerCase(),contenttype.toLowerCase());
	}
	
	
	
	public void readFromFile(String fileName) throws IOException, java.io.FileNotFoundException {
		FileInputStream fis = new FileInputStream(new File(fileName));
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String line;
		
		while((line = br.readLine()) != null){
			if(line.length() == 0) // ignore empty lines
				continue;
			if(line.startsWith("#")) //ignore comments
				continue;
			
			StringTokenizer tokens = new StringTokenizer(line);
			String type = tokens.nextToken();
			while(tokens.hasMoreElements()){
				String suffix = tokens.nextToken();
				put(suffix,type); // map each suffix to the content-type
			}
		
			
		}
		
		br.close();
		isr.close();
		fis.close();
	}
	
	
	// just testing
	public static void main(String []args){
		try {
			ContentTypeMap mm = new ContentTypeMap(args[0]);
			System.out.println(mm.map);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
}
