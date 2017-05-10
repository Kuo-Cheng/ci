/*
* MergeXml.java - Merge the generated jsp web.xml with the default web.xml.
* $Id:$
* $Rev:$
* $Date:$
* $Author:$
* 
*/
package build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
* This file is for the build on the windows platform without the linux merge files function.
* Copy the web.xml before running this jar.
* MergeXml uses the nio to improve the performance.
* 
* Author: Alex Chen
*/
public class MergeXml {
	
	/** The jsp replace tag on the original target web.xml. */
	private boolean jspReplaceTag = false;
	
	private static int READ_MODE_WEB_BASE = 0;
	
	public boolean getJspReplaceTag(){
		return this.jspReplaceTag;
	}
	
	public void setJspReplaceTag(boolean jspTag){
		this.jspReplaceTag = jspTag;
	}
	
	/**
	* Merge the files by web.xml and generated_web.xml.
	* The web.xml must have the one line '<!--ReplaceJSP -->', read file to this line.
	*/
	private String readWebXml(String filename, int readMode){
		
		String strCreateWebBase="";
		
		InputStream in = null;
		File fileTarget = null;
		BufferedReader br =null;
		StringBuilder sbWebXml = new StringBuilder();
		try {
			fileTarget = new File(filename);
			
			in = new FileInputStream(fileTarget);
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"), 1024);
			
			String str="";
			
			//Set the jsp replace flag to false.
			if(readMode == READ_MODE_WEB_BASE){
				setJspReplaceTag(false);
			}
			
			
			while ((str = br.readLine()) != null){
				
				if(str.contains("<!--ReplaceJSP -->")){
					if(readMode == READ_MODE_WEB_BASE){
						setJspReplaceTag(true);
					}
					sbWebXml.append("\n");
					break;
				}
				sbWebXml.append(str).append("\n");
			}//end while

			strCreateWebBase = sbWebXml.toString().trim();
	    } catch (IOException ex){
	        ex.printStackTrace();
	    } finally{
	    	try{
	    		if(in!=null){
	    			in.close();
	    			in=null;
	    		}
	    		if(br!=null){
	    			br.close();
	    			br=null;
	    		}
	    	}catch(Exception e){
	    		System.out.println(e.getMessage());
	    	}
	    }
		
		return strCreateWebBase ;
	}
	
	@SuppressWarnings("resource")
	private void backupFile(File source, File dest){
		// Generate the web.xml to temp web.xml.b !
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    try {
			sourceChannel = new FileInputStream(source).getChannel();
	        destChannel = new FileOutputStream(dest).getChannel();
	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (FileNotFoundException e) {
			e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}finally{
           try {
        	   if(sourceChannel!=null)	sourceChannel.close();
        	   if(destChannel!=null)	destChannel.close();
           } catch (IOException e) {
				e.printStackTrace();
           }
       }
	}
	
	/**
	* 1 parameters mode
	* java -jar MergeXml.jar one
	* one - D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF
	* 
	* 3 parameters mode
	* java -jar MergeXml.jar one two three
	* one - D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\web.xml
	* two - D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\generated_web.xml
	* three - D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\tmp\\web.xml
	* @param fileWebXml args[0]
	* @param generatedJspXml args[1]
	*/
	public static void main(String[] args){
		System.out.println("Merge the web.xml and the generated_web.xml!!! ");
		MergeXml mergeXml = new MergeXml();
		
		//The default values
		String fileWebXml =  "./WEB-INF/web.xml";
		String generatedJspXml = "./WEB-INF/generated_web.xml";
		String outputXml = "./WEB-INF/web.xml";
		
		if(args!=null){
			//Suppose thet are located on the same folder, just one folder path.
			if(args.length==1){
				System.out.println(args[0]);
				
				fileWebXml = args[0]+"\\web.xml";
				generatedJspXml = args[0]+"\\generated_web.xml";
				outputXml = args[0]+"\\web.xml";
			}else if(args.length==3){
				System.out.println(args[0]);
				System.out.println(args[1]);
				System.out.println(args[2]);
				fileWebXml = args[0];
				generatedJspXml = args[1];
				outputXml = args[2];
			}else{
				System.out.println("Error: Wrong parameters!");
			}
			
		}
		
		String strCreateWebBase = "";
		String strJspcWebBase = "";
		
		String xmlBak =  "./WEB-INF/web.xml.b";
		
		File webFile = new File(fileWebXml);
		File xmlBakFile = new File(xmlBak);
		File outputXmlFile = new File(outputXml);
		
		try{
			if(xmlBakFile.exists()){//do backup the original web.xml one time and read it when execute build
				strCreateWebBase = mergeXml.readWebXml(xmlBak, READ_MODE_WEB_BASE);
			}else{
				mergeXml.backupFile(webFile, xmlBakFile);
				strCreateWebBase = mergeXml.readWebXml(fileWebXml, READ_MODE_WEB_BASE);
			}			
			
			strJspcWebBase = mergeXml.readWebXml(generatedJspXml, -1);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BufferedWriter bw = null;
		//Check jspReplaceTag, if it's true, do the merge,
		//false, skip it.
		if(mergeXml.getJspReplaceTag()==true){
			try {
				 if(outputXmlFile!=null && outputXmlFile.exists()){
					 outputXmlFile.delete();
					 outputXmlFile = new File(outputXml);
				 }
				 // APPEND MODE SET HERE
				 bw = new BufferedWriter(new FileWriter(outputXmlFile, true));
				 bw.write(strCreateWebBase+"\n"+strJspcWebBase+"\n</web-app>");
				 bw.newLine();
				 bw.flush();
			} catch (IOException ioe) {
				 ioe.printStackTrace();
			} finally {// always close the file
				 if (bw != null){ 
					 try {
						 bw.close();
					 } catch (IOException ioe2) {
						 ioe2.printStackTrace();// just ignore it
					 }
				 } 
			} // end try/catch/finally
		}else{
			System.out.println("NOTE:The jsp replace tag flag="+mergeXml.getJspReplaceTag());
		}
	    
		
		
		try{
			if(xmlBakFile.exists()){
				xmlBakFile.delete();
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Done!!! ");
	}

	@SuppressWarnings("unused")
	private void writeWebXml(String strCreateWebBase,String strJspcWebBase ,File outFile) {
		 
	    FileOutputStream outputFile = null; 
	    try {
	    	outputFile = new FileOutputStream(outFile, false);
	      
	      	System.out.println("File stream created successfully.");
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace(System.err);
	    }
	    FileChannel outChannel = outputFile.getChannel();
	    
	    ByteBuffer buf = ByteBuffer.allocate(1024);
	    
	    System.out.println("New buffer:           position = " + buf.position() + "\tLimit = "+ buf.limit() + "\tcapacity = " + buf.capacity());
	    
	    for (char ch : (strCreateWebBase+strJspcWebBase+"</web-app>").toCharArray()) {
	    	buf.putChar(ch);
	    }
	    
	    System.out.println("Buffer after loading: position = " + buf.position() + "\tLimit = "+ buf.limit() + "\tcapacity = " + buf.capacity());buf.flip(); 
	    System.out.println("Buffer after flip:   position = " + buf.position() + "\tLimit = "+ buf.limit() + "\tcapacity = " + buf.capacity());
	    try {
	    	outChannel.write(buf);
	    } catch (IOException e) {
	    	e.printStackTrace(System.err);
	    }finally{
	    	if(outputFile!=null){
	    		try {
					outputFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	System.out.println("Buffer contents written to file.");
	    }
		
	}
}
