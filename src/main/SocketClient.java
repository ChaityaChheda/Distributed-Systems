package main;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import main.Util;

public class SocketClient {
	
	public static void main(String[] args) throws Exception {
		Socket s = new Socket("localhost", 9999); 
		
		Util util = new Util();
		  
        // to send data to the server 
		
        DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
  
        DataInputStream dis = new DataInputStream(s.getInputStream());
        
        BufferedReader kb = new BufferedReader(new InputStreamReader(System.in)); 
        
        
        String str_tosend, str_toreceive; 
    
        //Ask for username 
        str_toreceive = dis.readUTF();
        System.out.println(str_toreceive);

        //Send Username
    	str_tosend = kb.readLine();
    	String user = str_tosend;
    	dos.writeUTF(str_tosend);
    	
    	while(true){
    		str_toreceive = dis.readUTF();
    		System.out.println(str_toreceive);
    		
    		str_tosend = kb.readLine();
    		dos.writeUTF(str_tosend);
    		
    		if (str_tosend.equals("4")){
    			String fsize = dis.readUTF();
    			dos.writeUTF("Received the File Size: "+fsize);
    			util.saveFile(user+"-client",dis,Integer.parseInt(fsize));
    			dos.writeUTF("\n Received the Log File,Thank You");
    			str_toreceive = dis.readUTF();
    			System.out.println(str_toreceive);
    			str_tosend = kb.readLine();
    			
    			File f3 = new File(user+"-client");
    			
    			
    			dos.writeUTF(Long.toString(f3.length()));
    			
    			str_toreceive = dis.readUTF();
    			
        		util.sendLogFile(user+"-client", dos);
    		}
    		
    		
    		if(str_tosend.equals("5")){
    			break;
    		}
    		
    	}
  
	}
	
	

}