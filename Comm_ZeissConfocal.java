import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import ij.IJ;
import java.lang.*;

import ij.plugin.*;
import ij.gui.GenericDialog;

public class Comm_ZeissConfocal implements PlugIn {
	
	String location = "HKCU\\SOFTWARE\\VB and VBA Program Settings\\OnlineImageAnalysis\\macro";
	String winReg_separator = "REG_SZ"	;
	
	public void run(String arg) {
		
		IJ.log(" ");
		IJ.log("Comm_ZeissConfocal: started");
		
		//static String message="my message";
		String[] actions = {"read registry", "write registry", "obtaining image"};
		String[] commands = {"do nothing", "bleach object", "image object"};
		//static String action	
        	GenericDialog gd = new GenericDialog("Communicate with Zeiss Confocal");
        	gd.addChoice("action: ", actions, actions[1]);
      	        gd.addChoice("command: ", commands, commands[0]);
      	        gd.addNumericField("offsetx: ", 0, 0);
      	        gd.addNumericField("offsety: ", 0, 0);
  	        gd.showDialog();
        	if(gd.wasCanceled()) return;
        	String action = (String)gd.getNextChoice();
        	String command = (String)gd.getNextChoice();
        	int offsetx = (int)gd.getNextNumber();
        	int offsety = (int)gd.getNextNumber();

		if (action=="wait for and load image" ) {
			obtainImage();
		}
     		
		writeToMacro(command, offsetx, offsety);
		readFromMacro();
		
    	}

	public void obtainImage() {
		
		IJ.log("Comm_ZeissConfocal: obtaining image...");
			
		String code = "do nothing";
		
		while ( ! code.equals("1")) {
 			code = getTrimmedRegistryValue("Code");
 			IJ.log("Comm_ZeissConfocal: current code value = "+code);
 			try {
 			   Thread.sleep(1000);
			} catch(InterruptedException ex) {
    			   Thread.currentThread().interrupt();
			}
 		}
				
		WindowsRegistry.writeRegistry(location, "Code", "do nothing");
	}


	public void writeToMacro(String code, int offsetx, int offsety) {
		WindowsRegistry.writeRegistry(location, "Code", code);
		WindowsRegistry.writeRegistry(location, "offsetx", ""+offsetx);
		WindowsRegistry.writeRegistry(location, "offsety", ""+offsety);
		IJ.log("Comm_ZeissConfocal: wrote to microscope");
	}

	public void readFromMacro() {
		
		String code = getTrimmedRegistryValue("Code");
		IJ.log("Comm_ZeissConfocal: read Code = "+ code);
						
	}

	public String getTrimmedRegistryValue(String key) {
		String temp1 = WindowsRegistry.readRegistry(location, key);
		String [] temp2 = temp1.split(winReg_separator); // extract only the value
		String value = temp2[1].trim(); // get rid of whitespaces
		return value;
	}


}



/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez 
 */
class WindowsRegistry {

    /**
     * 
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */

    public static final String writeRegistry(String location, String key, String value){
    	try {
    		
    		String cmd = "reg add " + '"' + location + "\" /v " + key + " /d \"" + value  + '"' + " /f ";
    		//IJ.log(""+cmd);
    		Process process = Runtime.getRuntime().exec(cmd);
    		
            	StreamReader reader = new StreamReader(process.getInputStream());
            	reader.start();
            	process.waitFor();
            	reader.join();
            	String output = reader.getResult();
    		//IJ.log(""+output);
    		return "ok";
    	}
    	catch (Exception e) {
            return null;
        }
    }
    
    public static final String readRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            String cmd = "reg query " + '"' + location + "\" /v " + key;
    	    //IJ.log(""+cmd);
            Process process = Runtime.getRuntime().exec(cmd);
    		
            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();
            //IJ.log(""+output);
	    return output;	
	   	
	    // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            //if( !output.contains("\t")){
            //        return "WinReg Error: string does not contain tabs :-(";
            //}
            
            // Parse out the value
            //String[] parsed = output.split("\t");
            //return parsed[parsed.length-1];
        }
        catch (Exception e) {
            return "WinReg Error: something unknown went wrong :-(";
        }

    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            }
            catch (IOException e) { 
        }
        }

        public String getResult() {
            return sw.toString();
        }
    }

	public static void checkImageJ(){
	       IJ.log("You successfully imported the WindowsRegistry java class to ImageJ.");
	}
    
   public static void main(String[] args) {

        // Sample usage
   	IJ.log("read: "+WindowsRegistry.readRegistry("HKLM\\SOFTWARE\\MyCo","Data"));
   	WindowsRegistry.writeRegistry("HKLM\\SOFTWARE\\MyCo","Data","suupi");
   	IJ.log("read: "+WindowsRegistry.readRegistry("HKLM\\SOFTWARE\\MyCo","Data"));
   	
   }
}