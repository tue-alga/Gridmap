This folder contains the code for the controller of the gridmap pipeline. The main method is in GridmapCombiner.java.
The pipeline can be run for a set of parameters by running the main method of this class. Parameters can be set in the program itself. If required, the code can be easily changed to be called from the commandline instead

The code requires that: 
GridMapPartitioner is compiled and stored as a fat jar at "../GridMapPartitioner/store/GridMapPartitioner.jar" 
Mosaic-maps is compiled and stored at "../mosaic-maps/dist/MosaicMaps.jar".
GridMapLP is compiled and stored as a fat jar at "../GridMapLP/store/GridMapLP.jar"

The output will be stored under "combinedOutput/"

