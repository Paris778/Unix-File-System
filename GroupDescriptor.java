package dataStructures;

import java.util.*;
import java.io.*;
import java.nio.*;

import coreFileSystemFunctionality.*;
//import dataStructures.*;
//import misc.*;

/**
 * This is the GroupDescriptor
 * It extracts the Inode table pointer value from the group descriptor
 * of each block in the filesystem.
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */

public class GroupDescriptor {

    //Some constants
    static final int GROUP_DESCRIPTOR_SIZE = 32;
    static final int INODE_TABLE_OFFSET = 8;

    private int inodeValue = 0;
    private int adress = 0;
    private int id = 0;
    private int inodeTableAdress = 0;
    private ByteBuffer byteBuffer;

    /**
     * Constructor.
     * @param b An array of bytes which belong to the group descriptor of a block in the filesystem.
     * @param adr The address of this group descriptor (integer).
     * @param id The ID of the group block which this group descriptor belongs to.
     */
    public GroupDescriptor(byte[] b, int adr,int id) {

        this.id = id;
        this.adress = adr;

        try {
            byteBuffer = ByteBuffer.wrap(b);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * This method extracts the inode table pointer value from the group descriptor.
     */
    public void readGroupDescriptor(int i){
        this.inodeValue = byteBuffer.getInt(8);
        //this.inodeTableAdress = this.getInodeTableValue() * 1024;
    }

    /**
     * Accesor Method.
     * This method retrieves the value of the inode table pointer
     * @return The value of the inode table pointer.
     */
    public int getInodeTablePointer() {


        return this.inodeValue;
    }

    /**
     * Accesor Method.
     * This method retrieves the inode table address (integer).
     * @return The inode table address.
     */
    public int getInodeTableAddress() {

        return this.inodeTableAdress;
    }

    /**
     * Accesor Method.
     * This method retrieves address of this group descriptor.
     * @return The address of this group descriptor.
     */
    public int getAdress() {

        return this.adress;

    }

    /**
     * Accesor Method.
     * This method retrieves the number of the block this group descriptor belongs to.
     * @return The number of the block this group descriptor belongs to.
     */
    public int getID(){
        return this.id;
    }


    /**
     * This method prints all data extracted from the group descriptor onto the terminal.
     */
    public void printAllData(){

        System.out.println("========================");
        System.out.println("=Group Descriptor Data =");
        System.out.println("========================\n");

        System.out.println("Group descriptor ID: " + getID());
        System.out.println("Group descriptor address: " + getAdress());
        System.out.println("Inode Table value: " + getInodeTablePointer() + "\n" );
        System.out.println("\n========================\n");
        //System.out.println("Inode Table value: " + getInodeTableAddress() + "\n");
    }
}