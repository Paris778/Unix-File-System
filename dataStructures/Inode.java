package dataStructures;

import java.util.*;
import java.io.*;
import java.nio.*;

import coreFileSystemFunctionality.Ext2File;
import coreFileSystemFunctionality.Volume;
import dataStructures.Inode;
import dataStructures.Superblock;
import dataStructures.Directory;
import dataStructures.GroupDescriptor;
import misc.Helper;

/**
 * This is the Directory class.
 * It extracts info about directories and their contents.
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */

public class Inode {

    private int adress;
    private short fileMode;
    private short userID;
    private int fileSizeLower;
    private int accessTime;
    private int creationTime;
    private int modifiedTime;
    private int deleteTime;
    private short groupID;
    private short numberOfLinks;
    private int[] blockPointerValue;
    private int fileSizeUpper;

    private ByteBuffer byteBuffer;

    //COnstants
    static final int BLOCK_SIZE = 1024;
    static final int INT_SIZE = 4;

    /**
     * Constructor.
     * @param b Array of bytes containing the inode.
     */
    public Inode(byte[] b) {

        byteBuffer = ByteBuffer.wrap(b);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        blockPointerValue = new int[15]; // Array of 15 i block pointers
        extractInodeInfo();

    }

    /**
     * This method extracts all the useful information from the inode and stores them.
     */
    public void extractInodeInfo() {

        fileMode = byteBuffer.getShort(0);
        userID = byteBuffer.getShort(2);
        fileSizeLower = byteBuffer.getInt(4);
        accessTime = byteBuffer.getInt(8);
        creationTime = byteBuffer.getInt(12);
        modifiedTime = byteBuffer.getInt(16);
        deleteTime = byteBuffer.getInt(20);
        groupID = byteBuffer.getShort(24);
        numberOfLinks = byteBuffer.getShort(26);

        for (int x = 0; x < 15; x++) {
            blockPointerValue[x] = byteBuffer.getInt(40 + (x * 4));
        }

        fileSizeUpper = byteBuffer.getInt(108);
    }

    // Figure out what block we're currently in
    /**
     * This method figures out what block the inode is in.
     * @param offset This is the address of the inode.
     * @param superblock The superblock.
     * @param gDescriptorArray The array of group descriptors 
     * @return The number of the block this inode resides in.
     */
    public static int getInodeBlock(int offset, Superblock superblock, GroupDescriptor[] gDescriptorArray) {

        // populate array of group Descriptor table pointer values
        int[] groupDescriptorPointer = new int[superblock.getNumberOfBlocksInSystem()];

        // Retrieving all the group descriptors
        for (int i = 0; i < gDescriptorArray.length; i++) {
            groupDescriptorPointer[i] = gDescriptorArray[i].getInodeTablePointer();
            // System.out.println("Inode table pointer : " + groupDescriptorPointer[i]);
        }

        int inodeResidence;
        double pointer;
        double containingBlock;

        if (offset >= 2) {
            if (offset < superblock.getNumberOfInodesSystem()) {

                offset--;

                // dividing the inode number with the number of inodes per group
                // to get index of the inode in the Descriptor table

                inodeResidence = offset / superblock.getNumberOfInodesPerGroup();

                // Remainder used to calculate residence block

                pointer = offset % superblock.getNumberOfInodesPerGroup();

                int inodeTablePointer = groupDescriptorPointer[inodeResidence];

                // https://www.nongnu.org/ext2-doc/ext2.html
                containingBlock = ((pointer * superblock.getSizeOfInodes() / BLOCK_SIZE) + inodeTablePointer) * BLOCK_SIZE;

                return (int) containingBlock;

            }
        }

        return 0;
    }

    /**
     * This method retrieves the Inode at the specified path.
     * @param path The path of the file/directory
     * @param file The ext2 file of the filesystem.
     * @param inode The current inode.
     * @param sb The superblock.
     * @param gd The group descriptor array.
     * @return The inode specieifed in the path.
     */
    public static Inode getCurrentInode(String[] path, Ext2File file, Inode inode, Superblock sb,
            GroupDescriptor[] gd) {

        Inode currentInode = inode;
        for (String segment : path) {
            int offset = getInodeAdress(segment, currentInode, file);

            if (offset > 0) {
                byte[] iData = file.readByteBlock(Inode.getInodeBlock(offset, sb, gd), sb.getSizeOfInodes());

                currentInode = new Inode(iData);
                currentInode.extractInodeInfo();
            }
            if (offset < 0) {
                System.out.println("Error: Inode.getCurrentInode : No such file in directory");
                return null;
            }

        }

        return currentInode;
    }

    // Accessor methods

    public String getUSerID() {
        String id;
        if (this.userID == 0) {
            id = "root";
        } else {
            id = "user";
        }
        return id;
    }

    public String getGroupID() {
        String id;
        if (this.groupID == 0) {
            id = "root";
        } else {
            id = "group";
        }
        return id;
    }

    public int getFileSizeLower() {
        return this.fileSizeLower;
    }

    public int getFileSizeUpper() {
        return this.fileSizeUpper;
    }

    public int[] getBlockPointervalue() {
        return this.blockPointerValue;
    }

    public boolean isFile() {
        boolean isfile = false;
        if (((int) fileMode & 0x8000) == 0x8000) {
            isfile = true;
        }
        return isfile;
    }

    public boolean isDirectory() {
        boolean isdirectory = false;
        if (((int) fileMode & 0x4000) == 0x4000) {
            isdirectory = true;
        }
        return isdirectory;
    }

    public Date getCreationDate() {
        return new Date((long) creationTime * 1000);
    }

    public Date getAccessDate() {
        return new Date((long) accessTime * 1000);
    }

