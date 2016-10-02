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

public class FileClient {
	//client sockets
	public static void main(String args[])
	{
		
	}

}

class ClientSocketHandler extends Thread
{
	String request;
	Socket socket;
	ClientSocketHandler(String request,Socket socket) throws IOException
	{
		this.socket=socket;
		this.request=request;
		BufferedReader responseStream=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter requestStream=new PrintWriter(socket.getOutputStream(),true);
	}
}