
package utils;

public class console {

    public static void log(Object... args) {
        for (Object arg : args) {
            System.out.print(arg + " ");
        }
        System.out.print("\n");
    }

}