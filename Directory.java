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

public class Directory {

   //Constant
   static final int BLOCK_SIZE = 1024;

   /**
    * Accesor methods.
    * This method determines whether the current inode is a file or directory and calls the
    * getBlockData() method.
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    * @param url The path of the file.
    */
   public static void getFileData(Inode inode, Superblock sb, Ext2File file, GroupDescriptor[] gd, String url) {

      if (inode.isFile() && !inode.isDirectory()) {
         getBlockData(inode, sb, file, gd);
      }
      if (!inode.isFile() && inode.isDirectory()) {
         System.out.println("This item is a Directory , not a file");
      }

   }

    /**
    * Accesor method.
    * This method determines whether the current inode is a file or directory and calls the
    * getBlockData() method.
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    */
   public static void getDirectoryData(Inode inode, Superblock sb, Ext2File file, GroupDescriptor[] gd) {

      if (!inode.isFile() && inode.isDirectory()) {
         getBlockData(inode, sb, file, gd);
      }
      if (inode.isFile() && !inode.isDirectory()) {
         System.out.println("This item is a File , not a directory");
      }
   }

    /**
    * Accesor method.
    * This method determines whether the current inode is a file or directory and calls the
    * getBlockData() method.
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    */
   public static void getBlockData(Inode inode, Superblock sb, Ext2File file, GroupDescriptor[] gd) {
      // isFile() from inode

      int[] dataBlockPointer = inode.getBlockPointervalue();

      // YOU WERE HERE MANA MOU

      for (int i = 0; i < 12; i++) {
         if (dataBlockPointer[i] != 0) {
            printAllBlockData(dataBlockPointer[i], inode, sb, gd, file);
         }
      }
      // Check for single indirect data
      if (dataBlockPointer[12] != 0) {
         getSingleIndirectData(dataBlockPointer[12], sb, file, inode, gd);

      }
      // Check for double indirect data
      if (dataBlockPointer[13] != 0) {
         getDoubleIndirectData(dataBlockPointer[13], sb, file, inode, gd);
      }
      // Check for triple indirect data
      if (dataBlockPointer[14] != 0) {
         getTripleIndirectData(dataBlockPointer[14], sb, file, inode, gd);
      }

   }

   /// GET SINGLE INDIRECT DATA
    /**
    * Accesor method.
    * This method extracts the singly indirect data of an inode.
    * @param address The address of the inode we're working with (byte offset).
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    */
   public static void getSingleIndirectData(int address, Superblock sb, Ext2File file, Inode inode,
         GroupDescriptor[] gd) {

      byte[] bytes = file.readByteBlock(address * BLOCK_SIZE, BLOCK_SIZE);
      ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < byteBuffer.limit(); i += 4) {
         if (byteBuffer.getInt(i) != 0) {
            printAllBlockData(byteBuffer.getInt(i), inode, sb, gd, file);
         }
      }

   }

   /// GET DOUBLE INDIRECT DATA
    /**
    * Accesor method.
    * This method extracts the double indirect data of an inode.
    * @param address The address of the inode we're working with (byte offset).
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    */
   public static void getDoubleIndirectData(int address, Superblock sb, Ext2File file, Inode inode,
         GroupDescriptor[] gd) {

      byte[] bytes = file.readByteBlock(address * BLOCK_SIZE, BLOCK_SIZE);
      ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < byteBuffer.limit(); i += 4) {
         if (byteBuffer.getInt(i) != 0) {
            getSingleIndirectData(byteBuffer.getInt(i), sb, file, inode, gd);
         }
      }

   }

   /// GET TRIPLE INDIRECT DATA
    /**
    * Accesor method.
    * This method extracts the triple indirect data of an inode.
    * @param address The address of the inode we're working with (byte offset).
    * @param inode The current inode.
    * @param sb The superblock.
    * @param file The ext2 file of the filesystem.
    * @param gd The array of Gropu descriptors.
    */
   public static void getTripleIndirectData(int address, Superblock sb, Ext2File file, Inode inode,
         GroupDescriptor[] gd) {

      byte[] bytes = file.readByteBlock(address * BLOCK_SIZE, BLOCK_SIZE);
      ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < byteBuffer.limit(); i += 4) {
         if (byteBuffer.getInt(i) != 0) {
            getDoubleIndirectData(byteBuffer.getInt(i), sb, file, inode, gd);
         }
      }

   }

   /**
    * This method prints all Block data onto the console.
    * @param blockNumber The number of the block.
    * @param inode The current inode.
    * @param sb The superblock.
    * @param gd The array of Group descriptors.
    * @param file The ext2 file we're working with.
    */
   public static void printAllBlockData(int blockNumber, Inode inode, Superblock sb, GroupDescriptor[] gd,
         Ext2File file) {

      // Get all block data (multipy by 1024 to go to the correct offset address in
      // the block)

      byte[] allBlockData = file.readByteBlock(blockNumber * BLOCK_SIZE, BLOCK_SIZE);

      // If file path is a FILE
      if (inode.isFile() && !inode.isDirectory()) {
         System.out.println(
            "\n==========================================================================================================");
      System.out.println(
            "=                                        Printing file contents                                          =");
      System.out.println(
            "==========================================================================================================\n");
         // trim method to get rid of spaces:
         // https://www.geeksforgeeks.org/java-string-trim-method-example/
         String string = new String(allBlockData).trim();
         System.out.println(string);
      }

      // If file path is a directory
      if (inode.isDirectory() && !inode.isFile()) {

         System.out.println(
            "\n==========================================================================================================");
      System.out.println(
            "=                                        Printing directory  data                                        =");
      System.out.println(
            "==========================================================================================================\n");

         System.out.println(
               "Permissions - Links  - UserID - GroupID -Size          -       Creation Date     -   Directory Name");
         System.out.println(
               "__________________________________________________________________________________________________________");

         short lengthOfDirectory = 0;
         ByteBuffer byteBuffer;
         byteBuffer = ByteBuffer.wrap(allBlockData);
         byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

         for (int i = 0; i < byteBuffer.limit(); i += lengthOfDirectory) {

            int offset = byteBuffer.getInt(i);
            // index is 4 bytes long
            lengthOfDirectory = byteBuffer.getShort(4 + i);
            // 4 = length of bytes + 2 = length of shorts = 6
            byte names = byteBuffer.get(i + 6);
            byte[] characters = new byte[names];

            for (int y = 0; y < characters.length; y++) {
               // 8 = lenght of bytes * 2
               characters[y] = byteBuffer.get(y + i + 8);
            }

            int blockAdress = Inode.getInodeBlock(offset, sb, gd);

            //System.out.println(blockAdress);

            byte[] miscData = file.readByteBlock(blockAdress, sb.getSizeOfInodes());

            Inode inodeData = new Inode(miscData);
            long correctSize = ((long) inodeData.getFileSizeUpper() << 32)
                  | ((long) inodeData.getFileSizeLower() & 0xFFFFFFFFL);

            // Print inode data

            System.out.println("\n");
            System.out.format("%-12s\t", inodeData.getFilePermissions());
            System.out.format("%-4d\t", inodeData.getNumebrOfLinks());
            System.out.format("%-7s\t", inodeData.getUSerID());
            System.out.format("%-7s\t", inodeData.getGroupID());
            System.out.format("%-12d\t", correctSize);
            System.out.format("%-30s\t", inodeData.getCreationDate());
            System.out.format("%-30s\t", new String(characters).trim());
         }
      }
      System.out.println(
            "\n\n==========================================================================================================\n");
   }
}