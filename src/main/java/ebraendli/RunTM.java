package ebraendli;

/**
 * Created by fusions on 25.02.16.
 */
public class RunTM {
    public static void main(String ... args){
        for (int i = 0; i < args.length; i++) {
            System.out.println(i+".Station\t"+args[i]);
        }
        TransactionManager tm = new TransactionManager(args);
    }
}