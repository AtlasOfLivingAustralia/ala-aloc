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

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AnalysisLog {

    private final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";

    FileWriter fw;
    String filename;

    public AnalysisLog(String filename) {
        this.filename = filename;
        try {
            fw = new FileWriter(filename, true);
        } catch (Exception e) {
            System.out.println("error opening log file");
            e.printStackTrace();
        }
    }

    private String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public void log(String s) {
        output(s, false);
    }

    public void err(String s) {
        output(s, true);
    }

    void output(String s, boolean error) {
        if (error) {
            System.err.println(s);
        } else {
            System.out.println(s);
        }
        if (fw != null) {
            try {
                if (error) {
                    fw.write("ERROR: ");
                }
                fw.write(s);
                fw.write("\n");
                fw.flush();
            } catch (Exception e) {
                System.out.println("error writting to log file");
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (fw != null) {
            try {
                fw.flush();
                fw.close();
            } catch (Exception e) {
                System.out.println("error closing log file");
                e.printStackTrace();
            }
        }
    }
}
