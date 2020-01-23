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
 * This is the main driver of the program. It extracts some key information in
 * order to support the rest of the functions of this program. It gives the user
 * the choice to enable a Command line environment (not finished yet)
 * 
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */

public class Main {

    static String currentDirectory;
    public static String pathToFollow;
    static String[] path;
    static String userPath = "/";
    static Inode currentInode;
    static Inode rootInode;
    static Inode useInode;

    // Constants
    static final short MAGIC_NUMBER = (short) 0xef53;
    static final int BLOCK_SIZE = 1024;
    static final String FILE_PATH = new String("ext2fs");
    //
    public static LinkedList<String> filePathMap = new LinkedList<>();

    static public Scanner scan = new Scanner(System.in);
    static String userInput;

    // Enables and disables command line envirnment
    static boolean enableCmdEnvironment = true;

    // MAIN METHOD
    public static void main(String[] args) {

        Volume vol = new Volume(FILE_PATH);
        Ext2File file = new Ext2File(vol);

        byte[] superBlock = file.readByteBlock(BLOCK_SIZE, BLOCK_SIZE);

        Superblock sb1 = new Superblock(superBlock);
        sb1.extractAllData();

        // Get into group descriptors and make an array

        GroupDescriptor[] groupDescriptorArray = new GroupDescriptor[sb1.getNumberOfGroupsInSystem()];
        int currentAdress = 2048;

        // Initialising all group descriptors

        for (int index = 0; index < sb1.getNumberOfGroupsInSystem(); index++) {

            System.out.println("Group Descriptor " + index + " made successfully !!!");
            byte[] gd = file.readByteBlock(currentAdress, BLOCK_SIZE);
            groupDescriptorArray[index] = new GroupDescriptor(gd, currentAdress, index);
            groupDescriptorArray[index].readGroupDescriptor(index);
            currentAdress += 32;
        }

        // Initialise the root path
        filePathMap.add("/");
        currentDirectory = filePathMap.peek();

        // Finding the rooot inode address
        int rootInodeOffset = Inode.getInodeBlock(2, sb1, groupDescriptorArray);

        // Load bytes of root inode
        byte[] rootInodeBytes = file.readByteBlock(rootInodeOffset, sb1.getSizeOfInodes());

        // Make a new root Inode
        rootInode = new Inode(rootInodeBytes);
        useInode = new Inode(rootInodeBytes);

        currentInode = rootInode;

        System.out.println("============================================================\n");
        System.out.println(" Would you like to activate the CMD environemnt ? (yes/no)");
        System.out.println("\n============================================================\n");

        boolean keep = true;
        String choice = scan.nextLine();

        // get user input for the command line environment choice of being on or off.
        while (keep) {
            if (choice.equals("yes")) {
                enableCmdEnvironment = true;
                keep = false;
            } else if (choice.equals("no")) {
                enableCmdEnvironment = false;
                keep = false;
            } else {
                System.out.println("============================================================\n");
                System.out.println(" Sorry ... Invalid answer ...");
                System.out.println(" Would you like to activate the CMD environemnt ? (yes/no)");
                System.out.println("\n============================================================\n");
            }
        }

        //////////////////////////////////////
        // Command Line Environment enabled//
        ////////////////////////////////////

        System.out.println("\n* Hint: Try using the ' help ' command for a list of commands. *");

        if (enableCmdEnvironment && !keep) {
            pathToFollow = "/";
            String[] path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);
            while (enableCmdEnvironment) {

                System.out.println("\nPlease type a command:");
                userInput = scan.nextLine();
                if (userInput.equals("exit")) {
                    System.exit(0);
                }

                if (!userInput.equals("help") && !userInput.equals("info") && !userInput.equals("group")) {
                    System.out.println("Please type a path:");
                    userPath = scan.nextLine();
                    pathToFollow = userPath;
                    if (!pathToFollow.equals("/.") && userInput.equals("cd")) {
                        filePathMap.clear();
                        filePathMap.add("/");
                    }
                }

                if (checkInputValidity(userInput)) {

                    path = Helper.cleanPath(userPath);

                    switch (userInput) {

                    case "cd":
                        try {

                            cd(path);
                            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

                        } catch (Exception e) {
                            System.out.println("We're Sorry. An exception has occured with this command: " + userInput);
                        }
                        break;

                    case "ls":

                        try {

                            ls(currentInode, file, sb1, groupDescriptorArray);
                        } catch (Exception e) {
                            System.out.println("We're Sorry. An exception has occured with this command: " + userInput);
                        }
                        break;

                    case "cat":
                        try {

                            view(currentInode, file, sb1, groupDescriptorArray, userPath);
                        } catch (Exception e) {
                            System.out.println("We're Sorry. An exception has occured with this command: " + userInput);
                        }
                        break;

                    case "info":

                        sb1.printAllSuperblockData();
                        rootInode.printAllInodeData();
                        System.out.println("Root inode offset: " + rootInodeOffset);

                        break;

                    case "group":

                        for(int p = 0 ; p < groupDescriptorArray.length ; p++ ){
                            groupDescriptorArray[p].printAllData();
                        }

                        break;

                    case "help":

                        System.out.println(
                                "===============================================================================\n");
                        System.out.println(
                                "-                   Here's a list of all the available commands               - ");
                        System.out.println(
                                "-------------------------------------------------------------------------------");
                        System.out.println(
                                "\n - cd  path/example    :  Changes directory into the specieifed directory.");
                        System.out.println(" - cd  /.              :  Goes up one directory.");
                        System.out.println(" - cd  /..             :  Goes back to the root directory.");
                        System.out.println(" - cat                 :  Reads files sequentially, writing them to standard output.");
                        System.out.println(" - ls path/example     :  Lists the contents of a directory or file.");
                        System.out.println(" - info                :  Print useful data of the filesystem.");
                        System.out.println(" - group               :  Prints al the group descriptor information.");
                        System.out.println(" - exit                :  Terminates the program.");
                        
                        System.out
                                .println("\n     * Hint:   writing  ' / ' for  the path uses the current directory *");
                        System.out.println(
                                "===============================================================================\n");
                        break;

                    }
                }
                printCurrentDirectory();
            }
        }

