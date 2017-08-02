package com.ufmg.ppsus.xmlflat_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class Util {

    public static String getPath(String path) throws UnsupportedEncodingException {
        String fullPath = URLDecoder.decode(path, "UTF-8").replace("classes", Constants.PATH_ARCHETYPE);
        return new File(fullPath).getPath();
    }
    
    /**
     * save uploaded file to a defined location on the server
     *
     * @param uploadedInputStream
     * @param serverLocation
     * @throws IOException
     */
    public static void saveFile(InputStream uploadedInputStream,
            String serverLocation) throws IOException {
        OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
        int read = 0;
        byte[] bytes = new byte[1024];
        outpuStream = new FileOutputStream(new File(serverLocation));
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            outpuStream.write(bytes, 0, read);
        }
        outpuStream.flush();
        outpuStream.close();
    }

    /**
     * Convert file to a string
     *
     * @param uploadedInputStream
     * @return
     * @throws IOException
     */
    public static String fileToString(InputStream uploadedInputStream) throws IOException {
        return IOUtils.toString(uploadedInputStream, Constants.UTF8);
    }

    /**
     * Save log file
     *
     * @param path
     * @param fileName
     * @param out
     * @throws IOException
     */
    public static void saveLogFile(String path, String fileName, String out) throws IOException {
        FileWriter arq = new FileWriter(path + "\\" + getDateTime() + fileName.replace(".", "_") + ".txt");
        PrintWriter saveArq = new PrintWriter(arq);
        saveArq.printf(out.toString());
        arq.close();
    }

    /**
     *
     * @param archetypeId
     * @throws Exception
     */
    public static void archetypeNameRules(String archetypeId) throws Exception {
        if (archetypeId == null || archetypeId.isEmpty()) {
            throw new Exception(Constants.Error.ARCHETYPE_ID_NULL);
        }
        if (!archetypeId.contains("adl")) {
            throw new Exception(Constants.Error.ARCHETYPE_ADL_NULL);
        }
        if (!archetypeId.contains("COMPOSITION")) {
            throw new Exception(Constants.Error.ARCHETYPE_COMPOSITION_NULL);
        }
    }

    /**
     * return current date and time
     *
     * @return
     */
    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * remove especial char
     *
     * @param str
     * @return
     */
    public static String removeSpecialChars(String str) {
        String ret = str.replaceAll("\\(+", "");
        ret = ret.trim();
        ret = ret.replaceAll("\\)+", "");
        ret = ret.replaceAll("\\,+", "");
        ret = ret.replaceAll("\\;+", "");
        ret = ret.replaceAll("\\/+", "");
        ret = ret.replaceAll("\\s+", "_");
        ret = ret.replaceAll("\\\\+", "_");
        ret = ret.replaceAll("\\-+", "_");
        ret = ret.replaceAll("\\.+", "_");
        return Normalizer.normalize(ret, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

}
