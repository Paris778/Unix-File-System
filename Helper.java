package misc;

import java.lang.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import java.lang.Object;
import java.nio.charset.StandardCharsets;

import coreFileSystemFunctionality.*;
//import dataStructures.*;
//import misc.*;

/**
 * This is the Helper class.
 * It provides some useful static methods to aid other functions in the program.
 * @author Paraskevas Solomou (34838805)
 * @version Final as of 01/12/2019
 */


public class Helper {

    static final int CONSTANT_OFFSET = 26;

    /**
     * This method gets rid of the forward slashes in a given string (path)
     * @param str The path which the user wants to follow.
     * @return The cleaned String.
     */
    public static String [] cleanPath(String str){
        String [] clean = str.split("/");
        return clean;
    }

    /**
     * This method gets a two byte integer and turns it into a string of hex values.
     * @param sh A short value to be turned into hex.
     * @return A string of hex values.
     */
    public static String makeHex(Short sh) {
        String changed = Integer.toHexString(sh - 0xffff0000);
        String corrected = "0x" + changed;
        return corrected;
    }

    

    /**
     * This method prints out hex bytes and their ASCII equivalents on the side for debugging.
     * @param hex An array of bytes which will be printed out as hex.
     */
    public static void dumpHexBytes(byte[] hex) {

        StringBuilder hexString = new StringBuilder("");

        int index = 0;
        System.out.println("\n============================================");
        System.out.println("=              DUMPING HEX BYTES           =");
        System.out.println("============================================\n");

        int counter = 0;

        // for (int i = 0 ; i < hex.length ; i++) {
        for (byte piece : hex) {
            if (index != 0 && index % CONSTANT_OFFSET != 0)
                System.out.printf("\n");

            if (piece != 0x00) {
                System.out.printf(String.format("%02X ", piece));
                hexString.append(String.format("%02X", piece));
                index++;
                counter++;
                if (counter == 8) {
                    System.out.printf(" | ");
                }
                if (counter == 16) {
                    System.out.printf(" | ");

                    System.out.printf(" \n");
                    counter = 0;
                    hexString = new StringBuilder("");
                }

            }
            while (index % CONSTANT_OFFSET != 0) {
                System.out.print("00 ");
                hexString.append("00");
                index++;
                counter++;
                if (counter == 8) {
                    System.out.printf(" | ");
                }
                if (counter == 16) {
                    System.out.printf(" | ");

                    byte[] hexBytes = DatatypeConverter.parseHexBinary(hexString.toString().trim());
                    System.out.print(new String(hexBytes));

                    hexString = new StringBuilder("");
                    System.out.printf(" \n");
                    counter = 0;
                }

            }

        }

        while (counter != 16) {
            System.out.print("XX ");
            counter++;
            if (counter == 8) {
                System.out.printf(" | ");
            }
            if (counter == 16) {
                System.out.printf(" | ");
                System.out.printf(" \n");
            }
        }

        System.out.println("\n============================================\n");

    }
}