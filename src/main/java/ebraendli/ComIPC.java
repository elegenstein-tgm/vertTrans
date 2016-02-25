package ebraendli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by fusions on 21.02.16.
 */
public class ComIPC {



    public static void main(String ... args){
        Calendar c = Calendar.getInstance();
        System.out.format("%td/%tm/%ty", c,c,c);
    }

    public static boolean send(String toIP, int toPort, String msg){
        Calendar c = Calendar.getInstance();
        try {
            Socket ct = new Socket(toIP, toPort);
            PrintWriter printWriter = new PrintWriter(ct.getOutputStream());
            printWriter.format("%s %td/%tm/%ty %tl:%tM%tp§%s", ct.getLocalAddress().toString().replace("/","").split(":")[0], c, c, c, c, c, c, msg);
            printWriter.flush();
            printWriter.close();
            ct.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] read(Socket rx){
        String[] ret = new String[3];
        String ip, msg, timestamp;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(rx.getInputStream()));
            String tmp, fullrx="";
            while ((tmp = br.readLine()) != null){
                fullrx += tmp;
            }
            br.close();
            rx.close();
            ip = fullrx.split(" ")[0];
            timestamp = fullrx.split(" ")[1]+fullrx.split(" ")[2].split("§")[0];
            msg = fullrx.substring(fullrx.indexOf('§')+1);
            ret[0] = ip;
            ret[1] = timestamp;
            ret[2] = msg;
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
