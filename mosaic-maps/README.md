The folder contains the code to generate a mosaic map and is based on the code from Cano, Rafael G., Kevin Buchin, Thom Castermans, Astrid Pieterse, Willem Sonke, and Bettina Speckmann. "Mosaic drawings and cartograms." In Computer Graphics Forum, vol. 34, no. 3, pp. 361-370. 2015.

It can be run via gui/MainGUI.java. Note that the actual gui is disabled, and this program fully runs via the commandline.
After compilation, the code can be run via the command line with the following parameters :

-map: Input map location
-data: weight data file
-ipe: Output file location
-square: Use square tiles
-hexagonal: Use hexagonal tiles
-unit x: x tile per unit of weight. Default for gridmaps is 1.
-exact: If true, the program is allowed to remove adjacencies to get the exact amount of squares.

Note that there exists more parameters, but these are not used for the pipeline.


Example command:
   -map ../Data/output/PartitionLabeled.ipe -data ../Data/output/Weights.tsv -ipe ../Data/output/MosaicMapOutput.ipe -square -unit 1 -exact
