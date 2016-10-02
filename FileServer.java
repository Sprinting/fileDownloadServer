package fileDownloadServer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.json.*;

public class FileServer  {
	//server sockets
	public static void main(String args[])
	{
		
		try(ServerSocket s=new ServerSocket(1807);){
			boolean listening=true;
			while(listening)
			{
				Socket clientSocket=s.accept();
				System.out.println("Connected!");
				new FileSocketHandler(clientSocket).start();
				
				
			}
			
			System.out.println(System.getProperty("user.dir"));
			
			
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}

}

class FileSocketHandler extends Thread
{
	DataInputStream requestStream=null;
	DataOutputStream fileStream=null;
	PrintWriter logStream=null;
	Socket fileSocket=null;
	
	public static enum methodList
	{
		DOWN,UP,DN;
		
		public static Boolean checkService(String method)
		{
			for(methodList i:methodList.values())
			{
				if(method.equals(i.toString()))
					return true;
			}
			return false;
		}
	}
	
	
	
	FileSocketHandler(Socket socket) throws IOException
	{
		fileSocket=socket;
		requestStream=null;
		fileStream=null ;
		this.logStream=new PrintWriter(new FileWriter("server_logs.txt",true));
		sendLog("_init_:File Handler passes socket to a thread");
	}
	
	// for testing 
	FileSocketHandler() throws IOException
	{
		requestStream=new DataInputStream(System.in);
		fileStream=new DataOutputStream(System.out);
		logStream=new PrintWriter(new FileWriter("server_logs_test.txt",true));
	    System.out.println(sendLog("_init_:File Handler started for testing"));
	}	
	synchronized public String sendLog(String s)
	{
		String log= "[LOG]:: "+s+ " [TIME] :: "+System.currentTimeMillis()+System.lineSeparator();
		logStream.append(log);
		return log;
	}
	 synchronized public String sendError(String s)
	{
		String error ="[ERROR]:: "+s+"[TIME] :: "+System.currentTimeMillis()+System.lineSeparator();
		logStream.append(error);
		return error;
	}
	
	public void run() 
	{
		String requestString=null;
		String command,filepath;
		
		try(
				DataInputStream in=new DataInputStream(fileSocket.getInputStream());
				DataOutputStream out= new DataOutputStream(fileSocket.getOutputStream());
			)
		{
			requestStream=in;
			fileStream=out;
			
			 System.out.println("Command!");
			 
		
				 //System.out.println("YOHOO");
			requestString=requestStream.readUTF();
			
			 //System.out.println("Not blocked!");
			 //requestString=requestStream.readLine();
			 String requestList[]=requestString.split(" ",2);
			 //System.out.println(requestList);
			command=requestList[0];
			System.out.println("Command :: "+command);
			filepath=requestList[1];
			this.setName(filepath);
			boolean Success;
			boolean hasMethod=FileSocketHandler.methodList.checkService(command);
			//System.out.println("hasMethod :: "+hasMethod);
			
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
			//e.printStackTrace();
			System.out.println(sendError("Catch block"+e.getMessage()));
			
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("The command format is not valid!");
			System.out.println("Usage:"+System.lineSeparator()+"======");
			System.out.println("<command> <filepath>"+System.lineSeparator());
		}
		finally
		{
			
			try {
				if(logStream!=null)
				 logStream.close();
				if(fileStream!=null)
				 fileStream.close();
				if(requestStream!=null)
				 requestStream.close();
				if(fileSocket!=null)
				 fileSocket.close();
			} catch (IOException e) {
				
				System.out.println("finally block!");
				
			}
		}
		
	}

	 public  boolean do_DN(String filepath) throws IOException  
	 {
		 System.out.println("DO_DN");
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
			System.out.println(sendError(this.getName()+" :: The chosen file is not a directory. Can not explore non-directory"));
			return false;
		 }
		else if(!file.exists())
		 {
			 System.out.println(sendError(this.getName()+" :: No such file found"));
			 return false;
		 }
		 
		 //JsonArray final_,next_,prev_,curr_;
		 //System.out.println("DO_DN_1");
		 JsonArray file_structure=
		 Json.createArrayBuilder()
		 .add(Json.createObjectBuilder().add("parent", prev
				 .substring(0,file.getAbsolutePath().lastIndexOf(File.separatorChar)+1)))
		 .add(Json.createObjectBuilder().add("this", prev))
		 .add(Json.createObjectBuilder().add("dirs", dir.toString()))
		 .add(Json.createObjectBuilder().add("files", files.toString()))
		 .build();
		 
		 fileStream.writeUTF(file_structure.toString());
		 System.out.println(sendLog(this.getName()+" :: [do_DN]File Sturcture Written"));
		 return true;
		 
	}

	 boolean do_UP(String filepath) throws IOException 
	 {
		
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
			System.out.println(sendError(this.getName()+" :: The chosen file is not a directory. Can not explore non-directory"));
			return false;
		 }
		 else if(!file.exists())
		 {
			 System.out.println(sendError(this.getName()+" :: No such file found"));
			 return false;
		 }
		 
		 file_structure=
				 Json.createArrayBuilder().add(Json.createObjectBuilder().add("parent", prev))
				 .add(Json.createObjectBuilder().add("this", file.getAbsolutePath()))
				 .add(Json.createObjectBuilder().add("dirs", dir.toString()))
				 .add(Json.createObjectBuilder().add("files", files.toString()))
				 .build();
		 fileStream.writeUTF(file_structure.toString());
		 System.out.println(sendLog(this.getName()+" :: [do_UP]File Sturcture Written"));		 
		 return true;
	 }

	 boolean do_DOWN(String filepath) throws IOException 
	 {
		File f=new File(filepath);
		if(f.isDirectory())
			{
				System.out.println(sendError(this.getName()+" :: Can not download directories!"));
				return true;
			}
		else if(!f.exists())
			{
			System.out.println(sendError(this.getName()+" :: Unfortunately, the file does not exist"));
			return false;
			}
			else
		{
			//TODO replace with fileStream and requestStream
			FileInputStream tempFileReader=new  FileInputStream(filepath);
			
			int cbuf;
			while((cbuf=tempFileReader.read())!=-1)
			{
				
				fileStream.write(cbuf);;
				
			}
			tempFileReader.close();	
			System.out.println(sendLog(this.getName()+" :: File "+filepath+" successfully downloaded"));
			System.out.println("DONE");
		}
		 return true;
	}
}