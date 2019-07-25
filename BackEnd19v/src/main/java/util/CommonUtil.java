package util;

public class CommonUtil {
    public static String getlabel(String name) {
        if(Character.isLetter(name.charAt(0))||Character.isDigit(name.charAt(0))){
            return name.substring(0,1);
        }else {
            return "Spe";
        }

    }

}
