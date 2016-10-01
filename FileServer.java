package fileDownloadServer;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import javax.json.*;

public class FileServer  {
	//server sockets
	public static void main(String args[])
	{
		FileSocketHandler f=new FileSocketHandler();
		//try {
			//f.do_UP("C:\\8787 MassXP v0.4\\Config");
			//System.out.println("+++\n\n\n+++");
			//f.do_DN("C:\\8787 MassXP v0.4\\Config");
			
			f.start();
			
		//} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
	}

}

class FileSocketHandler extends Thread
{
	BufferedReader requestStream=null;
	PrintWriter fileStream=null;
	Socket fileSocket=null;
	
	public static enum methodList
	{
		DOWN,UP,DN;
		
		public static Boolean checkService(String method)
		{
			for(methodList i:methodList.values())
			{
				if(method.equals(i.toString()));
					return true;
			}
			return false;
		}
	}
	
	
	
	FileSocketHandler(Socket socket) throws IOException
	{
		sendLog("_init_:File Handler passes socket to a thread");
		requestStream=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		fileStream=new PrintWriter(socket.getOutputStream(),true);
	}
	
	// for testing 
	FileSocketHandler()
	{
		System.out.println(sendLog("_init_:File Handler started for testing"));
	}
	public String sendLog(String s)
	{
		String log= "[LOG]:: "+s+ " [TIME] :: "+System.currentTimeMillis();
		return log;
	}
	public String sendError(String s)
	{
		String error ="[ERROR]:: "+s+"[TIME] :: "+System.currentTimeMillis();
		return error;
	}
	
	public void run() 
	{
		String requestString,command,filepath;
		
		try
		{
			System.out.println("Command!");
			//TODO requestString=requestStream.readLine();
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			requestString=in.readLine();
			
			String requestList[]=requestString.split(" ",2);
			 //System.out.println(requestList);
			command=requestList[0];
			System.out.println("Command :: "+command);
			filepath=requestList[1];
			this.setName(filepath);
			boolean Success;
			boolean hasMethod=FileSocketHandler.methodList.checkService(command);
			System.out.println("hasMethod :: "+hasMethod);
			
			if(hasMethod)
			{
				switch(command)
				{
					case "DOWN":
						Success=do_DOWN(filepath);
						break;
					case "UP":
						Success=do_UP(filepath);
						break;
					case "DN":
						Success=do_DN(filepath);
						break;
				}
				
			}
			else
			{
				Success=false;
				System.out.println(sendError(Success+ " :: This service is not yet implemented\n"));
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println(sendError(e.getMessage()));
			
		}
		
	}

	 public  boolean do_DN(String filepath) throws IOException  
	 {
		
		 File file=new File(filepath);
		 ArrayList<String> dir=new ArrayList<String>(),files=new ArrayList<String>();
		 String prev=null;
		 
		 if(file.isDirectory())
		 {
			prev=file.getAbsolutePath();
			for(File filename:file.listFiles())
			{
				if(filename.isDirectory())
				{
					dir.add(filename.getName());
				}
				else if(filename.isFile())
				{
					files.add(filename.getName());
				}
				
				
				
				
			}
		 }
		else if(!file.isDirectory() && file.exists())
		{
			System.out.println(file.toString());
			System.out.println(sendError("The chosen file is not a directory. Can not explore non-directory"));
			return false;
		 }
		else if(!file.exists())
		 {
			 System.out.println(sendError("No such file found"));
			 return false;
		 }
		 
		 //JsonArray final_,next_,prev_,curr_;
		 
		 JsonArray file_structure=
		 Json.createArrayBuilder()
		 .add(Json.createObjectBuilder().add("parent", prev
				 .substring(0,file.getAbsolutePath().lastIndexOf(File.separatorChar)+1)))
		 .add(Json.createObjectBuilder().add("this", prev))
		 .add(Json.createObjectBuilder().add("dirs", dir.toString()))
		 .add(Json.createObjectBuilder().add("files", files.toString()))
		 .build();
		 
		 //fileStream.println(file_structure.toString());
		//TODO link to actual stream before submitting
		 new PrintWriter(System.out,true).println("LOL: "+file_structure.toString());
		 System.out.println(sendLog("[do_DN]File Sturcture Written"));
		//.out.println(s);
		 
		 return true;
		 
	}

	 boolean do_UP(String filepath) throws IOException 
	 {
		// TODO Auto-generated method stub
		 File file=new File(filepath.substring(0,filepath.lastIndexOf(File.separatorChar)+1));
		 //System.out.println(new File(filepath).getAbsolutePath());
		 System.out.println(file.getAbsolutePath());
		 
		 JsonArray file_structure;
		 
		 ArrayList<String> dir=new ArrayList<String>(),files=new ArrayList<String>();
		 String prev=null;
		 
		 
		 if(file.isDirectory())
		 {
			 prev=file.getAbsolutePath()
					 .substring(0,file.getAbsolutePath().lastIndexOf(File.separatorChar)+1);
			 
			 for(File filename:file.listFiles())
				{
					if(filename.isDirectory())
					{
						dir.add(filename.getName());
					}
					else if(filename.isFile())
					{
						files.add(filename.getName());
					}	
				}
		 	}
		 else if(file.isFile() && file.exists())
		 {
			System.out.println(file.toString());
			System.out.println(sendError("The chosen file is not a directory. Can not explore non-directory"));
			return false;
		 }
		 else if(!file.exists())
		 {
			 System.out.println(sendError("No such file found"));
			 return false;
		 }
		 
		 file_structure=
				 Json.createArrayBuilder().add(Json.createObjectBuilder().add("parent", prev))
				 .add(Json.createObjectBuilder().add("this", file.getAbsolutePath()))
				 .add(Json.createObjectBuilder().add("dirs", dir.toString()))
				 .add(Json.createObjectBuilder().add("files", files.toString()))
				 .build();
		 //TODO link to actual stream before submitting;
		 new PrintWriter(System.out,true).println("LOL: "+file_structure.toString());
		 System.out.println(sendLog("[do_UP]File Sturcture Written"));
		 
		 return true;
	 }

	 boolean do_DOWN(String filepath) throws IOException 
	 {
		File f=new File(filepath);
		if(f.isDirectory())
			{
				System.out.println(sendError("Can not download directories!"));
				return true;
			}
		else if(!f.exists())
			{
			System.out.println(sendError("Unfortunately, the file does not exist"));
			return false;
			}
			else
		{
			BufferedReader tempFileReader=new BufferedReader(new FileReader(filepath));
			PrintWriter tempFileWriter=new PrintWriter(new FileWriter("C:\\ada\\HaxLogsCopy.txt"),true);
			String cbuf;
			while((cbuf=tempFileReader.readLine())!=null)
			{
				tempFileWriter.println(cbuf);
				//tempFileWriter.println("\n");
			}
			tempFileReader.close();	
		}
		 return true;
	}
}
