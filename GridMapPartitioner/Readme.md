Contains the code to partition an input map into parts. GridmapPartitioner contains the main method.
Code uses CPLEX (external/sdg-voronoi-edges.exe) to generate the medial axis required for generating the candidate cuts.


After compilation, the code can be run via the command line with the following parameters:

-i: Input map location
-s: Site file location
-o: Output file location
-d: dilation threshold between 0 and 1
-p: productivity thresholds. Must be >= 1




Example command: -i ../Data/maps/nederlandOutline.ipe -s ../Data/maps/Sites/NetherlandsMunicipalitySitesColor.tsv -o ../Data/output/partition.ipe  -d 3 -p 4 