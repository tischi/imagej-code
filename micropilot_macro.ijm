// NEW PLUGIN: obtain image from microscope
run("Microscope Communicator", "microscope=LSM780 action=[obtain image] command=[do nothing] object_x=0 object_y=0");

// threshold particles 
rename("raw");
run("Duplicate...", "title=thresholded");
run("Auto Threshold", "method=Otsu white");

// find particles and add to ROI manager
run("Analyze Particles...", "size=100-Infinity circularity=0.00-1.00 show=Nothing exclude clear add");

// measure particle properties
selectWindow("raw");
roiManager("Show All with labels");
run("Set Measurements...", "area mean center redirect=None decimal=2");
run("Clear Results");
roiManager("Measure");

// NEW PLUGIN: select best particle
run("Select best particle", "filter=max measurement=Area");
// NEW PLUGIN: tell microscope to image the identified particle
run("Microscope Communicator", "microscope=LSM780 action=[submit command] command=[image selected particle] object_x=0 object_y=0");