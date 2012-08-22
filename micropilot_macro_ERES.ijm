
do {

// todo: make a GUI for stopping the thing


run("Close all forced");

// obtain image (NEW)
run("Microscope Communicator", "microscope=LSM780 action=[obtain image] command=[do nothing] object_x=0 object_y=0");

// find nuclei
rename("raw"); selectWindow("raw");
run("Duplicate...", "title=thresholded");
run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
run("Analyze Particles...", "size=2000-Infinity pixel circularity=0.00-1.00 show=Nothing exclude clear add");

// prepare the ERES thresholding
selectWindow("raw"); run("Next Slice [>]"); run("Duplicate...", "title=ERES"); 
run("FeatureJ Laplacian", "compute smoothing=3"); run("Invert"); run("16-bit");
run("Duplicate...", "title=thresholded_cell");

selectWindow("ERES"); 
run("Enlarge all ROIs", "number=50"); // NEW PLUGIN
run("Set Measurements...", "area mean center redirect=None decimal=2");
roiManager("Deselect"); run("Clear Results"); roiManager("Measure");

// select best cell
selectWindow("thresholded_cell"); 
run("Select best particle", "filter=threshold measurement=Mean threshold_min=6 threshold_max=7");
run("Auto Threshold", "method=Otsu white"); run("Convert to Mask");
run("Select best particle", "filter=threshold measurement=Mean threshold_min=6 threshold_max=7");
run("Analyze Particles...", "size=10-Infinity pixel circularity=0.00-1.00 show=Nothing exclude clear add");

// find best ERES
selectWindow("ERES Laplacian");
run("Set Measurements...", "min center redirect=None decimal=2");
roiManager("Deselect"); run("Clear Results"); roiManager("Measure");
run("Select best particle", "filter=max measurement=Max threshold_min=0 threshold_max=0");

// NEW PLUGIN: tell microscope to image the identified particle
run("Microscope Communicator", "microscope=LSM780 action=[submit command] command=[image selected particle] object_x=0 object_y=0");

} while (1);
