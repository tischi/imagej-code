/***
 * @author Christian Tischer
 */


import ij.*;
import ij.plugin.*;
import ij.measure.*;


import ij.gui.GenericDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.*;
import java.util.*;


public class retrieve_best_particle implements PlugIn {
	
	String plugin_name = "Find best particle";
	
	public void run(String arg) {
		
		IJ.log(" ");
		IJ.log(""+plugin_name+": started");
 		
		// todo: what happens if there is no results table?
		String[] colnames = get_cols_of_ResultsTable();
		// todo: throw an error is this does not return a particle
		// todo: throw an error if the x and y colnames do not exist
		String[] methods = {"max", "min", "random"};
		//static String action	
        	GenericDialog gd = new GenericDialog("Retrieve best particle");
        	gd.addChoice("filter method: ", methods, methods[0]);
        	gd.addChoice("measurement: ", colnames, colnames[0]);
        	gd.showDialog();
        	if(gd.wasCanceled()) return;
        	String method = (String)gd.getNextChoice();
        	String colname = (String)gd.getNextChoice();
        	
		int[] xy = getXYofBestParticle(colname, method);
		
    	}

	public int[] getXYofBestParticle(String colname, String method) {
		int[] xy = new int[2];
		int iBest = 0;
		int i;
		float vBest;
			
		ResultsTable rt = ResultsTable.getResultsTable();	
		float[] v = (float[])rt.getColumn(rt.getColumnIndex(colname));
		vBest = v[0];
			
		if (method=="max") {
			for (i = 1; i < v.length; i++) {
				if (v[i] > vBest) {
					vBest = v[i];
					iBest = i;
				}
			}
		} 
		else if (method=="min") {
			
		}
		else if (method=="random") {
		
		}

		float[] x = rt.getColumn(rt.getColumnIndex("XM"));
		float[] y = rt.getColumn(rt.getColumnIndex("YM"));

		xy[0]=(int)x[iBest]; 
		xy[1]=(int)y[iBest];

		IJ.log(""+plugin_name+": best particle: index, measurement = "+iBest+", "+vBest);
		IJ.log(""+plugin_name+": best particle: x, y = "+xy[0]+", "+xy[1]);

		return xy;
		
	}

	public String[] get_cols_of_ResultsTable() {
		IJ.log(""+plugin_name+": getting colnames of results table...");
		ResultsTable rt = ResultsTable.getResultsTable();
		String temp = (String)rt.getColumnHeadings().trim();
		String[] colnames = temp.split("\t");
		// todo output all names
		IJ.log(""+plugin_name+": colnames[0]: "+colnames[0]);
		return colnames;
	}

}


