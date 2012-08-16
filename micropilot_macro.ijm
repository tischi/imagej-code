// obtain image
run("Comm ZeissConfocal", "microscope=LSM780 action=[obtain image] command=[do nothing] object_x=0 object_y=0");
rename("raw");

// threshold particles 
run("Duplicate...", "title=thresholded");
run("Auto Threshold", "method=Otsu white");

// add to ROI manager
// todo: close current ROI manager
run("Analyze Particles...", "size=100-Infinity circularity=0.00-1.00 show=Nothing exclude add");

// measure particle properties
selectWindow("raw");
roiManager("Show All with labels");
run("Set Measurements...", "area mean center redirect=None decimal=2");
run("Clear Results");
roiManager("Measure");

// select the best particle
run("select best particle", "filter=max measurement=Area");
