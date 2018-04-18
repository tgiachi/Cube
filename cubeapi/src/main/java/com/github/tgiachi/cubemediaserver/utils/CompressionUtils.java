package com.github.tgiachi.cubemediaserver.utils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


import java.util.ArrayList;
import java.util.List;




public class CompressionUtils {


    public static boolean unzipFile(String filename, String outDirectory)
    {
        List<String> outDirectoryZip = new ArrayList<>();
        try
        {

            try {
                ZipFile zipFile = new ZipFile(filename);
                zipFile.extractAll(outDirectory);

            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
        catch (Exception ex)
        {
            return false;

        }

        return true;

    }
}
