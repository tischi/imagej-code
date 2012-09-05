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
public class Measure_nearest_neighbour_distance implements PlugIn {
	
	String plugin_name = "Measure nearest neighbour distance";
	
	public void run(String arg) {
		
		IJ.log(" ");
		IJ.log(""+plugin_name+": started");
 		
		// todo: what happens if there is no results table?
		//String[] colnames = get_cols_of_ResultsTable();
		// todo: throw an error is this does not return a particle
		// todo: throw an error if the x and y colnames do not exist
		//String[] methods = {"max", "min", "threshold", "random"};
		
		//static String action	
        	//GenericDialog gd = new GenericDialog("Retrieve best particle");
        	//gd.addChoice("filter method: ", methods, methods[0]);
        	//gd.addChoice("measurement: ", colnames, colnames[0]);
        	//gd.addNumericField("threshold_Min: ", 0, 0);
        	//gd.addNumericField("threshold_Max: ", 0, 0);
      	        //gd.showDialog();
        	//if(gd.wasCanceled()) return;
        	//String method = (String)gd.getNextChoice();
        	//String colname = (String)gd.getNextChoice();
        	//float th_min = (float)gd.getNextNumber();
        	//float th_max = (float)gd.getNextNumber();

        	// todo: how to deal with multiple particles that are within the range?
		//float[] ivxy = getBestParticle(colname, method, th_min, th_max);

		measure_nn_dist();
		
		
    	}

	public void measure_nn_dist() {
		double minDist,dx2,dy2;
	
			
		ResultsTable rt = ResultsTable.getResultsTable();	
		float[] x = (float[])rt.getColumn(rt.getColumnIndex("XM"));
		float[] y = (float[])rt.getColumn(rt.getColumnIndex("YM"));
		

		for (int i = 0; i < x.length; i++) {
			minDist = Float.MAX_VALUE;
			for (int j = 0; j < x.length; j++) {
				if (i!=j) {
					//IJ.log("i"+i+" j"+j);
			
					dx2 = Math.pow(x[i]-x[j],2);
					dy2 = Math.pow(y[i]-y[j],2);
					if (Math.sqrt(dx2+dy2) < minDist) {
						minDist = Math.sqrt(dx2+dy2);
					}		
				}
			}
			//IJ.log("minDist ="+minDist);
			//rt.addValue("nnd", (double)minDist);
			rt.setValue("nn_distance", i, minDist);
		}
	
	rt.updateResults();
	rt.show("Results");
	
	}
	
	
}


