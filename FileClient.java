package fileDownloadServer;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.json.*;



public class FileClient {
	//client sockets
	public static void main(String args[]) throws UnknownHostException, IOException
	{
		try(Scanner s=new Scanner(System.in);)
		{
			while(true)
			{
				ClientSocketHandler.printCommandList();
				System.out.println("Usage"+System.lineSeparator()+"<cmd> <path>");
				String userRequest=s.nextLine();
				new ClientSocketHandler(userRequest).start();
			}
			
			
			
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
		
		this.request=request.replace("/", "\\");
		this.request=this.request.replace("\\", "\\");
		this.request=this.request.replace("//", "\\");
		//System.out.println("Constructor: request:"+this.request);
	}
	
	void processInput() throws IOException
	{
		@SuppressWarnings("unused")
		JsonString parent=null,curr=null;
		JsonString dirs=null,files=null;
		
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
		
		System.out.println("++++++"+System.lineSeparator()+"++++++");
		System.out.println();
		System.out.println("Directory List");
		System.out.println();
		for(String i:directoryList)
			System.out.println(i);
		
		System.out.println("++++++"+System.lineSeparator()+"++++++");
		System.out.println();
		System.out.println("Files List");
		System.out.println();
		for(String i:fileList)
			System.out.println(i);
		
		
		
	}
	public static enum methodList
	{
		HOME,DOWN,UP,DN;
		
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
	public static void printCommandList()
	{
		System.out.println("Commands Available on this system:");
		System.out.println();
		for(Object i:methodList.values())
		{
			System.out.println(i.toString());
			
			switch(i.toString())
			{
			case "HOME":
			{
				System.out.println("This command prints "
						+System.lineSeparator()+ "the remote systems home direcotry"
						+System.lineSeparator());
				break;
			}
			case "DOWN":
			{
				System.out.println("Downloads the requested file to "
						+System.getProperty("user.dir")+System.lineSeparator());
				break;
			}
			case "UP":
			{
				System.out.println("Navigates up from the specified directory"
						+System.lineSeparator()+ "(Goes to the parent directory)"
						+System.lineSeparator());
				break;
			}
			case "DN":
			{
				System.out.println("Navigates down to the specified directory"
						+System.lineSeparator());
			}
			}
		}
	}
	public void run()
	{
		this.setName(request);
		
		try(
				Socket socket=new Socket("localhost",1807);
				DataInputStream responseStream=new DataInputStream(socket.getInputStream());
				DataOutputStream requestStream=new DataOutputStream(socket.getOutputStream());	
				//BufferedReader stdIn=new BufferedReader(new InputStreamReader(System.in));
				)
		{
			
			
			serverResponseStream=responseStream;
			System.out.println("Client Active!");
			//System.out.println(responseStream.readUTF());
			//processInput();
			
			
			//boolean continue_=true;
			//while(continue_)
			//{
				//System.out.println(":>>>:");
				//String userRequest=stdIn.readLine();
				//this.getRequest(userRequest);
				requestStream.writeUTF(request);
				requestStream.flush();
				if(request.equals("HOME"))
				{	
					//System.out.println("here!");
					System.out.println(responseStream.readUTF());	
				}
				else
				{
					String cmd=request.split(" ", 2)[0];
					
					//requestStream.flush();
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
							//System.out.println(selectedFilename);
							try(FileOutputStream f=new FileOutputStream(selectedFilename);)
							{
								int rbuff;
								while((rbuff=responseStream.read())!=-1)
								{
									f.write(rbuff);
									
								}
								//f.close();
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
				
				
			//}
			
			
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("The command format is not valid!");
			System.out.println("Usage:\n======");
			System.out.println("<command> <filepath>");
		} 
		catch (IOException e) 
		{
			
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