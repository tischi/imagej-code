/***
 * @author Christian Tischer
 * using a (modified) class for Windows Registry communication, written by Oleg Ryaboy
 */


import ij.*;
import ij.plugin.*;
import ij.gui.GenericDialog;
import ij.measure.*;
import ij.plugin.frame.RoiManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.*;


public class Microscope_Communicator implements PlugIn {
	
	String plugin_name = "Communicator";
	String microscope = "LSM780";
	String winreg_location = "HKCU\\SOFTWARE\\VB and VBA Program Settings\\OnlineImageAnalysis\\macro";; 
	String winreg_separator = "REG_SZ";
	
	public void run(String arg) {
		
		IJ.log(" ");
		IJ.log(""+plugin_name+": started");
	
		String[] microscopes = {"LSM780"};
		String[] actions = {"read status", "submit command", "obtain image"};
		String[] commands = {"do nothing", "image selected particle", "image at x, y"};
		//static String action	
        	GenericDialog gd = new GenericDialog("Microscope Communication");
        	gd.addChoice("microscope: ", microscopes, microscopes[0]);
        	gd.addChoice("action: ", actions, actions[1]);
      	        gd.addChoice("command: ", commands, commands[0]);
      	        //gd.addNumericField("x: ", 0, 0);
      	        //gd.addNumericField("y: ", 0, 0);
  	        gd.showDialog();
        	if(gd.wasCanceled()) return;
        	microscope = (String)gd.getNextChoice();
        	String action = (String)gd.getNextChoice();
        	String command = (String)gd.getNextChoice();
        	//int offsetx = (int)gd.getNextNumber();
        	//int offsety = (int)gd.getNextNumber();
        	// todo: maybe get rid of the offset variables
		int offsetx = 0;
		int offsety = 0;
		
		
		
		IJ.log(""+plugin_name+": user values retrieved");
		IJ.log(""+plugin_name+": action = "+action);
	


		if (microscope.equals("LSM780")) {
			winreg_location = "HKCU\\SOFTWARE\\VB and VBA Program Settings\\OnlineImageAnalysis\\macro";
		} 

		// action choice
		if (action.equals("obtain image")) {
			obtainImage();
		}
		else if (action.equals("submit command")) {
			writeToMacro(command, offsetx, offsety);
		}
     		else if (action.equals("read status")) {
			readFromMacro();
     			
     		}

		IJ.log(""+plugin_name+": done");
		
    	}

	public void obtainImage() {
		
		IJ.log(""+plugin_name+": obtaining image...");
			
		String code = "do nothing";
		
		while ( ! code.equals("1")) {
 			code = getTrimmedRegistryValue("Code");
 			// todo: nicer update of waiting => update command in same line
 			//IJ.log("Comm_ZeissConfocal: current code value = "+code);
 			try {
 			   Thread.sleep(1000);
			} catch(InterruptedException ex) {
    			   Thread.currentThread().interrupt();
			}
 		}
 		IJ.log(""+plugin_name+": microscope responded.");
 		String path = getTrimmedRegistryValue("filepath");
		IJ.log(""+plugin_name+": loading image from "+path);
 		ImagePlus imp = new ImagePlus(path);
		imp.show();
 		
	}


	public void writeToMacro(String command, int offsetx, int offsety) {
		
		if (command.equals("image selected particle")) {
			IJ.log(""+plugin_name+": ");
			// measure currently selected particle
			// todo: make sure the center of mass coordinates are selected
			RoiManager manager = RoiManager.getInstance();
			manager.runCommand("Measure");
			// get x,y coordinates
			ResultsTable rt = ResultsTable.getResultsTable();
			int lastRow = rt.getCounter()-1;
			int x = (int)rt.getValueAsDouble(rt.getColumnIndex("XM"),lastRow);
			int y = (int)rt.getValueAsDouble(rt.getColumnIndex("YM"),lastRow);
			IJ.log(""+plugin_name+": X, Y ="+x+", "+y);
			rt.deleteRow(lastRow);
		}
	
		WindowsRegistry.writeRegistry(winreg_location, "Code", command);
		WindowsRegistry.writeRegistry(winreg_location, "offsetx", ""+offsetx);
		WindowsRegistry.writeRegistry(winreg_location, "offsety", ""+offsety);
		IJ.log(""+plugin_name+": wrote to microscope");
	}

	public void readFromMacro() {
		
		String code = getTrimmedRegistryValue("Code");
		IJ.log(""+plugin_name+": read Code = "+ code);
						
	}


	// todo: move to Winreg class; parameter: winreg_separator
	public String getTrimmedRegistryValue(String key) {
		//IJ.log("reading from "+winreg_location);
		String temp1 = WindowsRegistry.readRegistry(winreg_location, key);
		//IJ.log("return="+temp1);
		String [] temp2 = temp1.split(winreg_separator); // extract only the value
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