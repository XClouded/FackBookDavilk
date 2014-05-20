package com.example.testdavilk.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

public class DexSign
{

    public void main(String args)
    {

        try
        {
            File file = new File(args);

            byte[] barr = null;
            barr = getBytesFromFile(file);

            System.out.print("Original Checksum: ");
            for (int i = 8; i < 12; i += 4)
                System.out.printf("0x%02X%02X%02X%02X ", barr[i + 3], barr[i + 2], barr[i + 1], barr[i]);

            System.out.print("\nOriginal Signature: 0x");
            for (int i = 12; i < 32; i += 4)
                System.out.printf("%02X%02X%02X%02X ", barr[i], barr[i + 1], barr[i + 2], barr[i + 3]);

            calcSignature(barr);
            calcChecksum(barr);

            System.out.print("\n\nNew Checksum: ");
            for (int i = 8; i < 12; i += 4)
                System.out.printf("0x%02X%02X%02X%02X ", barr[i + 3], barr[i + 2], barr[i + 1], barr[i]);

            System.out.print("\nNew Signature: 0x");
            for (int i = 12; i < 32; i += 4)
                System.out.printf("%02X%02X%02X%02X ", barr[i], barr[i + 1], barr[i + 2], barr[i + 3]);

            try
            {
                String str = readUserInput("\nSave it(Yes or No)£º");
                if (str.equalsIgnoreCase("yes"))
                {
                    putBytesToFile(barr, args);
                    System.err.println("\nFixed.");
                    System.err.println(args);
                }
                else
                {
                    System.err.println("\nNothing");
                }
            }
            catch (IOException except)
            {
                except.printStackTrace();
            }

        }
        catch (Exception e)
        {
            System.err.println("File input error");
        }
    }

    private static String readUserInput(String prompt) throws IOException
    {
        System.out.print(prompt);
        InputStreamReader is_reader = new InputStreamReader(System.in);
        return new BufferedReader(is_reader).readLine();
    }

    @SuppressWarnings("resource")
	public static byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE)
        {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
        {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static void putBytesToFile(byte[] data, String outfile) throws IOException
    {
        File destinationFile = new File(outfile);

        if (destinationFile.exists())
        {
            System.out.println("overwrite");
        }

        FileOutputStream fos = new FileOutputStream(destinationFile);

        try
        {
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

    }

    private static void calcSignature(byte bytes[])
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException(ex);
        }

        md.update(bytes, 32, bytes.length - 32);
        try
        {
            int amt = md.digest(bytes, 12, 20);
            if (amt != 20)
                throw new RuntimeException((new StringBuilder()).append("unexpected digest write:").append(amt).append("bytes").toString());
        }
        catch (DigestException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static void calcChecksum(byte bytes[])
    {
        Adler32 a32 = new Adler32();
        a32.update(bytes, 12, bytes.length - 12);
        int sum = (int) a32.getValue();
        bytes[8] = (byte) sum;
        bytes[9] = (byte) (sum >> 8);
        bytes[10] = (byte) (sum >> 16);
        bytes[11] = (byte) (sum >> 24);
    }
}
