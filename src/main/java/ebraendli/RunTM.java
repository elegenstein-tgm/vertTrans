package ebraendli;

import javax.swing.*;

/**
 * Created by fusions on 25.02.16.
 */
public class RunTM {
    public static void main(String ... args){
        for (int i = 0; i < args.length; i++) {
            System.out.println(i+".Station\t"+args[i]);
        }
        TransactionManager tm = new TransactionManager(args);
        tm.prepare("create table \"t1\" ( id integer );");
        tm.doFinal();
        String tmp_sql;
        while((tmp_sql = JOptionPane.showInputDialog("sql?")) != null){
            tm.prepare(tmp_sql);
            tm.doFinal();
        }
        tm.globalshutdown(true);
        System.exit(0);
    }
}
