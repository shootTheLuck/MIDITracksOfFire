package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {

    //adapted from https://mkyong.com/java/how-to-generate-a-file-checksum-value-in-java/
    public static String generate(String filepath) {

        StringBuilder result = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md);
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            dis.close();
        } catch (Exception ex) {
            console.error("An error occurred trying to generated checksum", ex);
        }

        return result.toString();
    }
}