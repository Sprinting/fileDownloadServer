package fileDownloadServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import javax.json.*;

public class FileServer  {
	//server sockets
	public static void main(String args[])
	{
		FileSocketHandler f=new FileSocketHandler();
		try {
			f.do_DN("C://ada");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				if(method==i.toString())
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
			
			requestString=requestStream.readLine();
			String requestList[]=requestString.split(" ",2);
			 //System.out.println(requestList);
			command=requestList[0];
			filepath=requestList[1];
			boolean Success;
			if(methodList.checkService(command))
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
				sendError("This service is not yet implemented\n");
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println(sendError(e.getMessage()));
			
		}
		
	}

	 public  boolean do_DN(String filepath) throws IOException {
		
		 File file=new File(filepath);
		 ArrayList<String> dir=new ArrayList<String>(),files=new ArrayList<String>();
		 String prev=null;
		 if(file.isDirectory())
		 {
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
				prev=filename.getParent();
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
		 .add(Json.createObjectBuilder().add("parent", prev))
		 .add(Json.createObjectBuilder().add("dirs", dir.toString()))
		 .add(Json.createObjectBuilder().add("files", files.toString())).build();
		 
		 //fileStream.println(file_structure.toString());
		 new PrintWriter(System.out,true).println("LOL: "+file_structure.toString());
		 System.out.println(sendLog("[do_DN]File Sturcture Written"));
		//.out.println(s);
		 
		 return true;
		 
	}

	 boolean do_UP(String filepath) {
		// TODO Auto-generated method stub
		 return true;
	}

	 boolean do_DOWN(String filepath) {
		// TODO Auto-generated method stub
		 return true;
	}
}