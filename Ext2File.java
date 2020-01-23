
package coreFileSystemFunctionality;

import java.util.*;
import java.io.*;
import java.nio.*;

//import coreFileSystemFunctionality.*;
import dataStructures.*;
import misc.*;

/**
 * This is the Ext2File Class.
 * This class reads the ext2 file and extracts the key information.
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */

public class Ext2File {

    private RandomAccessFile raf;
    private byte[] allData;

    /**
     * Constructor
     * @param vol Accepts a volume object.
     */
    public Ext2File(Volume vol) {
        System.out.println("Creating EXT2File...");
        raf = vol.getRAF();
        System.out.println("File:  " + vol.getRAF());
    }

    public byte[] readByteBlock(long adress, long length) {

        byte[] allData = new byte[(int) length];
        try {
            raf.seek(adress);
            raf.readFully(allData);

        } catch (Exception e) {
            System.out.println("Exception in EXT2File : readByteBlock : " + e.getMessage());
        }

        return allData;

    }

    public byte[] readByteBlock(long length) {

        byte[] allData = new byte[(int) length];
        try {
            raf.readFully(allData);
        } catch (Exception e) {
            System.out.println("Exception in EXT2File : readByteBlock- length only : " + e.getMessage());
        }

        return allData;

    }

    public void seek(long position) throws IOException{
        raf.seek(position);
    }

    public long position() throws IOException{
        return raf.getFilePointer();
    }

    public long size() throws IOException {
        return raf.length();
    }
}
