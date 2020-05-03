package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import main.Client;

public class Util {
	
    synchronized boolean writeMaptoFile(HashMap<String,Client> clients_map)
    {
        try 
        {

            FileOutputStream fileOut = new FileOutputStream("data/map.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(clients_map);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in data/map.ser");
        } 
        catch (IOException i) 
        {
            i.printStackTrace();
            return false;
        }

        return true;
    }
	
	synchronized boolean  Compare2Files(String file1,String file2) throws IOException{
		BufferedReader reader1 = new BufferedReader(new FileReader(file1));
        
        BufferedReader reader2 = new BufferedReader(new FileReader(file2));
         
        String line1 = reader1.readLine();
         
        String line2 = reader2.readLine();
         
        boolean areEqual = true;
         
       
        while (line1 != null || line2 != null)
        {
            if(line1 == null || line2 == null)
            {
                areEqual = false;
                 
                break;
            }
            else if(! line1.equalsIgnoreCase(line2))
            {
                areEqual = false;
                 
                break;
            }
             
            line1 = reader1.readLine();
             
            line2 = reader2.readLine();
           
        }

        reader1.close();
         
        reader2.close();
        System.out.println(areEqual);
    
         
        return areEqual;
        
    }
	
	
    synchronized void sendLogFile(String file,DataOutputStream dos) throws IOException
    {
        	
        	File f = new File(file);
    		FileInputStream fis = new FileInputStream(file);
    		byte[] buffer = new byte[(int) f.length()];
    		
    		while (fis.read(buffer) > 0) {
    			dos.write(buffer);
    		}
    		
    		fis.close();

        	
    }


    synchronized void saveFile(String filename,DataInputStream dis,int fsize) throws IOException{
    	
    	FileOutputStream fos = new FileOutputStream(filename);
    	byte[] buffer = new byte[4096];
    	
    	int filesize = fsize; // Send file size in separate msg
    	int read = 0;
    	int totalRead = 0;
    	int remaining = filesize;
    	while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
    		totalRead += read;
    		remaining -= read;
    		System.out.println("read " + totalRead + " bytes.");
    		fos.write(buffer, 0, read);
    	}
    	System.out.println("Saved Client file");
    	fos.close();


    }


}
