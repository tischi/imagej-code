do {

// todo: make a GUI for stopping the thing
run("Close all forced");

// obtain image (NEW)
run("Microscope Communicator", "microscope=LSM510 action=[obtain image] command=[do nothing] object_x=0 object_y=0");
run("Properties...", "unit=pix pixel_width=1 pixel_height=1 voxel_depth=1 origin=0,0");
rename("raw"); 


// extract images
selectWindow("raw"); run("Duplicate...", "title=nuclei");
selectWindow("raw"); run("Next Slice [>]"); run("Duplicate...", "title=eres");

// find nuclei 
selectWindow("nuclei"); run("Duplicate...", "title=nuclei_bw");
run("Gaussian Blur...", "sigma=3 slice"); wait(500);
run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
run("Analyze Particles...", "size=20000-Infinity pixel circularity=0.00-1.00 show=Nothing exclude clear add");

// find cells 
run("Enlarge all ROIs", "number=50"); // NEW PLUGIN

// measure cells
selectWindow("eres"); 
run("Set Measurements...", "area mean center redirect=None decimal=2");
roiManager("Deselect"); run("Clear Results"); roiManager("Measure");

// find best cell 
run("Select best particle", "filter=threshold measurement=Mean threshold_min=15 threshold_max=20");

if (selectionType() > -1) {

	// document the best cell
	selectWindow("eres"); run("Select None"); wait(500); run("Duplicate...", "title=eres_docu");
	run("Select best particle", "filter=threshold measurement=Mean threshold_min=15 threshold_max=20");
	setForegroundColor(255, 255, 255); run("Line Width...", "line=4"); run("Draw");
	
	// enhance ERES
	selectWindow("eres"); run("Select None"); wait(500); 
	run("FeatureJ Laplacian", "compute smoothing=3"); run("Invert"); run("16-bit"); rename("eres_laplacian"); 
	
	// threshold ERES in the best cell
	run("Duplicate...", "title=eres_bw");
	run("Select best particle", "filter=threshold measurement=Mean threshold_min=10 threshold_max=15");
	run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
	
	// find particles in the best cell
	run("Select best particle", "filter=threshold measurement=Mean threshold_min=10 threshold_max=15");
	run("Analyze Particles...", "size=10-Infinity pixel circularity=0.00-1.00 show=Nothing exclude clear add");
	
	// measure ERES
	selectWindow("eres"); run("Select None"); roiManager("Show All"); wait(500);
	run("Set Measurements...", "min center redirect=None decimal=2");
	roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
	run("Measure nearest neighbour distance");

	// filter ERES
	// isolated but not alone
	run("Filter particles", "filter=threshold measurement=nn_distance threshold_min=30 threshold_max=70");
	// not saturated
	run("Filter particles", "filter=threshold measurement=Max threshold_min=0 threshold_max=253");
	
	// measure local contrast
	selectWindow("eres_laplacian");
	roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
	
	// find best ERES
	run("Select best particle", "filter=max measurement=Max threshold_min=0 threshold_max=0");
	
	if (selectionType() > -1) {	
		// mark best ERES and save image together with the original for documentation
		selectWindow("eres_docu"); wait(500); 
		run("Select best particle", "filter=max measurement=Max threshold_min=0 threshold_max=0");
		setForegroundColor(255, 255, 255); run("Line Width...", "line=1"); run("Draw");
		run("Microscope Communicator", "microscope=LSM510 action=[save current image] command=[do nothing]"); 
		
		//run("Select best particle", "filter=max measurement=nn_distance threshold_min=0 threshold_max=0");
		// NEW PLUGIN: tell microscope to image the identified particle
		run("Microscope Communicator", "microscope=LSM510 action=[submit command] command=[image selected particle] object_x=0 object_y=0");
	}		
} 

if (selectionType() == -1) {	
	run("Microscope Communicator", "microscope=LSM510 action=[submit command] command=[do nothing] object_x=0 object_y=0");
	}
	
} while (1);
