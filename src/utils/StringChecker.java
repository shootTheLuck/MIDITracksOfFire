package utils;


public class StringChecker {

    public static boolean isNullOrEmpty(String string) {

        return string == null ||
            string.isEmpty() ||
            string.trim().isEmpty();

    }

}