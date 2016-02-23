package ebraendli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by fusions on 21.02.16.
 */
public class TransactionManager {
    public boolean isListening = true;
    private HashMap<String, String> stations = new HashMap<String, String>();

    public void prepare(String sql) {
        Iterator<String> iter = stations.keySet().iterator();

        /**
         * Cleans the last transaction logs
         */
        while (iter.hasNext()) {
            stations.put(iter.next(), "");
        }

        iter = stations.keySet().iterator();

        if (sql.contains("commit;")) {
            sql.replace("commit;", "");
        }
        if (sql.contains("begin;")) {
            sql.replace("begin;", "");
        }

        while (iter.hasNext()) {
            ComIPC.send(iter.next(), ConstraintsAndUtils.COM_PORT, sql);
        }
    }

    public boolean waitAndCheck(long milisecToWait){
        try {
            Thread.sleep(milisecToWait);
            Iterator<String> iter = stations.keySet().iterator();
            int iok= 0;
            while (iter.hasNext()){
                String tmp = iter.next();
                if (stations.get(tmp).equals("ready") || stations.get(tmp).equals("ack"))
                    iok++;
            }
            if (stations.size() == iok)
                return true;
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doFinal() {
        Iterator<String> iter = stations.keySet().iterator();
        if (waitAndCheck(ConstraintsAndUtils.TIME_TO_RESPOND)) {
            System.out.println("Doing commit!");
            while (iter.hasNext()) {
                ComIPC.send(iter.next(), ConstraintsAndUtils.COM_PORT, "commit;");
            }
            return true;
        } else {
            System.out.println("do abort!");
            while (iter.hasNext()) {
                ComIPC.send(iter.next(), ConstraintsAndUtils.COM_PORT, "abort;");
            }
            return false;
        }

    }

    class RxThread extends Thread {
        public void run() {
            while (isListening) try {
                ServerSocket ss = new ServerSocket(ConstraintsAndUtils.COM_PORT);
                Socket rx = ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(rx.getInputStream()));
                String tmp, data = "";
                while ((tmp = br.readLine()) != null) {
                    data += tmp;
                }
                br.close();
                rx.close();
                String[] msg = ConstraintsAndUtils.convertMsg(data);
                stations.put(msg[0], msg[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
