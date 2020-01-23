package coreFileSystemFunctionality;

import java.util.*;
import java.io.*;
import java.nio.*;

//import coreFileSystemFunctionality.*;
import dataStructures.*;
import misc.*;



public class Volume{

    private int numberOfInodes = 0;
    private int sizeOfInodes = 0;
    private int currentAdress = 0;
    private RandomAccessFile raf;

    public Volume(String fileName){

        System.out.println("Opening EXT2 File ....");

        try{

        raf = new RandomAccessFile(fileName,"r");

        } catch(Exception e){

            System.out.println("Error: File not found or incorrect file name entered.");
            System.exit(0);

        }
        
    }

    public RandomAccessFile getRAF(){
        return this.raf;
    }

}

   /*
    public int getSizeOfInodes(){
        return sb1.getSizeOfInodes();
    }

    public int getNumberOfInodesPerGroup(){
        return sb1.getNumberOfInodesPerGroup();
    }

    public int getNumberOfBlocksPerGroup(){
        return sb1.getNumberOfBlocksPerGroup();
    }

    public int getNumberOfBlocksInSystem(){
        return sb1.getNumberOfBlocksInSystem();
    }

    public int getTotalNumberOfGroups(){
        return sb1.getTotalNumberOfGroups();
    }

    public int getSizeOfGroups(){
        return sb1.getSizeOfGroups();
    }

    public int getCurrentAdress(){
        return this.currentAdress;
    }

    public void jumpToAdress(int i){
        this.currentAdress = i;
    }

    public GroupDescriptor getGroup(int a){
        return gd[a];
    }
    */
