package im.conversations.compliance.utils;

public class Utils {
    public static boolean nullableStringEqual(String a, String b) {
        //If both are null, return true
        if(a == b) {
            return true;
        }
        //If one of them is null and other not null, return false
        else if(a == null || b == null) {
            return false;
        }
        //If both are not null, check equality
        if(a.equals(b)) {
            return true;
        }
        return false;
    }
}
