
// obtain image
//run("Comm ZeissConfocal", "microscope=LSM780 action=[wait&obtain image] command=[do nothing] object=0 object=0");
rename("raw.tif");

// find particles
run("Duplicate...", "title=thresholded.tif");
run("Auto Threshold", "method=Otsu white");
run("Analyze Particles...", "size=100-Infinity circularity=0.00-1.00 show=Nothing exclude add");

// measure properties
run("Set Measurements...", "area mean center redirect=None decimal=2");
roiManager("Show All with labels");
selectWindow("raw.tif");
run("Clear Results"); // empty the results table
roiManager("Measure");


open("C:\\temp\\image.tif");
roiManager("Measure");
roiManager("Show None");
roiManager("Show All");
run("Set Measurements...", "area mean center redirect=None decimal=9");
roiManager("Measure");
