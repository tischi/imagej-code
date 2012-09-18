// LSM780 1024x1024 zoom1 63x
microscope = "File system" //"LSM780"
nuc_size_min = "8000"
cell_intens_min = "5"
cell_intens_max = "20"
eres_dist_min =  "15"
eres_size_min = "5"
eres_size_max = "20"
// LSM510


do {

run("Close all forced");

// obtain image (NEW)
//run("Microscope Communicator", "microscope=[File system] action=[obtain image] command=[do nothing] choose=/Users/tischi/Downloads/ctrl_z10_n172_r40_02/ctrl_L1_R1.lsm_eres_docu.jpg");
run("Microscope Communicator", "microscope=[File system] action=[obtain image] command=[do nothing] choose=/Users/tischi/Downloads/ctrl_z10_n172_r40_02/ctrl_L1_R1.lsm");

//run("Microscope Communicator", "microscope="+microscope+" action=[obtain image] command=[do nothing]");
run("Properties...", "unit=pix pixel_width=1 pixel_height=1 voxel_depth=1 origin=0,0");
rename("raw"); 

// extract images
selectWindow("raw"); run("Duplicate...", "title=nuclei");
selectWindow("raw"); run("Next Slice [>]"); run("Duplicate...", "title=eres");

// enhance ERES
selectWindow("eres");
run("Duplicate...", "title=gs");
run("Gaussian Blur...", "sigma=1.5 scaled");
//selectWindow("eres");
//run("Duplicate...", "title=gb");
//run("Gaussian Blur...", "sigma=10 scaled");
//imageCalculator("Subtract", "gs","gb"); rename("gaussian diff");

// threshold ERES
selectWindow("gs"); run("Duplicate...", "title=gs_bw");
run("Auto Local Threshold", "method=Niblack radius=20 parameter_1=4.0 parameter_2=0 white");
run("Convert to Mask");

// segment ERES
run("Analyze Particles...", "size="+eres_size_min+"-"+eres_size_max+" pixel circularity=0.00-1.00 show=Masks exclude clear add");
rename("ERES_Masks");

// measure ERES
// measure ERES neighborhood
selectWindow("ERES_Masks"); run("Dilate"); run("Invert"); run("Divide...", "value=255");
imageCalculator("Multiply create", "gs","ERES_Masks");
run("Gray Morphology", "radius=10 type=circle operator=[fast dilate]");
rename("ERES_Neighborhood");

ff

//



selectWindow("eres"); run("Duplicate...", "title=eres_removed");
selectWindow("eres_removed");
//roiManager("Show All");
//setForegroundColor(0, 0, 0);
//run("Fill", "slice");

ff


ff
ff



// find nuclei 
selectWindow("nuclei"); run("Duplicate...", "title=nuclei_bw");
run("Gaussian Blur...", "sigma=3 slice"); wait(500);
run("Auto Threshold", "method=Otsu white"); run("Convert to Mask"); wait(500);
run("Analyze Particles...", "size="+nuc_size_min+"-Infinity pixel circularity=0.00-1.00 show=Nothing exclude clear add");

// find cells 
run("Enlarge all ROIs", "number=70");

// measure cells
selectWindow("eres"); 
run("Set Measurements...", "area mean center redirect=None decimal=2");
roiManager("Deselect"); run("Clear Results"); roiManager("Measure");

// filter cells
run("Filter particles", "filter=threshold measurement=Mean threshold_min="+cell_intens_min+" threshold_max="+cell_intens_max);

// find best cell 
run("Measure image center distance");
run("Select None"); run("Select best particle", "filter=min measurement=imCenter_distance threshold_min="+cell_intens_min+" threshold_max="+cell_intens_max);

if (selectionType() > -1) {

	// document the best cell
	selectWindow("eres"); run("Select None"); wait(500); run("Duplicate...", "title=eres_docu");
	run("Select best particle", "filter=min measurement=imCenter_distance threshold_min="+cell_intens_min+" threshold_max="+cell_intens_max);
	setForegroundColor(255, 255, 255); run("Line Width...", "line=2"); run("Draw");
	
	// enhance ERES
	selectWindow("eres"); run("Select None"); wait(500); 
	run("FeatureJ Laplacian", "compute smoothing=1"); run("Invert"); rename("eres_laplacian"); 
	
	// threshold ERES in the best cell
	run("Duplicate...", "title=eres_bw");
	run("Select best particle", "filter=min measurement=imCenter_distance threshold_min="+cell_intens_min+" threshold_max="+cell_intens_max);
	run("8-bit"); run("Auto Local Threshold", "method=Niblack radius=15 parameter_1=2.0 parameter_2=0 white");
	run("Convert to Mask");
	//run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
	//run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
	
	// find particles in the best cell
	run("Select best particle", "filter=min measurement=imCenter_distance threshold_min="+cell_intens_min+" threshold_max="+cell_intens_max);
	run("Analyze Particles...", "size=5-100 pixel circularity=0.00-1.00 show=Nothing exclude clear add");
	
	// measure ERES
	selectWindow("eres"); run("Select None"); roiManager("Show All"); wait(500);
	run("Set Measurements...", "min area center redirect=None decimal=2");
	roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
	run("Measure nearest neighbour distance");
	
	// filter ERES
	// isolated but not alone
	run("Filter particles", "filter=threshold measurement=nn_distance threshold_min="+eres_dist_min+" threshold_max=100");

	// not too big and not too small
	run("Filter particles", "filter=threshold measurement=area threshold_min=6 threshold_max=15");

	// not saturated and not too dimm
	run("Filter particles", "filter=threshold measurement=Max threshold_min=50 threshold_max=253");

	
	// measure whether ERES are in the nucleus
	selectWindow("nuclei_bw"); run("Select None"); roiManager("Show All"); wait(500);
	run("Set Measurements...", "min area center redirect=None decimal=2");
	roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
	// not in the nucleus 
	run("Filter particles", "filter=threshold measurement=Max threshold_min=0 threshold_max=100");
	
	// measure local contrast
	selectWindow("eres_laplacian");
	roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
	
	// find best ERES
	run("Select best particle", "filter=max measurement=Max threshold_min=0 threshold_max=0");
		
	if (selectionType() > -1) {	
		// mark best ERES and save image together with the original for documentation
		selectWindow("eres_docu"); wait(500); 
		run("Select best particle", "filter=max measurement=Max threshold_min=0 threshold_max=0");
		run("Enlarge...", "enlarge=5");
		setForegroundColor(255, 255, 255); run("Line Width...", "line=1"); run("Draw");
		run("Microscope Communicator", "microscope="+microscope+" action=[save current image] command=[do nothing]"); 
		
		//run("Select best particle", "filter=max measurement=nn_distance threshold_min=0 threshold_max=0");
		// NEW PLUGIN: tell microscope to image the identified particle
		run("Microscope Communicator", "microscope="+microscope+" action=[submit command] command=[image selected particle] object_x=0 object_y=0");
	}		
} 

if (selectionType() == -1) {	
	run("Microscope Communicator", "microscope="+microscope+" action=[submit command] command=[do nothing] object_x=0 object_y=0");
	}
	
wait(500);
	
} while (1);
