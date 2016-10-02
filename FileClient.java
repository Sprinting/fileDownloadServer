package fileDownloadServer;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
//import java.io.FileNotFoundException;
import java.io.IOException;
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
	String selectedFilename="default.txt";
	ClientSocketHandler(String request) throws IOException
	{
		
		this.request=request;
		//responseStream=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//requestStream=new PrintWriter(socket.getOutputStream(),true);
	}
	
	public void run()
	{
		this.setName(request);
		
		try(
				Socket socket=new Socket("localhost",1807);
				DataInputStream responseStream=new DataInputStream(socket.getInputStream());
				DataOutputStream requestStream=new DataOutputStream(socket.getOutputStream());	
				
				)
		{
			
			String cmd=request.split(" ", 2)[0];
			String response=null;
			requestStream.writeUTF(request);
			requestStream.flush();
			
			
			switch(cmd)
			{
			case "DN":
			case "UP":
			{
				JsonString parent=null,curr=null,dirs=null,files=null;
				
				response=responseStream.readUTF();
				
				
				
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
			case "DOWN":
			{
				setSelectedFilename("");
				try(FileOutputStream f=new FileOutputStream(selectedFilename);)
				{
					int rbuff;
					while((rbuff=responseStream.read())!=-1)
					{
						f.write(rbuff);
					}
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