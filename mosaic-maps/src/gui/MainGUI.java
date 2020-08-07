package gui;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import algorithms.MosaicHeuristic;
import colouring.Colouring;
import colouring.RandomNonAdjacentColouring;
import colouring.colourschemes.ColourSchemes;
import geom.Point2D;
import geom.Polygon;
import gui.panels.MosaicPanel;
import model.ComponentManager;
import model.ComponentManager.Component;
import model.Cartogram.MosaicCartogram;
import model.Cartogram.MosaicCartogram.MosaicRegion;
import model.Network;
import model.subdivision.Map;
import model.subdivision.Map.Face;
import model.util.ElementList;
import model.util.IpeExporter;
import model.util.IpeImporter;
import model.util.KML.KMLToIpeConverter;
import model.util.Vector2D;
import parameter.ParameterManager;
import parameter.ParameterManager.Application.GridType;

public class MainGUI {

    private static final boolean runHonorsAlgorithms = false;

    private MosaicPanel cartogramPanel;

    public MainGUI() {
        cartogramPanel = new MosaicPanel();
    }

    public void run(boolean readParameters) {
        System.out.println("3");
        HeuristicRunner runner = new HeuristicRunner(readParameters);
        Thread thread = new Thread(runner);
        thread.start();
    }

    private void importData(String fileName, Map map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fileName)));
            ElementList<Boolean> hasData = new ElementList<>(map.numberOfBoundedFaces(), false);
            String line = br.readLine();
            while (line != null) {
                String[] components = line.split("\t");
                if (components.length == 2) {
                    Map.Face f = map.getFace(components[0]);
                    if (f == null) {
//                        System.out.println("Warning: face '" + components[0] + "' not found");
                    } else {
                        if (hasData.get(f)) {
                            throw new RuntimeException("multiple data values found for face " + f.getLabel().getText());
                        }
                        hasData.set(f, true);
                        double weight = Double.parseDouble(components[1]);
                        f.setWeight(weight);
                    }
                }
                line = br.readLine();
            }
            for (Map.Face f : map.boundedFaces()) {
                if (!hasData.get(f)) {
                    throw new RuntimeException("no data found for face " + f.getLabel().getText());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IpeExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(IpeExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void importColors(String fileName, Map map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fileName)));
            ElementList<Boolean> hasColor = new ElementList<>(map.numberOfBoundedFaces(), false);
            String line = br.readLine();
            while (line != null) {
                String[] components = line.split("\\s+");
                if (components.length == 4) {
                    Map.Face f = map.getFace(components[0]);
                    if (f == null) {
                        System.out.println("Warning: face '" + components[0] + "' not found");
                    } else {
                        if (hasColor.get(f)) {
                            throw new RuntimeException("multiple colors found for face " + f.getLabel().getText());
                        }
                        hasColor.set(f, true);
                        int r = Integer.parseInt(components[1]);
                        int g = Integer.parseInt(components[2]);
                        int b = Integer.parseInt(components[3]);
                        f.setColor(new Color(r, g, b));
                    }
                }
                line = br.readLine();
            }
            for (Map.Face f : map.boundedFaces()) {
                if (!hasColor.get(f) && !f.getLabel().getText().equals("*SEA*")) {
                    throw new RuntimeException("no color found for face " + f.getLabel().getText());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IpeExporter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(IpeExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void importColorsVec(String fileName, Map map) {
        try ( Scanner s = new Scanner(new FileReader(new File(fileName)))) {
            ElementList<Boolean> hasColor = new ElementList<>(map.numberOfBoundedFaces(), false);
            int num = s.nextInt();
            if (num != 3) {
                System.err.println("Only RGB is supported.");
                return;
            }
            while (s.hasNext()) {
                String faceName = s.nextLine().trim();
                if (faceName.isEmpty()) {
                    continue;
                }
                Face f = map.getFace(faceName);
                if (f == null) {
                    System.err.println("Warning: face '" + faceName + "' not found");
                } else {
                    if (hasColor.get(f)) {
                        System.err.println("Warning: multiple colors found for face "
                                           + f.getLabel().getText() + ", using last one");
                    }
                    hasColor.set(f, true);
                    float red = s.nextFloat();
                    float green = s.nextFloat();
                    float blue = s.nextFloat();
                    f.setColor(new Color(red, green, blue));
                }
            }
            for (Map.Face f : map.boundedFaces()) {
                if (!hasColor.get(f) && !f.getLabel().getText().equals("*SEA*")) {
                    System.err.println("Warning: no color found for face " + f.getLabel().getText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class HeuristicRunner implements Runnable {

        private final String MAP_FILE_NAME;
        private final String DATA_FILE_NAME;
        private final String MOSAIC_FILE_NAME;
        private final String COLOR_FILE_NAME;
        private final boolean COLOR_MAP;
        private final boolean VORNOI_ENABLED;
        private final String IPE_FILE_NAME;
        private final String STATS_FILE_NAME;
        private final Double RESOLUTION;
        private final Double UNIT_DATA;
        private final GridType TYPE;
        private final boolean FINALIZE_ONLY;
        private final boolean ANIMATION_ONLY;
        private final boolean EXACT_TILES;
        private final boolean EXIT_APP = true;
        private Map map = null;
        private ComponentManager manager = null;
        private final Double unitData;

        public HeuristicRunner(boolean readParameters) {
            if (!readParameters) {
                //MAP_FILE_NAME = "usa.ipe";
                MAP_FILE_NAME = "europe-animation.ipe";
                //MAP_FILE_NAME = "italy.ipe";
                //DATA_FILE_NAME = "wfb-table/GDP real growth rate.dat";
                DATA_FILE_NAME = "europe-pop.dat";
                //DATA_FILE_NAME = "usa-starbucks.dat";
                //DATA_FILE_NAME = null;
                //MOSAIC_FILE_NAME = "coordinates-starbucks-finalize.coo";
                //MOSAIC_FILE_NAME = "starbucks-almost-done.coo";
                //MOSAIC_FILE_NAME = "coordinates-eu.coo";
                MOSAIC_FILE_NAME = null;
                //COLOR_FILE_NAME = "worldmapper-colours.col";
                COLOR_FILE_NAME = null;
                COLOR_MAP = false;
                VORNOI_ENABLED = false;
                //IPE_FILE_NAME = "world-population.ipe";
                IPE_FILE_NAME = null;
                //STATS_FILE_NAME = "teste.csv";
                STATS_FILE_NAME = null;
                //RESOLUTION = 20.0;
                RESOLUTION = null;
                //UNIT_DATA = null;
                UNIT_DATA = 2E+6;
                TYPE = GridType.HEXAGONAL;
                FINALIZE_ONLY = false;
                ANIMATION_ONLY = false;
                EXACT_TILES = false;

            } else {
                MAP_FILE_NAME = ParameterManager.Application.getMapFileName();
                DATA_FILE_NAME = ParameterManager.Application.getDataFileName();
                MOSAIC_FILE_NAME = ParameterManager.Application.getMosaicFileName();
                COLOR_FILE_NAME = ParameterManager.Application.getColorFileName();
                COLOR_MAP = ParameterManager.Application.getColorMap();
                VORNOI_ENABLED = ParameterManager.Application.getVornoiEnabled();
                IPE_FILE_NAME = ParameterManager.Application.getIpeFileName();
                STATS_FILE_NAME = ParameterManager.Application.getStatsFileName();
                RESOLUTION = ParameterManager.Application.getMosaicResolution();
                UNIT_DATA = ParameterManager.Application.getUnitData();
                TYPE = ParameterManager.Application.getGridType();
                FINALIZE_ONLY = false;
                ANIMATION_ONLY = false;
                EXACT_TILES = ParameterManager.Application.getExactTiles();
            }
            System.out.println("4");
            unitData = initialize();
            System.out.println("5");

        }

        @Override
        public void run() {
            final double SCALING_THRESHOLD = 10;//7 or 10
            final double SCALING_FACTOR = 1.4142;

            System.out.println("Full program");
            // Find average number of tiles per region
            int totalTiles = 0;
            for (Map.Face f : map.boundedFaces()) {
                totalTiles += Math.max(1, (int) Math.round(f.getWeight() / unitData));
            }
            double averageTiles = (double) totalTiles / (double) map.numberOfBoundedFaces();
            double currentUnitData = unitData * averageTiles / SCALING_THRESHOLD;
            int scalingIteration = 1;
            System.out.println("Start calculation");
            while (currentUnitData > SCALING_FACTOR * unitData) {
                if (scalingIteration == 1) {
                    manager = new ComponentManager(map, TYPE, currentUnitData, 5);
                    if (MOSAIC_FILE_NAME != null) {
                        manager.initializeComponentsFromFile(MOSAIC_FILE_NAME);
                    } else {
                        manager.initializeComponentsFromEmbedding();
                    }
                } else {
                    manager.updateUnitData(currentUnitData);
                }
                System.out.println("Scaling with currentUnitData = " + String.format("%.2f", currentUnitData));
                for (Component component : manager.components()) {
                    MosaicCartogram componentCartogram = component.getCartogram();
                    Map componentMap = component.getMap();
                    Network componentWeakDual = component.getWeakDual();
                    MosaicHeuristic heuristic = new MosaicHeuristic(componentMap, componentWeakDual, componentCartogram);
                    componentCartogram = heuristic.execute(cartogramPanel, 5000, false, false);//no need for exact tiles yet
//                        //Intermediate files
//                        if (IPE_FILE_NAME == null) {
//                            IpeExporter.exportCartogram(componentCartogram, "cartogram" + scalingIteration + ".ipe");
//                        } else {
//                            IpeExporter.exportCartogram(componentCartogram, IPE_FILE_NAME.replace(".", "-" + scalingIteration + "."));
//                        }
                    System.out.println("start export coordinates");
                    componentCartogram.exportCoordinates("coordinates" + scalingIteration + ".coo");
                    component.setCartogram(componentCartogram);
                }
                System.out.println("Getting new unitData");
                currentUnitData /= SCALING_FACTOR;
                scalingIteration++;
            }
            System.out.println("final run starting");
            // Final run
            if (manager == null) {
                manager = new ComponentManager(map, TYPE, unitData, 5);
                if (MOSAIC_FILE_NAME != null) {
                    System.out.println("initializeComponentsFromFile");
                    manager.initializeComponentsFromFile(MOSAIC_FILE_NAME);
                } else {
                    System.out.println("InitializeComponentsFromEmbedding");
                    manager.initializeComponentsFromEmbedding();
                }
            } else {
                manager.updateUnitData(unitData);
            }
            System.out.println("Final run with UNIT_DATA = " + unitData);
            for (Component component : manager.components()) {
                System.out.println("start component");
                MosaicCartogram componentCartogram = component.getCartogram();
                System.out.println("getMap");
                Map componentMap = component.getMap();
                System.out.println("getWeakDual");
                Network componentWeakDual = component.getWeakDual();
                MosaicHeuristic heuristic = new MosaicHeuristic(componentMap, componentWeakDual, componentCartogram);
                System.out.println("execute heuristic");
                componentCartogram = heuristic.execute(cartogramPanel, 5000, true, EXACT_TILES);//finalize it. If specified use the exact amount of tiles

                System.out.println("start export coordinates");
                componentCartogram.exportCoordinates("coordinates" + scalingIteration + ".coo");
                System.out.println("set cartogram");
                component.setCartogram(componentCartogram);
                System.out.println("cartogram set");
            }
            System.out.println("merging cartograms");
            MosaicCartogram mergedCartogram = manager.mergeCartograms();
            System.out.println("Cartograms are merged");
            mergedCartogram.exportCoordinates("coordinates.coo");
            System.out.println("set cartogram");
//            cartogramPanel.setCartogram(mergedCartogram, true);
//            cartogramPanel.setCartogram(mergedCartogram);
            System.out.println("export ipe");
            if (IPE_FILE_NAME == null) {
                IpeExporter.exportCartogram(mergedCartogram, "cartogram.ipe");
            } else {
                IpeExporter.exportCartogram(mergedCartogram, IPE_FILE_NAME);
            }
            System.out.println("ipe exported");
            System.out.println("done");
            if (EXIT_APP) {
                System.out.println("exit");
                System.exit(0);
            }
        }

        private double symDiff(MosaicRegion r, MosaicCartogram cartogram) {
            // Region stuff
            ArrayList<java.awt.geom.Point2D> outline = r.computeOutlinePoints();
            ArrayList<Point2D> outline2 = new ArrayList<>(outline.size());
            for (java.awt.geom.Point2D p : outline) {
                outline2.add(new Point2D(p.getX(), p.getY()));
            }
            Polygon polyRegion = new Polygon(outline2);
            Vector2D regionCentroid = new Vector2D(polyRegion.getCentroid().getX(), polyRegion.getCentroid().getY());

            // Original face stuff
            Map.Face f = r.getMapFace();
            double faceArea = f.getArea();
            double regionArea = r.size() * cartogram.getCellArea();
            double factor = Math.sqrt(regionArea / faceArea);
            Vector2D newCentroid = Vector2D.product(f.getCentroid(), factor);
            Vector2D translation = Vector2D.difference(regionCentroid, newCentroid);
            Path2D facePath = new Path2D.Double();
            boolean first = true;
            for (Map.Vertex v : f.getBoundaryVertices()) {
                Vector2D position = v.getPosition();
                double vx = position.getX() * factor + translation.getX();
                double vy = position.getY() * factor + translation.getY();
                if (first) {
                    facePath.moveTo(vx, vy);
                    first = false;
                } else {
                    facePath.lineTo(vx, vy);
                }
            }
            facePath.closePath();
            Area a1 = new Area(facePath);
            Area a2 = polyRegion.convertToArea();
            a1.exclusiveOr(a2);
            List<Polygon> xor = Polygon.areaToPolygon(a1);
            double areaXor = 0;
            for (Polygon p : xor) {
                areaXor += p.getSignedArea();
            }
            if (areaXor < 0) {
                areaXor = -areaXor;
            }
//            System.out.println(f.getLabel().getText() + ": " + areaXor / regionArea);
//            IpeExporter exporter = new IpeExporter();
//            exporter.appendVertex(facePath, null);
//            exporter.appendVertex(polyRegion.convertToPath(), null);
//            exporter.exportToFile("symdifftest.ipe");
            return areaXor / regionArea;
        }

        private double initialize() {

            if (RESOLUTION != null && UNIT_DATA != null) {
                throw new RuntimeException("Resolution and Unit Data cannot be set simultaneously");
            }
            String mapFileName = MAP_FILE_NAME;
            if (MAP_FILE_NAME.endsWith(".kml")) {
                System.out.println("kmlConverter");
                KMLToIpeConverter kmlToIpeConverter = new KMLToIpeConverter();
                kmlToIpeConverter.convertMap(MAP_FILE_NAME, MAP_FILE_NAME.replace(".kml", ".ipe"), VORNOI_ENABLED);
                mapFileName = MAP_FILE_NAME.replace(".kml", ".ipe");
            }
            System.out.println("6");
            map = IpeImporter.importMap(mapFileName);
            System.out.println("7");
            if (COLOR_MAP) {
                Colouring c = new RandomNonAdjacentColouring(ColourSchemes.getOxygenColourScheme());
                c.assignColours(map);
            }

//            mapPanel.setMap(map);
            if (DATA_FILE_NAME != null) {
                importData(DATA_FILE_NAME, map);
            } else {
                for (Map.Face face : map.boundedFaces()) {
                    face.setWeight(face.getArea());
                }
            }

            if (COLOR_FILE_NAME != null) {
                if (COLOR_FILE_NAME.endsWith("vec")) {
                    importColorsVec(COLOR_FILE_NAME, map);
                } else {
                    importColors(COLOR_FILE_NAME, map);
                }
            }

            double unit;
            if (RESOLUTION == null) {
                unit = UNIT_DATA;
            } else {
                double dataSum = 0;
                for (Map.Face face : map.boundedFaces()) {
                    dataSum += face.getWeight();
                }
                double totalTiles = RESOLUTION * map.numberOfBoundedFaces();
                unit = dataSum / totalTiles;
            }
            return unit;
        }

    }
}
