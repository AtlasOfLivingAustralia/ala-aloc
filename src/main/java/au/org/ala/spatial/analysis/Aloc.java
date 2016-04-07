/**
 * ************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia All Rights Reserved.
 * <p>
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * <p>
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * *************************************************************************
 */

package au.org.ala.spatial.analysis;

import au.org.ala.spatial.util.Grid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * entry into running Aloc
 * <p/>
 * use run
 *
 * @author adam
 */
public class Aloc {

    /**
     * exports means and colours of a classification (ALOC) into a csv
     *
     * @param filename csv filename to export into
     * @param means    mean values for each legend record as [n][m] where n is
     *                 number of records m is number of layers used to generate the
     *                 classification (ALOC)
     * @param colours  RGB colours of legend records as [n][3] where n is number
     *                 of records [][0] is red [][1] is green [][2] is blue
     * @param layers   layers used to generate the classification as Layer[]
     */
    static void exportMeansColours(String filename, double[][] means, int[][] colours, String[] layers) {
        try {
            FileWriter fw = new FileWriter(filename);
            int i, j;

            /*
             * header
             */
            fw.append("group number");
            fw.append(",red");
            fw.append(",green");
            fw.append(",blue");
            for (i = 0; i < layers.length; i++) {
                fw.append(",");
                fw.append(layers[i]);
            }
            fw.append("\r\n");

            /*
             * outputs
             */
            for (i = 0; i < means.length; i++) {
                fw.append(String.valueOf(i + 1));
                fw.append(",");
                fw.append(String.valueOf(colours[i][0]));
                fw.append(",");
                fw.append(String.valueOf(colours[i][1]));
                fw.append(",");
                fw.append(String.valueOf(colours[i][2]));

                for (j = 0; j < means[i].length; j++) {
                    fw.append(",");
                    fw.append(String.valueOf(means[i][j]));
                }

                fw.append("\r\n");
            }

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void exportMetadata(String filename, int numberOfGroups, String[] layers, String[] invariantLayers, String pid, String coloursAndMeansUrl, String area, int width, int height, double minx, double miny, double maxx, double maxy, int iterationCount) {
        try {
            FileWriter fw = new FileWriter(filename);
            int i;

            fw.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"> <html> <head> <meta http-equiv=\"Content-Type\" content=\"text/html; charset=MacRoman\"> <title>Layer information</title> <link rel=\"stylesheet\" href=\"/alaspatial/styles/style.css\" type=\"text/css\" media=\"all\" /> </head> ");

            fw.append("<body>");
            fw.append("<h1>").append("Classification").append("</h1>");

            fw.append("<p> <span class=\"title\">Date/time:</span> <br /> ");
            fw.append(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(System.currentTimeMillis()));
            fw.append("</p>");

            fw.append("<p> <span class=\"title\">Model reference number:</span> <br /> ");
            fw.append(pid);
            fw.append("</p>");

            fw.append("<p> <span class=\"title\">Number of groups:</span> <br /> ");
            fw.append(String.valueOf(numberOfGroups));
            fw.append("</p>");

            fw.append("<p> <span class=\"title\">Number of iterations:</span> <br /> ");
            fw.append(String.valueOf(iterationCount));
            fw.append("</p>");

            fw.append("<p> <span class=\"title\">Layers (" + layers.length + "):</span> <br /> ");
            for (i = 0; i < layers.length; i++) {
                fw.append(layers[i].replace(".grd", "").replace(".GRD", ""));
                if (i < layers.length - 1) {
                    fw.append("<br /> ");
                }
            }
            fw.append("</p>");

            if (invariantLayers != null && invariantLayers.length > 0) {
                fw.append("<p> <span class=\"title\">DROPPED LAYERS: invariance across extent (" + invariantLayers.length + "):</span> <br /> ");
                for (i = 0; i < invariantLayers.length; i++) {
                    fw.append(invariantLayers[i].replace(".grd", "").replace(".GRD", ""));
                    if (i < invariantLayers.length - 1) {
                        fw.append("<br /> ");
                    }
                }
                fw.append("</p>");
            }

            fw.append("<p> <a href=\"http://spatial.ala.org.au/files/inter_layer_association.csv\" >");
            fw.append("<span class=\"title\">Inter-layer dissimilarity matrix (csv)</span>  ");
            fw.append("</a>");
            fw.append("</p>");

            fw.append("<p> <span class=\"title\">Area:</span> <br /> ");
            fw.append(area);
            fw.append("</p>");

            fw.append("<p> <a href=\"" + coloursAndMeansUrl + "\">");
            fw.append("<span class=\"title\">Group means and colours (csv)</span>  ");
            fw.append("</a>");
            fw.append("</p>");

            fw.append("</body> </html> ");

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * exports a geoserver sld file for legend generation
     * <p/>
     * TODO: find out why it reports error when attached layer is called
     *
     * @param filename sld filename to export into
     * @param means    mean values for each legend record as [n][m] where n is
     *                 number of records m is number of layers used to generate the
     *                 classification (ALOC)
     * @param colours  RGB colours of legend records as [n][3] where n is number
     *                 of records [][0] is red [][1] is green [][2] is blue
     * @param layers   layers used to generate the classification as Layer[]
     * @param id       unique id (likely to be session_id) as String
     */
    static void exportSLD(String filename, double[][] means, int[][] colours, String[] layers, String id) {
        try {
            StringBuffer sld = new StringBuffer();
            sld.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

            /*
             * header
             */
            sld.append("<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\"");
            sld.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            sld.append(" xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">");
            sld.append(" <NamedLayer>");
            sld.append(" <Name>aloc_" + id + "</Name>");
            sld.append(" <UserStyle>");
            sld.append(" <Name>aloc_" + id + "</Name>");
            sld.append(" <Title>ALA ALOC distribution</Title>");
            sld.append(" <FeatureTypeStyle>");
            sld.append(" <Rule>");
            sld.append(" <RasterSymbolizer>");
            sld.append(" <ColorMap type=\"values\" >");

            int i, j;
            String s;

            /*
             * outputs
             */
            for (i = 0; i < colours.length; i++) {
                j = 0x00000000 | ((colours[i][0] << 16) | (colours[i][1] << 8) | colours[i][2]);
                s = Integer.toHexString(j).toUpperCase();
                while (s.length() < 6) {
                    s = "0" + s;
                }
                sld.append("<ColorMapEntry color=\"#" + s + "\" quantity=\"" + (i + 1) + ".0\" label=\"group " + (i + 1) + "\" opacity=\"1\"/>\r\n");
            }

            /*
             * footer
             */
            sld.append("</ColorMap></RasterSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>");

            /*
             * write
             */
            FileWriter fw = new FileWriter(filename);
            fw.append(sld.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getGroupRange(int[] groups) {
        if (groups == null) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] < min) {
                min = groups[i];
            }
            if (groups[i] > max) {
                max = groups[i];
            }
        }
        if (min > max) {
            min = 0;
            max = 0;
        }
        return max - min;
    }

    public static void main(String[] args) {
        System.out.println("args[0] = grid files directory\n"
                + "args[1] = number of groups\n"
                + "args[2] = number of threads\n"
                + "args[3] = output path\n");

        //args = new String[] {"/data/modelling/aloc/test", "3", "8", "/data/modelling/aloc/test"};

        String gridfilepath = args[0];
        int numberOfGroups = Integer.parseInt(args[1]);
        int numberOfThreads = Integer.parseInt(args[2]);
        String outputpath = args[3];

        String filename = outputpath + File.separator + "aloc.png";

        String name = "aloc";

        final AnalysisLog log = new AnalysisLog(outputpath + File.separator + "aloc.log");

        if (log != null) {
            log.log("start ALOC");
        }

        /*
         * get data, remove missing values, restrict by optional region
         */
        int i, j;
        j = 0;
        int width = 0, height = 0;

        int pieces = numberOfThreads * 4;

        //identify grid files
        File[] files = new File(gridfilepath).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                //use grid files where MinValue < MaxValue
                if (name.endsWith(".grd") || name.endsWith(".GRD")) {
                    try {
                        Grid g = new Grid(dir.getPath() + File.separator + name.substring(0, name.length() - 4));
                        if (g != null) {
                            if (g.minval >= g.maxval) {
                                log.log("layer " + name + " excluded from classification because it has no invariance for the selected area.");
                            }
                            return g.minval < g.maxval;
                        }
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        });

        File[] invariantFiles = new File(gridfilepath).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                //use grid files where MinValue < MaxValue
                if (name.endsWith(".grd") || name.endsWith(".GRD")) {
                    try {
                        Grid g = new Grid(dir.getPath() + File.separator + name.substring(0, name.length() - 4));
                        if (g != null) {
                            return g.minval >= g.maxval;
                        }
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        });

        ArrayList<Object> data_pieces = loadGrids(files, pieces, outputpath, log);
        if (data_pieces == null) {
            return;
        }

        //number of pieces may have changed
        pieces = data_pieces.size() - 2;

        log.log("set cells length");

        double[] extents = (double[]) data_pieces.get(data_pieces.size() - 1);
        width = (int) extents[0];
        height = (int) extents[1];

        String[] layers = new String[files.length];
        for (i = 0; i < files.length; i++) {
            layers[i] = files[i].getName();
        }

        String[] invariantLayers = new String[invariantFiles.length];
        for (i = 0; i < invariantFiles.length; i++) {
            invariantLayers[i] = invariantFiles[i].getName();
        }

        /*
         * run aloc Note: requested number of groups may not always equal
         * request
         */
        int[] iterationCount = new int[1];
        int[] groups = au.org.ala.spatial.analysis.aloc.Aloc.runGowerMetricThreadedMemory(data_pieces, numberOfGroups, layers.length, pieces, layers, log, numberOfThreads, iterationCount);
        if (groups == null || getGroupRange(groups) < 2) {
            log.err("Classification failed to generate >1 groups");
        }

        log.log("identified groups");

        /*
         * recalculate group counts
         */
        int newNumberOfGroups = 0;
        for (i = 0; i < groups.length; i++) {
            if (groups[i] > newNumberOfGroups) {
                newNumberOfGroups = groups[i];
            }
        }
        newNumberOfGroups++; //group number is 0..n-1
        numberOfGroups = newNumberOfGroups;

        /*
         * calculate group means
         */
        double[][] group_means = new double[numberOfGroups][layers.length];
        int[][] group_counts = new int[numberOfGroups][layers.length];

        /*
         * determine group means
         */
        int row = 0;
        for (int k = 0; k < pieces; k++) {
            float[] d = (float[]) data_pieces.get(k);
            for (i = 0; i < d.length; i += layers.length, row++) {
                for (j = 0; j < layers.length; j++) {
                    if (!Float.isNaN(d[i + j])) {
                        group_counts[groups[row]][j]++;
                        group_means[groups[row]][j] += d[i + j];
                    }
                }
            }
        }

        double[][] group_means_copy = new double[group_means.length][group_means[0].length];
        for (i = 0; i < group_means.length; i++) {
            for (j = 0; j < group_means[i].length; j++) {
                if (group_counts[i][j] > 0) {
                    group_means[i][j] /= group_counts[i][j];
                    group_means_copy[i][j] = group_means[i][j];
                }
            }
        }

        log.log("determined group means");

        /*
         * get RGB for colouring group means via PCA
         */
        int[][] colours = Pca.getColours(group_means_copy);
        log.log("determined group colours");

        /*
         * export means + colours
         */
        exportMeansColours(filename.replace("aloc.png", "classification_means.csv"), group_means, colours, layers);
        log.log("exported group means and colours");

        /*
         * export metadata html
         */
        exportMetadata(filename.replace("aloc.png", "classification") + ".html", numberOfGroups, layers, invariantLayers,
                "<insert job number here>",
                "classification_means.csv",
                "", //(job != null) ? job.area : "",
                width, height, extents[2], extents[3], extents[4], extents[5],
                iterationCount[0]);

        /*
         * export geoserver sld file for legend
         */
        exportSLD(filename + ".sld", group_means, colours, layers, "");

        /*
         * map back as colours, grey scale for now
         */
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        int[] image_bytes;

        image_bytes = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                null, 0, image.getWidth());

        /*
         * try transparency as missing value
         */
        for (i = 0; i < image_bytes.length; i++) {
            image_bytes[i] = 0x00000000;
        }

        int[][] cells = (int[][]) data_pieces.get(data_pieces.size() - 2);
        int[] colour = new int[3];
        for (i = 0; i < groups.length; i++) {
            for (j = 0; j < colour.length; j++) {
                colour[j] = (int) (colours[groups[i]][j]);
            }

            //set up rgb colour for this group (upside down)
            image_bytes[cells[i][0] + (height - cells[i][1] - 1) * width] = 0xff000000 | ((colour[0] << 16) | (colour[1] << 8) | colour[2]);
        }

        /*
         * write bytes to image
         */
        image.setRGB(0, 0, image.getWidth(), image.getHeight(),
                image_bytes, 0, image.getWidth());

        /*
         * save image
         */
        try {
            ImageIO.write(image, "png",
                    new File(filename));
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        log.log("saved image");

        log.log("finished ALOC");

        //write grid file
        double[] grid_data = new double[height * width];
        for (i = 0; i < grid_data.length; i++) {
            grid_data[i] = Double.NaN;
        }
        for (i = 0; i < groups.length; i++) {
            for (j = 0; j < colour.length; j++) {
                colour[j] = (int) (colours[groups[i]][j]);
            }
            grid_data[cells[i][0] + (height - cells[i][1] - 1) * width] = groups[i] + 1; //set grid values to "1 to number of groups" instead of "0 to number of groups - 1"
        }
        Grid g = new Grid(null);
        float res = (float) ((extents[4] - extents[2]) / width);
        g.writeGrid(filename.replace("aloc.png", name), grid_data, extents[2], extents[3], extents[4], extents[5],
                res, res,
                height, width);

        //export sld
        exportSLD(filename.replace("aloc.png", name + ".sld"), group_means, colours, layers, "0");

        //export ASCGRID
        BufferedWriter fw = null;
        try {
            fw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(filename.replace("aloc.png", name + ".asc")), "US-ASCII"));
            fw.append("ncols ").append(String.valueOf(width)).append("\n");
            fw.append("nrows ").append(String.valueOf(height)).append("\n");
            fw.append("xllcorner ").append(String.valueOf(extents[2])).append("\n");
            fw.append("yllcorner ").append(String.valueOf(extents[3])).append("\n");
            fw.append("cellsize ").append(String.valueOf(res)).append("\n");

            fw.append("NODATA_value ").append(String.valueOf(-1));

            for (i = 0; i < height; i++) {
                fw.append("\n");
                for (j = 0; j < width; j++) {
                    if (j > 0) {
                        fw.append(" ");
                    }
                    if (Double.isNaN(grid_data[i * width + j])) {
                        fw.append("-1");
                    } else {
                        fw.append(String.valueOf(grid_data[i * width + j]));
                    }
                }
            }
            fw.append("\n");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }

        log.close();
    }

    static String readFile(String file) {
        String s = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            byte[] b = new byte[(int) raf.length()];
            raf.read(b);
            raf.close();
            s = new String(b, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    private static void writeProjectionFile(String filename) {
        try {
            PrintWriter spWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

            StringBuffer sbProjection = new StringBuffer();
            sbProjection.append("GEOGCS[\"WGS 84\", ").append("\n");
            sbProjection.append("    DATUM[\"WGS_1984\", ").append("\n");
            sbProjection.append("        SPHEROID[\"WGS 84\",6378137,298.257223563, ").append("\n");
            sbProjection.append("            AUTHORITY[\"EPSG\",\"7030\"]], ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"6326\"]], ").append("\n");
            sbProjection.append("    PRIMEM[\"Greenwich\",0, ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"8901\"]], ").append("\n");
            sbProjection.append("    UNIT[\"degree\",0.01745329251994328, ").append("\n");
            sbProjection.append("        AUTHORITY[\"EPSG\",\"9122\"]], ").append("\n");
            sbProjection.append("    AUTHORITY[\"EPSG\",\"4326\"]] ").append("\n");

            spWriter.write(sbProjection.toString());
            spWriter.close();

        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    private static ArrayList<Object> loadGrids(File[] files, int pieces, String outputPath, AnalysisLog log) {
        ArrayList<Object> data = new ArrayList<Object>();

        //determine outer bounds of layers
        double xmin = Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE;
        double xmax = Double.MAX_VALUE * -1;
        double ymax = Double.MAX_VALUE * -1;
        double xres = 0.01;
        double yres = 0.01;
        for (File f : files) {
            String gridFilename = f.getPath().substring(0, f.getPath().length() - 4);
            Grid g = new Grid(gridFilename);
            xres = g.xres;
            yres = g.xres;
            if (xmin > g.xmin) {
                xmin = g.xmin;
            }
            if (xmax < g.xmax) {
                xmax = g.xmax;
            }
            if (ymin > g.ymin) {
                ymin = g.ymin;
            }
            if (ymax < g.ymax) {
                ymax = g.ymax;
            }
        }

        if (files.length < 2) {
            log.err("Fewer than two layers with postive range.");

            return null;
        }


        //determine range and width's
        double xrange = xmax - xmin;
        double yrange = ymax - ymin;
        int width = (int) Math.ceil(xrange / xres);
        int height = (int) Math.ceil(yrange / yres);

        //write extents into a file now
        String extentsFilename = outputPath + File.separator + "extents.txt";
        try {
            FileWriter fw = new FileWriter(extentsFilename);
            fw.append(String.valueOf(width)).append("\n");
            fw.append(String.valueOf(height)).append("\n");
            fw.append(String.valueOf(xmin)).append("\n");
            fw.append(String.valueOf(ymin)).append("\n");
            fw.append(String.valueOf(xmax)).append("\n");
            fw.append(String.valueOf(ymax));
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.log("exported extents");

        //make cells list for outer bounds
        int th = height;
        int tw = width;
        int tp = 0;
        int[][] cells = new int[tw * th][2];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[tp][0] = j;
                cells[tp][1] = i;
                tp++;
            }
        }

        log.log("determined target cells");

        log.log("Cut cells count: " + cells.length);

        //transform cells numbers to long/lat numbers
        double[][] points = new double[cells.length][2];
        for (int i = 0; i < cells.length; i++) {
            points[i][0] = xmin + cells[i][0] * xres;
            points[i][1] = ymin + cells[i][1] * yres;
        }

        //initialize data structure to hold everything
        // each data piece: row1[col1, col2, ...] row2[col1, col2, ...] row3...
        int remainingLength = cells.length;
        int step = (int) Math.floor(remainingLength / (double) pieces);
        for (int i = 0; i < pieces; i++) {
            if (i == pieces - 1) {
                data.add(new float[remainingLength * files.length]);
            } else {
                data.add(new float[step * files.length]);
                remainingLength -= step;
            }
        }

        //iterate for layers
        double[] layerExtents = new double[files.length * 2];
        for (int j = 0; j < files.length; j++) {
            String gridFilename = files[j].getPath().substring(0, files[j].getPath().length() - 4);
            Grid g = new Grid(gridFilename);
            float[] v = g.getValues2(points);

            //row range standardization
            float minv = Float.MAX_VALUE;
            float maxv = Float.MAX_VALUE * -1;
            for (int i = 0; i < v.length; i++) {
                if (v[i] < minv) {
                    minv = v[i];
                }
                if (v[i] > maxv) {
                    maxv = v[i];
                }
            }
            float range = maxv - minv;
            if (range > 0) {
                for (int i = 0; i < v.length; i++) {
                    v[i] = (v[i] - minv) / range;
                }
            } else {
                for (int i = 0; i < v.length; i++) {
                    v[i] = 0;
                }
            }
            layerExtents[j * 2] = minv;
            layerExtents[j * 2 + 1] = maxv;

            //iterate for pieces
            for (int i = 0; i < pieces; i++) {
                float[] d = (float[]) data.get(i);
                for (int k = j, n = i * step; k < d.length; k += files.length, n++) {
                    d[k] = v[n];
                }
            }

            log.log("opened grid: " + files[j].getName());
        }

        log.log("finished opening grids");

        //remove null rows from data and cells
        int newCellPos = 0;
        int currentCellPos = 0;
        for (int i = 0; i < pieces; i++) {
            float[] d = (float[]) data.get(i);
            int newPos = 0;
            for (int k = 0; k < d.length; k += files.length) {
                int nMissing = 0;
                for (int j = 0; j < files.length; j++) {
                    if (Float.isNaN(d[k + j])) {
                        nMissing++;
                    }
                }
                //if (nMissing < files.length) {
                if (nMissing == 0) {
                    if (newPos < k) {
                        for (int j = 0; j < files.length; j++) {
                            d[newPos + j] = d[k + j];
                        }
                    }
                    newPos += files.length;
                    if (newCellPos < currentCellPos) {
                        cells[newCellPos][0] = cells[currentCellPos][0];
                        cells[newCellPos][1] = cells[currentCellPos][1];
                    }
                    newCellPos++;
                }
                currentCellPos++;
            }
            if (newPos < d.length) {
                d = java.util.Arrays.copyOf(d, newPos);
                data.set(i, d);
            }
        }

        //remove zero length data pieces
        for (int i = pieces - 1; i >= 0; i--) {
            float[] d = (float[]) data.get(i);
            if (d.length == 0) {
                data.remove(i);
            }
        }

        //add cells reference to output
        data.add(cells);

        //add extents to output
        double[] extents = new double[6 + layerExtents.length];
        extents[0] = width;
        extents[1] = height;
        extents[2] = xmin;
        extents[3] = ymin;
        extents[4] = xmax;
        extents[5] = ymax;
        for (int i = 0; i < layerExtents.length; i++) {
            extents[6 + i] = layerExtents[i];
        }
        data.add(extents);

        log.log("cleaned data");

        return data;
    }
}