        //////////// Command environment OFF
        /////////////////////////////////////
        ////////////////////////////////////

        else {
            // Dumping hex bytes for debugging

            byte[] blockZero = file.readByteBlock(BLOCK_SIZE, 10);
            Helper.dumpHexBytes(blockZero);

            // TWO-CITIES SECTION

            // enter a path name and follow it
            pathToFollow = "/two-cities";
            // Split array into parts before and after of the slash and then clean any empty
            String[] path = Helper.cleanPath(pathToFollow);

            currentInode = Inode.getCurrentInode(path, file, currentInode, sb1, groupDescriptorArray);

            if (currentInode == null) {
                new Error("Oopsy error, couldn't find inode.");
            }

            try {
                cd(path);
                view(currentInode, file, sb1, groupDescriptorArray, pathToFollow);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("Exception with CD command");
            }

            /////////////////////////////////////////////////////////////////////////////////////////
            // Content of a file given a path:
            /////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////

            // Three standard procedures
            pathToFollow = "/deep/down/in/the/filesystem/there/lived/a/file";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                cd(path);
                view(currentInode, file, sb1, groupDescriptorArray, pathToFollow);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("Exception with CD command");
            }

            //////////////////////////////////////////////////////////////
            /// DIRECTORY HANDLING SECTION

            // Three standard procedures
            pathToFollow = "/"; // root inode
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            // ls() command shows the contents of the root directory
            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            /////////////////////////////
            // Content of large directory

            pathToFollow = "/big-dir"; // root inode
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            // ls() command shows the contents of the root directory
            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            ////////////////////////////////////////////////
            // Content of directory containing large files

            pathToFollow = "/files";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            // ls() command shows the contents of the root directory
            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            ///////////////////////////////////
            // ADVANCED FILE HANDLING SECTION//
            /////////////////////////////////

            /////// Small direct files

            pathToFollow = "/files/dir-s";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            /////// LARGE direct files

            pathToFollow = "/files/dir-e";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            //// Displaying the content of the first and last block in files with different
            ////////////////////////////////////////////////////////////////////////////////////////////////////////// levels
            ////////////////////////////////////////////////////////////////////////////////////////////////////////// of
            ////////////////////////////////////////////////////////////////////////////////////////////////////////// indirection...

            /////// Small Indirect files

            pathToFollow = "/files/ind-s";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            /////// Large Indirect files

            pathToFollow = "/files/ind-e";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            ////////////////////////////////////
            /////// SMALL DOUBLE Indirect files

            pathToFollow = "/files/dbl-ind-s";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            /////// LARGE DOUBLE Indirect files

            pathToFollow = "/files/dbl-ind-e";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            ////////////////////////////////////
            /////// SMALL TRIPLE Indirect files

            pathToFollow = "/files/trpl-ind-s";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

            /////// LARGE TRIPLE Indirect files

