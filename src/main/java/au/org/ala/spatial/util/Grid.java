/**************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 * <p>
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * <p>
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.spatial.util;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Grid { //  implements Serializable
    final double noDataValueDefault = -3.4E38;
    public Boolean byteorderLSB = true; // true if file is LSB (Intel)
    public int ncols, nrows;
    public double nodatavalue;
    public double xmin, xmax, ymin, ymax;
    public double xres, yres;
    public String datatype;
    // properties
    public double minval, maxval;
    public String filename;
    public String units;

    byte nbytes;
    float[] grid_data = null;
    public float rescale = 1;

    /**
     * loads grd for gri file reference
     *
     * @param fname full path and file name without file extension
     *              of .gri and .grd files to open
     */
    public Grid(String fname) { // construct Grid from file
        filename = fname;
        File grifile = new File(filename + ".gri");
        if (!grifile.exists()) {
            grifile = new File(filename + ".GRI");
        }
        File grdfile = new File(filename + ".grd");
        if (!grdfile.exists()) {
            grdfile = new File(filename + ".GRD");
        }
        if (grdfile.exists() && grifile.exists()) {
            readgrd(filename);

            //update xres/yres when xres == 1
            if (xres == 1) {
                xres = (xmax - xmin) / nrows;
                yres = (ymax - ymin) / ncols;
            }
        } else {
            if (fname != null) {
                System.out.println("cannot find GRID: " + fname);
            }
        }
    }

    //transform to file position
    public int getcellnumber(double x, double y) {
        if (x < xmin || x > xmax || y < ymin || y > ymax) //handle invalid inputs
        {
            return -1;
        }

        int col = (int) ((x - xmin) / xres);
        int row = this.nrows - 1 - (int) ((y - ymin) / yres);

        //limit each to 0 and ncols-1/nrows-1
        if (col < 0) {
            col = 0;
        }
        if (row < 0) {
            row = 0;
        }
        if (col >= ncols) {
            col = ncols - 1;
        }
        if (row >= nrows) {
            row = nrows - 1;
        }
        return (row * ncols + col);
    }

    private void setdatatype(String s) {
        s = s.toUpperCase();

        // Expected from grd file
        if (s.equals("INT1BYTE")) {
            datatype = "BYTE";
        } else if (s.equals("INT2BYTES")) {
            datatype = "SHORT";
        } else if (s.equals("INT4BYTES")) {
            datatype = "INT";
        } else if (s.equals("INT8BYTES")) {
            datatype = "LONG";
        } else if (s.equals("FLT4BYTES")) {
            datatype = "FLOAT";
        } else if (s.equals("FLT8BYTES")) {
            datatype = "DOUBLE";
        } // shorthand for same
        else if (s.equals("INT1B") || s.equals("BYTE")) {
            datatype = "BYTE";
        } else if (s.equals("INT1U") || s.equals("UBYTE")) {
            datatype = "UBYTE";
        } else if (s.equals("INT2B") || s.equals("INT16") || s.equals("INT2S")) {
            datatype = "SHORT";
        } else if (s.equals("INT4B")) {
            datatype = "INT";
        } else if (s.equals("INT8B") || s.equals("INT32")) {
            datatype = "LONG";
        } else if (s.equals("FLT4B") || s.equals("FLOAT32") || s.equals("FLT4S")) {
            datatype = "FLOAT";
        } else if (s.equals("FLT8B")) {
            datatype = "DOUBLE";
        } // if you rather use Java keywords...
        else if (s.equals("BYTE")) {
            datatype = "BYTE";
        } else if (s.equals("SHORT")) {
            datatype = "SHORT";
        } else if (s.equals("INT")) {
            datatype = "INT";
        } else if (s.equals("LONG")) {
            datatype = "LONG";
        } else if (s.equals("FLOAT")) {
            datatype = "FLOAT";
        } else if (s.equals("DOUBLE")) {
            datatype = "DOUBLE";
        } // some backwards compatibility
        else if (s.equals("INTEGER")) {
            datatype = "INT";
        } else if (s.equals("SMALLINT")) {
            datatype = "INT";
        } else if (s.equals("SINGLE")) {
            datatype = "FLOAT";
        } else if (s.equals("REAL")) {
            datatype = "FLOAT";
        } else {
            System.out.println("GRID unknown type: " + s);
            datatype = "UNKNOWN";
        }

        if (datatype.equals("BYTE") || datatype.equals("UBYTE")) {
            nbytes = 1;
        } else if (datatype.equals("SHORT")) {
            nbytes = 2;
        } else if (datatype.equals("INT")) {
            nbytes = 4;
        } else if (datatype.equals("LONG")) {
            nbytes = 8;
        } else if (datatype.equals("SINGLE")) {
            nbytes = 4;
        } else if (datatype.equals("DOUBLE")) {
            nbytes = 8;
        } else {
            nbytes = 0;
        }
    }

    private void readgrd(String filename) {
        IniReader ir = null;
        if ((new File(filename + ".grd")).exists()) {
            ir = new IniReader(filename + ".grd");
        } else {
            ir = new IniReader(filename + ".GRD");
        }

        setdatatype(ir.getStringValue("Data", "DataType"));
        maxval = (float) ir.getDoubleValue("Data", "MaxValue");
        minval = (float) ir.getDoubleValue("Data", "MinValue");
        ncols = ir.getIntegerValue("GeoReference", "Columns");
        nrows = ir.getIntegerValue("GeoReference", "Rows");
        xmin = ir.getDoubleValue("GeoReference", "MinX");
        ymin = ir.getDoubleValue("GeoReference", "MinY");
        xmax = ir.getDoubleValue("GeoReference", "MaxX");
        ymax = ir.getDoubleValue("GeoReference", "MaxY");
        xres = ir.getDoubleValue("GeoReference", "ResolutionX");
        yres = ir.getDoubleValue("GeoReference", "ResolutionY");
        if (ir.valueExists("Data", "NoDataValue")) {
            nodatavalue = ir.getDoubleValue("Data", "NoDataValue");
        } else {
            nodatavalue = Double.NaN;
        }

        String s = ir.getStringValue("Data", "ByteOrder");

        byteorderLSB = true;
        if (s != null && s.length() > 0) {
            if (s.equals("MSB")) {
                byteorderLSB = false;
            }// default is windows (LSB), not linux or Java (MSB)
        }

        units = ir.getStringValue("Data", "Units");

        //make a rescale value
        if (units != null && units.startsWith("1/")) {
            try {
                rescale = 1 / Float.parseFloat(units.substring(2, units.indexOf(' ')));
            } catch (Exception e) {
            }
        }
        if (units != null && units.startsWith("x")) {
            try {
                rescale = Float.parseFloat(units.substring(1, units.indexOf(' ')));
            } catch (Exception e) {
            }
        }
        if (rescale != 1) {
            units = units.substring(units.indexOf(' ') + 1);
            maxval *= rescale;
            minval *= rescale;
        }
    }

    public float[] getGrid() {
        int maxArrayLength = Integer.MAX_VALUE - 10;

        if (grid_data != null) {
            return grid_data;
        }

        int length = nrows * ncols;

        float[] ret = new float[length];

        RandomAccessFile afile;
        File f2 = new File(filename + ".GRI");

        try { //read of random access file can throw an exception
            if (!f2.exists()) {
                afile = new RandomAccessFile(filename + ".gri", "r");
            } else {
                afile = new RandomAccessFile(filename + ".GRI", "r");
            }

            byte[] b = new byte[(int) Math.min(afile.length(), maxArrayLength)];

            int i = 0;
            int max = 0;
            int len;
            while ((len = afile.read(b)) > 0) {
                ByteBuffer bb = ByteBuffer.wrap(b);

                if (byteorderLSB) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }

                if (datatype.equalsIgnoreCase("UBYTE")) {
                    max += len;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.get();
                        if (ret[i] < 0) {
                            ret[i] += 256;
                        }
                    }
                } else if (datatype.equalsIgnoreCase("BYTE")) {
                    max += len;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.get();
                    }
                } else if (datatype.equalsIgnoreCase("SHORT")) {
                    max += len / 2;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.getShort();
                    }
                } else if (datatype.equalsIgnoreCase("INT")) {
                    max += len / 4;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.getInt();
                    }
                } else if (datatype.equalsIgnoreCase("LONG")) {
                    max += len / 8;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.getLong();
                    }
                } else if (datatype.equalsIgnoreCase("FLOAT")) {
                    max += len / 4;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = bb.getFloat();
                    }
                } else if (datatype.equalsIgnoreCase("DOUBLE")) {
                    max += len / 8;
                    max = Math.min(max, ret.length);
                    for (; i < max; i++) {
                        ret[i] = (float) bb.getDouble();
                    }
                } else {
                    // / should not happen; catch anyway...
                    max += len / 4;
                    for (; i < max; i++) {
                        ret[i] = Float.NaN;
                    }
                }
            }

            //replace not a number
            for (i = 0; i < length; i++) {
                if ((float) ret[i] == (float) nodatavalue) {
                    ret[i] = Float.NaN;
                } else {
                    ret[i] *= rescale;
                }
            }

            afile.close();
        } catch (Exception e) {
            System.out.println("An error has occurred - probably a file error");
            e.printStackTrace();
        }
        grid_data = ret;
        return ret;
    }

    /**
     * for grid cutter
     * <p/>
     * writes out a list of double (same as getGrid() returns) to a file
     * <p/>
     * byteorderlsb
     * data type, FLOAT
     *
     * @param newfilename
     * @param dfiltered
     */
    public void writeGrid(String newfilename, double[] dfiltered, double xmin, double ymin, double xmax, double ymax, double xres, double yres, int nrows, int ncols) {
        int size, i, length = dfiltered.length;
        double maxvalue = Double.MAX_VALUE * -1;
        double minvalue = Double.MAX_VALUE;

        //write data as whole file
        RandomAccessFile afile;
        try { //read of random access file can throw an exception
            afile = new RandomAccessFile(newfilename + ".gri", "rw");

            size = 4;
            byte[] b = new byte[size * length];
            ByteBuffer bb = ByteBuffer.wrap(b);

            if (byteorderLSB) {
                bb.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                bb.order(ByteOrder.BIG_ENDIAN);
            }
            for (i = 0; i < length; i++) {
                if (Double.isNaN(dfiltered[i])) {
                    bb.putFloat((float) noDataValueDefault);
                } else {
                    if (minvalue > dfiltered[i]) {
                        minvalue = dfiltered[i];
                    }
                    if (maxvalue < dfiltered[i]) {
                        maxvalue = dfiltered[i];
                    }
                    bb.putFloat((float) dfiltered[i]);
                }
            }

            afile.write(b);

            afile.close();
        } catch (Exception e) {
            System.out.println("error writing grid file");
            e.printStackTrace();
        }

        writeHeader(newfilename, xmin, ymin, xmin + xres * ncols, ymin + yres * nrows, xres, yres, nrows, ncols, minvalue, maxvalue, "FLT4BYTES", String.valueOf(noDataValueDefault));
    }


    public void writeHeader(String newfilename, double xmin, double ymin, double xmax, double ymax, double xres, double yres, int nrows, int ncols, double minvalue, double maxvalue, String datatype, String nodata) {
        try {
            FileWriter fw = new FileWriter(newfilename + ".grd");

            fw.append("[General]");
            fw.append("\r\n").append("Title=").append(newfilename);
            fw.append("\r\n").append("[GeoReference]");
            fw.append("\r\n").append("Projection=GEOGRAPHIC");
            fw.append("\r\n").append("Datum=WGS84");
            fw.append("\r\n").append("Mapunits=DEGREES");
            fw.append("\r\n").append("Columns=").append(String.valueOf(ncols));
            fw.append("\r\n").append("Rows=").append(String.valueOf(nrows));
            fw.append("\r\n").append("MinX=").append(String.format("%.2f", xmin));
            fw.append("\r\n").append("MaxX=").append(String.format("%.2f", xmax));
            fw.append("\r\n").append("MinY=").append(String.format("%.2f", ymin));
            fw.append("\r\n").append("MaxY=").append(String.format("%.2f", ymax));
            fw.append("\r\n").append("ResolutionX=").append(String.valueOf(xres));
            fw.append("\r\n").append("ResolutionY=").append(String.valueOf(yres));
            fw.append("\r\n").append("[Data]");
            fw.append("\r\n").append("DataType=" + datatype);
            fw.append("\r\n").append("MinValue=").append(String.valueOf(minvalue));
            fw.append("\r\n").append("MaxValue=").append(String.valueOf(maxvalue));
            fw.append("\r\n").append("NoDataValue=").append(nodata);
            fw.append("\r\n").append("Transparent=0");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            System.out.println("error writing grid file header");
            e.printStackTrace();

        }
    }

    /**
     * do get values of grid for provided points.
     * <p/>
     * loads whole grid file as double[] in process
     *
     * @param points
     * @return
     */
    public float[] getValues2(double[][] points) {
        if (points == null || points.length == 0) {
            return null;
        }

        //init output structure
        float[] ret = new float[points.length];

        //load whole grid
        float[] grid = getGrid();
        int glen = grid.length;
        int length = points.length;
        int i, pos;

        //points loop
        for (i = 0; i < length; i++) {
            pos = getcellnumber(points[i][0], points[i][1]);
            if (pos >= 0 && pos < glen) {
                ret[i] = grid[pos];
            } else {
                ret[i] = Float.NaN;
            }
        }

        return ret;
    }


}