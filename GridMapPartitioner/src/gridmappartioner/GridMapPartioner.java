/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridmappartioner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;
import nl.tue.geometrycore.io.ipe.IPEWriter;
import nl.tue.geometrycore.util.Pair;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author msondag
 */
public class GridMapPartioner {

    File inputIpeFile;
    File outputIpeFile;
    File siteDataFile;

    List<Site> sites;

    private double dilationThreshold;
    private int productivityThreshold; 

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Only 4 digits precision in the data allowed by default.");
        //Example arg        
        //-i ../Data/maps/nederlandProvinces.ipe -s ../Data/maps/Sites/NetherlandsMunicipalitySitesColor.tsv -o ../Data/output/partition.ipe  -d 0.2 -p 4 
        //-i ../Data/maps/nederlandOutline.ipe -s ../Data/maps/Sites/NetherlandsMunicipalitySitesColor.tsv -o ../Data/output/partition.ipe  -d 0.33 -p 4 
        //-i ../Data/maps/debugPolygons/testPolygon.ipe -s ../Data/maps/Sites/testPolygonSites.tsv -o ../Data/output/partition.ipe  -d 0.33 -p 3
        new GridMapPartioner(args);
    }

    public GridMapPartioner(String[] args) throws IOException {
        parseArgs(args);
        partition();
        Utility.printTimer();
    }

    public GridMapPartioner(String inputIpeFile, String pointDataFile, String outputIpeFile) throws IOException {
        this.inputIpeFile = new File(inputIpeFile);
        this.siteDataFile = new File(pointDataFile);
        this.outputIpeFile = new File(outputIpeFile);
        partition();
    }

    HashMap<String, Long> times = new HashMap();

    private void partition() throws IOException {
        List<PartitionPolygon> inputPolygons = readInputPolygons();
        sites = readPointDataFile();

        for (Site s : sites) {
            boolean contained = false;
            for (PartitionPolygon p : inputPolygons) {
                if (p.containsPoint(s.point)) {
                    contained = true;
                }
            }
            if (!contained) {
                System.err.println("Not all points are in the inputPolygons:" + s.point + s.label);
            }
        }

        List<PartitionPolygon> partitionedPolygons = new ArrayList();
        for (PartitionPolygon p : inputPolygons) {
            List<PartitionPolygon> partitions = partitionPolygon(p);
            partitionedPolygons.addAll(partitions);
        }

        //in case the polygons are neighboring, we need to add vertices at cut places.
        addExtraVertices(partitionedPolygons);

        
        writeToIpe(partitionedPolygons);

        for (String key : times.keySet()) {
            System.out.println(key + ":" + times.get(key));
        }
    }

    private List<PartitionPolygon> readInputPolygons() throws IOException {
        IPEReader reader = IPEReader.fileReader(inputIpeFile);
        List<ReadItem> items = reader.read();

        List<PartitionPolygon> polygons = new ArrayList();

        for (ReadItem item : items) {
            BaseGeometry geometry = item.getGeometry();
            if (geometry.getGeometryType() == GeometryType.POLYGON) {
                Polygon p = (Polygon) geometry.toGeometry();

                List<PartitionSegment> psSegment = new ArrayList();
                for (LineSegment ls : p.edges()) {
                    psSegment.add(new PartitionSegment(ls));
                }

                PartitionPolygon pp = new PartitionPolygon(psSegment);

                pp.removeDegeneracies();

                polygons.add(pp);
            }
        }

        return polygons;
    }

    /**
     * Partitions a specific polygon
     *
     * @param inputPolygon
     */
    private List<PartitionPolygon> partitionPolygon(PartitionPolygon inputPolygon) {
        System.out.println("Partitioning Polygon");
        //generate the cuts
        Utility.startTimer("cuts");
        CutGenerator cg = new CutGenerator(inputPolygon, dilationThreshold);
        List<Cut> cuts = cg.getCandidateCuts();
        System.out.println("Cuts calculated");

        sortCutsByLength(cuts);

        Utility.endTimer("cuts");

        //generate graph structure
        Utility.startTimer("graphStructure");

        GraphStructure gs = generateGraphStructure(inputPolygon, cuts);
        Set<Cut> usedCuts = new HashSet();

        Utility.endTimer("graphStructure");

        int iteration = 0;

        //process each cut, and store the resulting polygons
        List<PartitionPolygon> partitionedPolygons = new ArrayList();
        partitionedPolygons.add(inputPolygon);
        for (Cut c : cuts) {
            iteration++;
            if (iteration % 10 == 0) {
                System.out.println("cut " + iteration + "/" + cuts.size());
            }

            //get the new partitionPolygons after this cut.
            //We do not know in which partition the cut is, so we have to go in all of them.
            List<PartitionPolygon> updatedList = new ArrayList();
            for (PartitionPolygon p : partitionedPolygons) {
                if (!p.containsCut(c)) {
                    //not in partition polygon, so no change.
                    updatedList.add(p);
                } else {
                    Utility.startTimer("dilation");
                    //need to recompute dilation as the polygon gets chopped up
                    c.computeDilation(p);
                    if (c.dilation > dilationThreshold) {
                        //skip this cut, it has too little dilation
                        updatedList.add(p);
                        continue;
                    }
                    Utility.endTimer("dilation");
                    Utility.startTimer("productive");
                    //check if the cut is productive
                    boolean productive = gs.isProductive(c, usedCuts, productivityThreshold);
                    Utility.endTimer("productive");
                    if (productive) {
                        Pair<PartitionPolygon, PartitionPolygon> splitPolygons = p.splitPolygon(c);
                        updatedList.add(splitPolygons.getFirst());
                        updatedList.add(splitPolygons.getSecond());
                        usedCuts.add(c);
                        System.out.println("productive");
                    } else {
                        updatedList.add(p);
                    }
                }
            }
            //went through all the polygons and updated the list
            partitionedPolygons = updatedList;
        }

        return partitionedPolygons;
    }

    private void writeToIpe(List<PartitionPolygon> outputPolygons) throws IOException {
        IPEWriter fileWriter = IPEWriter.fileWriter(outputIpeFile);

        fileWriter.initialize();
        fileWriter.newPage();

        Collections.sort(outputPolygons, (PartitionPolygon p1, PartitionPolygon p2) -> (Double.compare(p1.getMinY(), p2.getMinY())));

        for (PartitionPolygon pp : outputPolygons) {
            //fix the precision to 3 digits
            pp.fixedPrecision(3);
            //this might cause degeneracies, so remove those.
            pp.removeDegeneracies();
            fileWriter.appendCustomPathCommand(pp.toIpe());
        }
        fileWriter.close();

    }

    private void sortCutsByLength(List<Cut> cuts) {
        //shorest cut first
        cuts.sort((Cut c1, Cut c2) -> Double.compare(c1.getLength(), c2.getLength()));
    }

    private List<Site> readPointDataFile() {
        List<Site> sites = new ArrayList();
        try {
            List<String> lines = Files.readAllLines(siteDataFile.toPath());
            for (String line : lines) {
                String[] split = line.split("\t");
                String label = split[0];
                double x = Double.parseDouble(split[1]);
                double y = Double.parseDouble(split[2]);
                Site s = new Site(x, y, label);
                sites.add(s);

            }
        } catch (IOException ex) {
            Logger.getLogger(GridMapPartioner.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return sites;
    }

    private void parseArgs(String[] args) {
        System.out.println("printing arguments");
        for (String arg : args) {
            System.out.println("arg = " + arg);
        }
        Options options = new Options();

        Option input = new Option("i", "input", true, "input ipe map");
        input.setRequired(true);
        options.addOption(input);

        Option site = new Option("s", "sites", true, "input site map");
        site.setRequired(true);
        options.addOption(site);

        Option output = new Option("o", "output", true, "output ipe location");
        output.setRequired(true);
        options.addOption(output);

        Option dilation = new Option("d", "dilation", true, "dilation threshold between 0 and 1");
        dilation.setRequired(true);
        options.addOption(dilation);

        Option productivity = new Option("p", "productivity", true, "productive threshold. Greater or larger than 1");
        productivity.setRequired(true);
        options.addOption(productivity);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        inputIpeFile = new File(cmd.getOptionValue("input"));
        siteDataFile = new File(cmd.getOptionValue("sites"));
        outputIpeFile = new File(cmd.getOptionValue("output"));

        dilationThreshold = Double.parseDouble(cmd.getOptionValue("dilation", "" + dilationThreshold));
        productivityThreshold = Integer.parseInt(cmd.getOptionValue("productivity", "" + productivityThreshold));
    }

    private void addExtraVertices(List<PartitionPolygon> partitionedPolygons) {
        //inefficient, can use cuts instead
        System.out.println("adding extra vertices");
        //Add a vertex if it ends on the interior of a segment from a different polygon.
        for (PartitionPolygon p1 : partitionedPolygons) {
            for (Vector v : p1.getVertices()) {
                for (PartitionPolygon p2 : partitionedPolygons) {
                    if (p1.equals(p2)) {
                        continue;
                    }
                    PartitionSegment toSplit = null;
                    for (PartitionSegment s : p2.getSegments()) {
                        if (s.onBoundary(v) && !s.isApproxEndpoint(v)) {
                            toSplit = s;
                            break;
                        }
                    }
                    if (toSplit != null) {
                        toSplit.splitSegment(v);
                    }
                }
            }
        }
    }

    private GraphStructure generateGraphStructure(PartitionPolygon inputP, List<Cut> cuts) {
        List<Cut> remainingCuts = new ArrayList(cuts);

        PartitionPolygon inputPCopy = inputP.copy();
        
        List<PartitionPolygon> polygons = new ArrayList();
        polygons.add(inputPCopy);

        while (!remainingCuts.isEmpty()) {
            Cut c = remainingCuts.get(0);
            for (PartitionPolygon p : polygons) {
                if (p.containsCut(c)) {
                    Pair<PartitionPolygon, PartitionPolygon> splitPolygons = p.splitPolygon(c);
                    polygons.add(splitPolygons.getFirst());
                    polygons.add(splitPolygons.getSecond());
                    polygons.remove(p);
                    break;
                }
            }
            remainingCuts.remove(0);
        }
        //Polygon fully partitioned. start generating the graph
        GraphStructure gs = new GraphStructure(polygons, cuts, sites);
        return gs;
    }

}
