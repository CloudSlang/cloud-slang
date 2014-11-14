package com.hp.score.lang.cli.utils;


import org.apache.commons.io.FilenameUtils;


import java.io.*;

/**
 * Date: 11/13/2014
 *
 * @author lesant
 */


public class CompilerUtils {
    public static File getFile(String filePath) throws IOException{
        File targetFile;
        byte[] buffer;

        try (InputStream inStream = CompilerUtils.class.getClassLoader().getResourceAsStream(filePath)){
            buffer = new byte[inStream.available()];
            inStream.read(buffer);
            String fileName = FilenameUtils.getName(filePath);
            targetFile = new File(fileName);
            targetFile.deleteOnExit();
        }

        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(buffer);
        }
        return targetFile;
    }


}
