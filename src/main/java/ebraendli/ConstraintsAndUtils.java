package ebraendli;

/**
 * Created by fusions on 21.02.16.
 */
public class ConstraintsAndUtils {
    public static final int COM_PORT = 3838;
    public static final String PREPARE_MSG = "PREPARE";
    public  static final long TIME_TO_RESPOND = 4000L;
    public static final String GLOBAL_HALT = "g_____-halt";

    public static String[] convertMsg(String rawmsg){
        //System.err.println(rawmsg);
        String[] spdata = new String[3];
        spdata[0] = rawmsg.split(" ")[0];
        spdata[1] = rawmsg.split("§")[0].split(" ")[1] + rawmsg.split("§")[0].split(" ")[2];
        spdata[2] = "";
        for (int i = 1; i <= rawmsg.split("$").length; i++){
            spdata[2] += rawmsg.split("§")[i];
        }
        return spdata;
    }
}