    public Date getModifiedDate() {
        return new Date((long) modifiedTime * 1000);
    }

    public Date getDeletionDate() {

        Date del = new Date((long) deleteTime * 1000);

        return del;
    }

    public short getNumebrOfLinks() {
        return this.numberOfLinks;
    }

    /**
     * This method takes in the path and gets the exact offset (address) of the inode.
     * @param segment The cleaned string of the path.
     * @param inode The current inode.
     * @param file The ext2 file of the filesystem.
     * @return Returns the address (offset) of the inode we're working with.
     */
    public static int getInodeAdress(String segment, Inode inode, Ext2File file) {

        int[] pointers = inode.getBlockPointervalue();
        int nullData = 0;

        // Directories or files are always pointed at by the 12 direct inode pointers
        int i = 0;
        while (i < 12) {

            // exit if pointer value is equal to zero
            if (pointers[i] != 0) {
                // Reading block data into a byteBuffer
                byte[] data = file.readByteBlock(pointers[i] * BLOCK_SIZE, BLOCK_SIZE);
                ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                short length;

                for (int x = 0; x < byteBuffer.limit(); x += length) {

                    // Iterating one byte at a time ; 4 = byte length
                    length = byteBuffer.getShort(x + 4);

                    // 6 = 4 byte length + 2 short length
                    byte[] charBytes = new byte[byteBuffer.get(x + 6)];
                    byte[] stringBytes = charBytes;

                    // get each name byte
                    for (int y = 0; y < stringBytes.length; y++) {
                        // 8 = length of bytes 4 times 2
                        stringBytes[y] = byteBuffer.get(x + y + 8);
                    }

                    String newPath = new String(stringBytes).trim();
                    if (segment.equals(newPath)) {
                        return byteBuffer.getInt(x);
                    }
                }
            }
            i++;
        }
        return nullData;
    }

    // Inode Print all data method

    /**
     * This method prints out all the inode information extracted by
     * the extractInodeInfo( ) method.
     */
    public void printAllInodeData() {

        System.out.println("\n==========================================");
        System.out.println("=                 Inode data             =");
        System.out.println("==========================================\n");

        System.out.println("Inode is directory : " + isDirectory());
        System.out.println("Inode is file : " + isFile());
        System.out.println("Inode User ID : " + getUSerID());
        System.out.println("Inode File Size Lower : " + getFileSizeLower());
        System.out.println("Inode File Size Upper : " + getFileSizeUpper());
        System.out.println("Inode Last access time : " + getAccessDate());
        System.out.println("Inode Creation time : " + getCreationDate());
        System.out.println("Inode Last modified time : " + getModifiedDate());
        if (this.getDeletionDate().equals(new Date(0))) {
            System.out.println("Inode Deletion time : Never Deleted");
        } else {
            System.out.println("Inode Deletion time : " + getDeletionDate());
        }
        System.out.println("Inode Group ID Owner : " + getGroupID());
        System.out.println("Inode Number of Links : " + getNumebrOfLinks());

        System.out.println("=========================================================\n");

    }

    /**
     * This method checks the inode and extracts the file permissions.
     * @return The string of the permissions for this inode in codes. (drw--x-)
     */
    private String getPermissionCodes() {

        StringBuilder codes = new StringBuilder("");

        if (((int) this.fileMode & 0xC000) == 0xC000)
            codes.append("socket");
        else if (((int) this.fileMode & 0xA000) == 0xA000)
            codes.append("symbolic link");
        else if (((int) this.fileMode & 0x8000) == 0x8000)
            codes.append("-");
        else if (((int) this.fileMode & 0x6000) == 0x6000)
            codes.append("block device");
        else if (((int) this.fileMode & 0x4000) == 0x4000)
            codes.append("d");
        else if (((int) this.fileMode & 0x2000) == 0x2000)
            codes.append("c");
        else if (((int) this.fileMode & 0x1000) == 0x1000)
            codes.append("fifo");

        //////////////////////////////////////////////

        if (((int) this.fileMode & 0x0100) == 0x0100) {
            codes.append("r");
        } else {
            codes.append("-");
        }

        if (((int) this.fileMode & 0x0080) == 0x0080) {
            codes.append("w");
        } else {
            codes.append("-");
        }

        if (((int) this.fileMode & 0x0040) == 0x0040) {
            codes.append("x");
        } else {
            codes.append("-");
        }

        //////////////////////////////////////////////

        if (((int) this.fileMode & 0x0020) == 0x0020) {
            codes.append("r");
        } else {
            codes.append("-");
        }
        if (((int) this.fileMode & 0x0010) == 0x0010) {
            codes.append("w");
        } else {
            codes.append("-");
        }
        if (((int) this.fileMode & 0x0008) == 0x0008) {
            codes.append("x");
        } else {
            codes.append("-");
        }

        //////////////////////////////////////////////

        if (((int) this.fileMode & 0x0004) == 0x0004) {
            codes.append("r");
        } else {
            codes.append("-");
        }
        if (((int) this.fileMode & 0x0002) == 0x0002) {
            codes.append("w");
        } else {
            codes.append("-");
        }
        if (((int) this.fileMode & 0x0001) == 0x0001) {
            codes.append("x");
        } else {
            codes.append("-");
        }
        if (((int) this.fileMode & 0x0200) == 0x0200) {
            codes.append("t");
        } else {
            codes.append("");
        }

        //////////////////////////////////////////////

        return codes.toString();
    }

    /**
     * This method retrieves the permission codes and returns them.
     * @return The string of the permission codes.
     */
    public String getFilePermissions() {

        String perm = new String("");

        perm = getPermissionCodes();

        return perm;
    }
}