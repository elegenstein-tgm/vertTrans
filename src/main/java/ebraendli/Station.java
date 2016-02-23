package ebraendli;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by fusions on 21.02.16.
 */
public class Station {
    private String ipTransMan;
    protected String logFile;
    private boolean isReady= true;
    private boolean isListening = true;
    private boolean hasCurrentSql = false;


    public Station(String IpOfTransManager){
        this.ipTransMan = IpOfTransManager;
        Calendar c = Calendar.getInstance();
        File f = new File(String.format("log-%td-%tm-%ty.log",c,c,c));
        if(!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        logFile = f.getName();
    }

    public void parseInput(String rxMsg){
        if (rxMsg.equals("abort;")){
            doRollback();
            return;
        }
        if (rxMsg.equals("commit;")){
            doCommit();
            return;
        }
        doSql(rxMsg);
    }

    private void doRollback(){

        this.hasCurrentSql = false;
    }

    private void doCommit(){

        this.hasCurrentSql = false;
    }

    private void callFailed(){

    }

    private void callReady(){

    }

    private void doSql(String sql){
        if (this.hasCurrentSql) {
            callFailed();
            return;
        }
        //todo implement
        this.hasCurrentSql = true;
        callReady();
    }

    class RxThread extends Thread{
        public void run(){
            while (isListening){

            }
        }
    }
}
