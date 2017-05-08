package javaparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javaparser.UMLParser;

/**
* The main Class from where the UMLParser is called
* This class takes 2 input parameters from the console
* 1. Folder name - The folder containing the Java files that need to be scanned to build the class diagram
* 2. Output file name - the file name where the class diagram will be saved, with the extension
*/
public class JavaToUMLTest {

	public static void main(String[] args)
	{
		/*if(args.length == 0)
		{
			System.out.println("Please provide input parameters - <folder path of source files> <output file name>");
			return;
		}
		else
		{
			if(args.length == 1)
			{
				System.out.println("Please provide output file name with extension");
				return;
			}
		}
		
		System.out.println("Input Folder Provided - "+args[0]);
		System.out.println("Output file name will be - "+args[1]);
		System.out.println("Please wait, Creating UML Class diagram ...");
*/		UMLParser umlParser = new UMLParser(args[0]);
		String XML = umlParser.parse();
		String extension = args[1].substring(args[1].indexOf('.')+1,args[1].length());
		try
		{
			
			URL url = new URL(XML + extension);
			System.out.print(XML);
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			{
			   out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			
			FileOutputStream fos = new FileOutputStream(args[1]);
			fos.write(response);
			fos.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Error");
		}
	}
}
