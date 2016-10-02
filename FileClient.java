package fileDownloadServer;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.json.*;

public class FileClient {
	//client sockets
	public static void main(String args[]) throws UnknownHostException, IOException
	{
		try
		{
			ClientSocketHandler c1=new ClientSocketHandler("DOWN D:\\chrs.txt");
			c1.start();
		}
		finally
		{
			
		}
	}

}

class ClientSocketHandler extends Thread
{
	String request=null;
	String response="";
	DataInputStream serverResponseStream=null;
	String selectedFilename="default.txt";
	
	public void getRequest(String s)
	{
		request=s;
	}
	
	ClientSocketHandler(String request) throws IOException
	{
		
		this.request=request;
		
	}
	
	void processInput() throws IOException
	{
		JsonString parent=null,curr=null,dirs=null,files=null;
		
		response=serverResponseStream.readUTF();
		
		
		
		JsonReader list=Json.createReader(new StringReader(response));
		JsonArray file_structure=list.readArray();
		//int i=0;
		for(JsonValue v:file_structure)
		{
			
			JsonObject object=(JsonObject)v;
			
			for(String i:object.keySet())
			{
				switch(i)
				{
				case "parent":
					parent=object.getJsonString("parent");
				case "this":
					curr=object.getJsonString("this");
				case "dirs":
					dirs=object.getJsonString("dirs");
				case "files":
					files=object.getJsonString("files");
				}
			}
			
		}
		
		 ArrayList<String> directoryList=
				 new ArrayList<String>(Arrays
				 .asList(dirs.getString()
						 .substring(1,dirs.getString()
								 .length()-1).split(", ")));
		
		 ArrayList<String> fileList=
				 new ArrayList<String>(Arrays
				 .asList(files.getString()
						 .substring(1,files.getString()
								 .length()-1).split(", ")));
		
		
		for(String i:directoryList)
			System.out.println(i);
		
		System.out.println();
		
		for(String i:fileList)
			System.out.println(i);
		
		
		
	}
	
	public void run()
	{
		this.setName(request);
		
		try(
				Socket socket=new Socket("localhost",1807);
				DataInputStream responseStream=new DataInputStream(socket.getInputStream());
				DataOutputStream requestStream=new DataOutputStream(socket.getOutputStream());	
				BufferedReader stdIn=new BufferedReader(new InputStreamReader(System.in));
				)
		{
			
			
			serverResponseStream=responseStream;
			processInput();
			String userRequest=stdIn.readLine();
			
			boolean continue_=true;
			while(continue_)
			{
				this.getRequest(userRequest);
				if(request.equals("STOP"))
				{	
					requestStream.writeUTF(request);
					continue_=false;
				}
				String cmd=request.split(" ", 2)[0];
				requestStream.writeUTF(request);
				requestStream.flush();
				switch(cmd)
				{
				case "DN":
				case "UP":
				{
					processInput();
					break;		
				}
				case "DOWN":
				{
					int errorCode=responseStream.readInt();
					switch(errorCode)
					{
					case 0:
					{
						setSelectedFilename("");
						try(FileOutputStream f=new FileOutputStream(selectedFilename);)
						{
							int rbuff;
							while((rbuff=responseStream.read())!=-1)
							{
								f.write(rbuff);
							}
							f.flush();
						}
						break;
					}
					case -1:
					{
						System.out.println("Oops! Can't download a directory!");
						break;
					}
					case -2:
					{
						System.out.println("Oops! The requested file does not "
								+ "exist on the remote system!");
						break;
					}
					}
					
					break;
				}
				}
				
			}
			
			
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("The command format is not valid!");
			System.out.println("Usage:\n======");
			System.out.println("<command> <filepath>");
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setSelectedFilename(String s)
	{
	
		if(request.split(" ",2)[0].equals("DOWN"))
		{
			if(s.equals(""))
				selectedFilename=request.split(" ",2)[1]
						.substring(request.split(" ", 2)[1].lastIndexOf("\\")+1);
			else
				selectedFilename=s;
		}
		
	}
}