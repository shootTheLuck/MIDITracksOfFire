
package utils;

public class console {

    static String RED = "\033[0;31m";
    static String RESET = "\033[0m";

    public static void log(Object... args) {
        for (Object arg : args) {
            System.out.print(arg + " ");
        }
        System.out.print("\n");
    }

    public static void error(Object... args) {
        //for (Object arg : args) {
            //System.out.print(RED + arg + " ");
        //}
        //System.out.print(RESET + "\n");

        // same as log
        for (Object arg : args) {
            System.out.print(arg + " ");
        }
        System.out.print("\n");
    }

}