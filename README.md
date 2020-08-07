# Gridmap
Contains the code and data for generating gridmaps for complex data.



GridMapCombiner contains code to run the entire pipeline in one go.
It first runs GridMapPartitioner to take the input polygon and parittion it into pieces.
Afterwards mosaic-maps is run to give each piece the correct amount of tiles.
Finally GridMapLP is run to assign input site to each tile.



ColorGenerator contains the code used to assign colors to each site.
Data contains all required data for reproduction of the figures that are contained in this paper. This included example data to run each component of the pipeline.
