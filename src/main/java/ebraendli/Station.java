package ebraendli;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
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
    private ServerSocket serverSocket;


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
        initDBCon();
        logger.print("Setting up Socket ... ");
        try {
            serverSocket = new ServerSocket(ConstraintsAndUtils.COM_PORT);
            //serverSocket = new ServerSocket(7000);
            //new Socket(IpOfTransManager,ConstraintsAndUtils.COM_PORT).close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
        }
        new RxThread().start();

    }

    public void stop(){
        isListening = false;
    }

    private void initDBCon(){
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql:dezlab06","dezlab06","dezlab06");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            callFailed();
        }
    }

    public void parseInput(String rxMsg){
        if (rxMsg.equals(ConstraintsAndUtils.GLOBAL_HALT)){
            System.out.println(String.format("%td-%tm-%ty Halting...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            logger.write(String.format("%td-%tm-%ty Doing Halt...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            doHalt();
            return;
        }
        if (rxMsg.equals("abort;")){
            System.out.println(String.format("%td-%tm-%ty Doing Rollback...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            logger.write(String.format("%td-%tm-%ty Doing Rollback...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
            doRollback();
            return;
        }
        if (rxMsg.equals("commit;")){
            System.out.println(String.format("%td-%tm-%ty Doing Commit...", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
            logger.write(String.format("%td-%tm-%ty Doing Commit...", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
            doCommit();

            return;
        }
        System.out.println(String.format("%td-%tm-%ty Got new Transaction...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        logger.write(String.format("%td-%tm-%ty Got new Transaction...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        doSql(rxMsg);
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
            logger.print(String.format("%td-%tm-%ty Waiting for Commit...", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
        } catch (SQLException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
            callFailed();
            return;
        }
        this.hasCurrentSql = true;
        callReady();
    }

    private void doHalt(){
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        isListening =false;
        logger.close();
    }

    private void doRollback(){
        try {
            con.rollback();
            callAck();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
            callFailed();

        }
        this.hasCurrentSql = false;
    }

    private void doCommit(){
        try {
            con.commit();
            callAck();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.print(e.getMessage());
            callFailed();

        }
        this.hasCurrentSql = false;
    }

    private void callFailed(){
        logger.write(String.format("%td-%tm-%ty Calling Failed...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        System.err.println(String.format("%td-%tm-%ty Calling Failed...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        ComIPC.send(ipTransMan,ConstraintsAndUtils.COM_PORT,"failed;");
    }

    private void callReady(){
        logger.write(String.format("%td-%tm-%ty Calling Ready...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        System.err.println(String.format("%td-%tm-%ty Calling Ready...", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
        ComIPC.send(ipTransMan, ConstraintsAndUtils.COM_PORT, "ready;");
    }
    private void callAck(){
        logger.write(String.format("%td-%tm-%ty Calling Ack...",Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()));
        System.err.println(String.format("%td-%tm-%ty Calling Ack...", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
        ComIPC.send(ipTransMan, ConstraintsAndUtils.COM_PORT, "ack;");
    }

    class RxThread extends Thread{
        public void run(){
            while (isListening){
                try {
                    Socket sock = serverSocket.accept();
                    //System.out.println(sock.getRemoteSocketAddress().toString());
                    BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String rx="", tmp;
                    while ((tmp = br.readLine()) != null){
                        rx += tmp;
                    }
                    br.close();
                    sock.close();
                    parseInput(ConstraintsAndUtils.convertMsg(rx)[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