            pathToFollow = "/files/trpl-ind-e";
            path = Helper.cleanPath(pathToFollow);
            currentInode = Inode.getCurrentInode(path, file, rootInode, sb1, groupDescriptorArray);

            try {
                ls(currentInode, file, sb1, groupDescriptorArray);
                printCurrentDirectory(pathToFollow);
            } catch (Exception e) {
                System.out.println("We're Sorry. An exception has occured" + e.getMessage());
            }

        }
        ///// END of MAIN method
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////
    ///// OTHER METHODS

    /**
     * Method to list the contents of a directory given a path.
     * 
     * @param inode Current inode.
     * @param file  The ext2 file extracting information from.
     * @param sb    The Superblock of gropu 0.
     * @param gd    The Array of group descriptors created.
     * @throws IOException
     */
    public static void ls(Inode inode, Ext2File file, Superblock sb, GroupDescriptor[] gd) throws IOException {

        Directory.getBlockData(inode, sb, file, gd);
    }

    /**
     * Method to view the contents of a file.
     * Implementation of the cat command.
     * 
     * @param inode Current inode.
     * @param file  The ext2 file extracting information from.
     * @param sb    The Superblock of gropu 0.
     * @param gd    The Array of group descriptors created.
     * @param url   The path of the file we want to view.
     * @throws IOException
     */
    public static void view(Inode inode, Ext2File file, Superblock sb, GroupDescriptor[] gd, String url)
            throws IOException {

        Directory.getFileData(inode, sb, file, gd, url);
    }

    /**
     * Method to navigate into given path.
     * 
     * @param path The path the user wants to go into.
     */
    public static void cd(String[] path) {
        if (currentInode.isFile()) {
            System.out.println("This item is not a directory");
        }
        useInode = currentInode;
        getCurrentPath(path);
    }

    /**
     * Method to establish current Directory.
     * 
     * @param path The path we want to follow as a split String array.
     */
    public static void getCurrentPath(String[] path) {

        for (String segment : path) {

            // System.out.println("Path follow Normal START: " + filePathMap);
            if ((segment.equals(".") || segment.equals(".."))) { // && !filePathMap.peek().equals("/")
                // removes the last element entered

                filePathMap.pollLast();
                filePathMap.pollLast();

            } else if (!segment.equals(".") && !segment.equals("..")) {

                if (!filePathMap.peekLast().equals("") || !filePathMap.peekLast().equals(" ")) {
                    filePathMap.add(segment);
                } else if (filePathMap.peek().equals("/") && !filePathMap.peek().equals("")) {
                    filePathMap.add(segment);

                } else {
                    filePathMap.add("/" + segment);

                }
            }
        }

        // System.out.println("Path follow Normal AFTER: " + filePathMap);
        LinkedList<String> pathCopy = new LinkedList<>();
        pathCopy = (LinkedList) filePathMap.clone();

        StringBuilder build = new StringBuilder();

        while (!pathCopy.isEmpty()) {
            try {
                if (pathCopy.peekLast().equals("") || pathCopy.peekLast().equals(" ")
                        || pathCopy.peekLast().equals("/")) {
                    pathCopy.removeLast();
                }
                if (pathCopy.peekLast() != "/")
                    build.insert(0, "/");
                build.insert(0, pathCopy.removeLast());
            } catch (Exception e) {
                // TODO: handle exception
            }

            // System.out.println("File path contents: " + build.toString());

        }

        currentDirectory = build.toString();

    }

    /**
     * Method prints the current directory
     */
    static void printCurrentDirectory() {

        System.out.println("\n==============================================================================");
        System.out.println("| The current directory is:   " + currentDirectory);
        System.out.println("==============================================================================\n");
    }

    /**
     * Method prints the current directory
     * 
     * @param path The path which will be followed.
     */
    static void printCurrentDirectory(String path) {

        System.out.println("\n==============================================================================");
        System.out.println("| The current directory is:   " + path);
        System.out.println("==============================================================================\n");
    }

    /**
     * Method chekcs if the user input is valid.
     * 
     * @param str User input as a String.
     * @return Returns true if the input is valid. False if it's invalid.
     */
    static boolean checkInputValidity(String str) {
        boolean valid = false;

        if (str.equals("cd"))
            valid = true;
        if (str.equals("ls"))
            valid = true;
        if (str.equals("cat"))
            valid = true;
        if (str.equals("exit"))
            valid = true;
        if (str.equals("help"))
            valid = true;
        if (str.equals("info"))
            valid = true;
        if (str.equals("group"))
            valid = true;

        if (!valid)
            System.out.println("This command is not valid. Please try again !");

        return valid;
    }

}