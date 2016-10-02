package fileDownloadServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.json.*;

public class FileClient {
	//client sockets
	public static void main(String args[]) throws UnknownHostException, IOException
	{
		try
		{
			ClientSocketHandler c1=new ClientSocketHandler("DOWN C:\\ada\\a.exe");
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
	String response=null;
	
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
			
			request.split(" ", 2);
			requestStream.writeUTF(request);
			requestStream.flush();
			int rbuff;
			//rbuff=responseStream.read();
			FileOutputStream fileStream=new FileOutputStream("a.exe");
			while((rbuff=responseStream.read())!=-1)
			{
				
				 //response=response+(char)rbuff;
				 fileStream.write(rbuff);
			}
			System.out.println(response);
			fileStream.close();
			
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
}