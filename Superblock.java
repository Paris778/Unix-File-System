package dataStructures;

import java.util.*;
import java.io.*;
import java.nio.*;

import coreFileSystemFunctionality.*;
//import dataStructures.*;
import misc.*;

/**
 * This is the Superblock class.
 * It extracts all the useful informatin from the superblock.
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */


public class Superblock {

    private short magicNumber;
    private int numberOfBlocksSystem; // 1
    private int numberOfBlocksPerGroup; // 2
    private int numberOfInodesPerGroup; // 3
    private int sizeOfInode; // 4
    private int groupsInSystem; // 5
    private int sizeOfGroups; // 6
    private int totalNumberOfInodes;

    private String volumeName = null;

    private ByteBuffer byteBuffer;

    /**
     * Constructor.
     * @param b An array of bytes which belong to a Superblock (size 1024)
     */
    public Superblock(byte[] b) {

        try {
            byteBuffer = ByteBuffer.wrap(b);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // System.out.println("1. Exception in Superblock :" + "\n" + e.getMessage() );

        try {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //////////////////////////

    }

    // Accessor methods

    // 1
    /**
     * Accesor Method.
     * This method retrieves the Number of Blocks in the filesystem.
     * @return The number of blocks in the filesystem.
     */
    public int getNumberOfBlocksInSystem() {

        return this.numberOfBlocksSystem;
    }

    // 2
    /**
     * Accesor Method.
     * This method retrieves the Number of Blocks per group.
     * @return The number of blocks per group.
     */
    public int getNumberOfBlocksPerGroup() {

        return this.numberOfBlocksPerGroup;

    }

    // 3
    /**
     * Accesor Method.
     * This method retrieves the Number of inodes per group.
     * @return The number of inodes per group.
     */
    public int getNumberOfInodesPerGroup() {

        return this.numberOfInodesPerGroup;

    }

    // 4
    /**
     * Accesor Method.
     * This method retrieves the size of inodes.
     * @return The size of inodes.
     */
    public int getSizeOfInodes() {

        return this.sizeOfInode;

    }

    // 5
    /**
     * Accesor Method.
     * This method retrieves the Number of groups in the filesystem.
     * @return The number of groups in the filesystem.
     */
    public int getNumberOfGroupsInSystem() {

        return this.groupsInSystem;
    }

    // 6
    /**
     * Accesor Method.
     * This method retrieves the size of each group in bytes.
     * @return The size of each group in bytes.
     */
    public int getSizeOfGroups() {

        return this.sizeOfGroups;

    }

    /**
     * Accesor Method.
     * This method retrieves the Number of inodes in the filesystem.
     * @return The number inodes in the filesystem.
     */
    public int getNumberOfInodesSystem() {

        return this.totalNumberOfInodes;

    }

    /**
     * Accesor Method.
     * This method retrieves the magic number.
     * @return The magic number.
     */
    public short getMagicNumber() {

        return this.magicNumber;
    }

    /**
     * Accesor Method.
     * This method retrieves the Name of the volume (disk name).
     * @return The Name of the filesystem.
     */
    public String getVolumeName() {
        return this.volumeName;
    }

    /**
     * This method extracts al the relevant information from the superblock and stores
     * them in the relevant variables.
     */
    public void extractAllData() {
        // Determining values

        try {

            this.magicNumber = byteBuffer.getShort(56);
            this.totalNumberOfInodes = byteBuffer.getInt(0);
            this.numberOfBlocksSystem = byteBuffer.getInt(4);
            this.numberOfBlocksPerGroup = byteBuffer.getInt(32);
            this.numberOfInodesPerGroup = byteBuffer.getInt(40);
            this.sizeOfInode = byteBuffer.getInt(88);
            this.sizeOfGroups = this.sizeOfInode * this.numberOfInodesPerGroup;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        int temp = this.numberOfBlocksSystem / this.numberOfBlocksPerGroup;
        if (this.numberOfBlocksSystem % this.numberOfBlocksPerGroup != 0) {
            temp += 1;
        }

        this.groupsInSystem = temp;

        // Find the name of the volume
        byte[] volumeCharacters = new byte[16]; // 16 is the length of the name of the filesystem

        for (int x = 0; x < 16; x++) {
            volumeCharacters[x] = byteBuffer.get(120 + x); // 120 is the filesystem name offset
        }

        volumeName = new String(volumeCharacters);

    }

    /**
     * This method prints all the extracted information of the superblock
     * on the console.
     * void extractAllData() should be called before.
     */
    public void printAllSuperblockData() {

        try {

            System.out.println("\n==========================================");
            System.out.println("=             File System Data           =");
            System.out.println("==========================================\n");

            System.out.println("Volume Name: " + this.volumeName);
            System.out.println("Magic Number : " + Helper.makeHex(this.magicNumber));
            System.out.println("Inodes in system: " + this.totalNumberOfInodes);
            System.out.println("Inodes per group: " + this.numberOfInodesPerGroup);
            System.out.println("Inode size: " + this.sizeOfInode);
            System.out.println("Total number of blocks in system: " + this.numberOfBlocksSystem);
            System.out.println("Blocks per group: " + this.numberOfBlocksPerGroup);
            System.out.println("Total number of groups in system: " + this.groupsInSystem);
            System.out.println("Size of each group: " + this.sizeOfGroups);

            if (Helper.makeHex(this.magicNumber).equals("0xef53")) {
                System.out.println("Yayyyy !!! The magic number is CORRECT !!!!");
            } else {
                System.out.println("Oooof ... The magic number is WRONG ..... ");
            }

        } catch (Exception e) {
            System.out.println("---------There was an exception in superblock while trying to print out data.");
        }

        System.out.println("==========================================\n");
    }

}