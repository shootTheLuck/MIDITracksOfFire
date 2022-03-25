
package utils;

public class console {

    //String className = s.getClass().getSimpleName();

    public static void log(Object s) {
        System.out.println(s);
    }

    public static void log(Object s, Object s2) {
        System.out.println(s + " " + s2);
    }

    public static void log(Object s, Object s2, Object s3) {
        System.out.println(s + " " + s2 + " " + s3);
    }

    public static void log(Object s, Object s2, Object s3, Object s4) {
        System.out.println(s + " " + s2 + " " + s3 + " " + s4);
    }

    public static void log(Object s, Object s2, Object s3, Object s4, Object s5) {
        System.out.println(s + " " + s2 + " " + s3 + " " + s4 + " " + s5);
    }

    public static void log(Object s, Object s2, Object s3, Object s4, Object s5, Object s6) {
        System.out.println(s + " " + s2 + " " + s3 + " " + s4 + " " + s5 + " " + s6);
    }

}