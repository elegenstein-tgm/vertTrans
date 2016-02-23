package ebraendli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
    private Statement stmt;
    private Connection con;
    private PrintWriter logger;


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
        try {
            logger = new PrintWriter(new FileWriter(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseInput(String rxMsg){
        if (rxMsg.equals("abort;")){
            logger.write(String.format("%td-%tm-%ty Doing Rollback...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            doRollback();
            return;
        }
        if (rxMsg.equals("commit;")){
            logger.write(String.format("%td-%tm-%ty Doing Commit...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            doCommit();
            return;
        }
        logger.write(String.format("%td-%tm-%ty Got new Transaction...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        doSql(rxMsg);
    }

    private void doRollback(){
        try {
            con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
        }
        this.hasCurrentSql = false;
    }

    private void doCommit(){
        try {
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
        }
        this.hasCurrentSql = false;
    }

    private void callFailed(){
        logger.write(String.format("%td-%tm-%ty Calling Failed...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        ComIPC.send(ipTransMan,ConstraintsAndUtils.COM_PORT,"failed;");
    }

    private void callReady(){
        logger.write(String.format("%td-%tm-%ty Calling Ready...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        ComIPC.send(ipTransMan,ConstraintsAndUtils.COM_PORT,"ready;");
    }

    private void doSql(String sql) {
        if (this.hasCurrentSql) {
            callFailed();
            return;
        }
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.execute(sql);
            logger.print(String.format("%td-%tm-%ty Waiting for Commit...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        } catch (SQLException e) {
            e.printStackTrace();
            //todo logging
        }
        this.hasCurrentSql = true;
        callReady();
    }

    class RxThread extends Thread{
        public void run(){
            while (isListening){
                // todo implement
            }
        }
    }
}
