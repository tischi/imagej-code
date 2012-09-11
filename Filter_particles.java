/***
 * @author Christian Tischer
 */


import ij.*;
import ij.plugin.*;
import ij.measure.*;
import ij.plugin.frame.RoiManager;


import ij.gui.GenericDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.*;
import java.util.*;

// todo rename to "select_best_particle"
public class Filter_particles implements PlugIn {
	
	String plugin_name = "Filter particles";
	
	public void run(String arg) {
		
		IJ.log(" ");
		IJ.log(""+plugin_name+": started.");
 		
		// todo: what happens if there is no results table?
		String[] colnames = get_colnames_of_ResultsTable();
		// todo: throw an error is this does not return a particle
		// todo: throw an error if the x and y colnames do not exist
		String[] methods = {"threshold"};
		
		//static String action	
        	GenericDialog gd = new GenericDialog("Filter particles");
        	gd.addChoice("filter method: ", methods, methods[0]);
        	gd.addChoice("measurement: ", colnames, colnames[0]);
        	gd.addNumericField("threshold_Min: ", 0, 0);
        	gd.addNumericField("threshold_Max: ", 255, 0);
      	        gd.showDialog();
        	if(gd.wasCanceled()) return;
        	String method = (String)gd.getNextChoice();
        	String colname = (String)gd.getNextChoice();
        	float th_min = (float)gd.getNextNumber();
        	float th_max = (float)gd.getNextNumber();

        	// todo: how to deal with multiple particles that are within the range?
		filterParticles(colname, method, th_min, th_max);
		
    	}

	public void filterParticles(String colname, String method, float th_min, float th_max) {
		int i;
		int iCol;
		double v;

		
		ResultsTable rt = ResultsTable.getResultsTable();
		
		iCol = rt.getColumnIndex(colname); if(iCol == -1) return;
		
		//float[] values = (float[])rt.getColumn(rt.getColumnIndex(colname));
		//int n = values.length;

		int n = rt.getCounter();

		RoiManager manager = RoiManager.getInstance();
		manager.runCommand("Deselect");
		
		// todo: throw an error if the number of ROIs and Results is not the same
		if (method.equals("threshold")) {
			for (i=n-1; i>=0; i--) {
				v = rt.getValue(colname,i);	
				if ( (v > th_max) || (v < th_min) ) {  // particle is no good
					//IJ.log(""+i+" "+v);
					manager.select(i); 
					manager.runCommand("Delete");
					rt.deleteRow(i);
					//rt.updateResults();
					//rt.show("Results");
					//n=n-1;
					//i=i-1;			
				}
			}	
			rt.updateResults();
			rt.show("Results");		
		}
		
		
		
	}

	private String[] get_colnames_of_ResultsTable() {
		IJ.log(""+plugin_name+": getting colnames of results table...");
		ResultsTable rt = ResultsTable.getResultsTable();
		String temp = (String)rt.getColumnHeadings().trim();
		String[] colnames = temp.split("\t");
		// todo output all names
		//IJ.log(""+plugin_name+": colnames[0]: "+colnames[0]);
		return colnames;
	}

}


