package ebraendli;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by fusions on 21.02.16.
 */
public class TransactionManager {
    private HashMap<String,String> stations = new HashMap<String,String>();


    public TransactionManager(){

    }

    public boolean prepare(String sql){
        Iterator<String> iter = stations.keySet().iterator();

        /**
         * Cleans the last transaction logs
         */
        while (iter.hasNext()){
            stations.put(iter.next(),"");
        }

        iter = stations.keySet().iterator();

        while (iter.hasNext()) {
            ComIPC.send()
        }
    }

}
