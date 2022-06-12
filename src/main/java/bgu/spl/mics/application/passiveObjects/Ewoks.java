package bgu.spl.mics.application.passiveObjects;


import bgu.spl.mics.MessageBusImpl;

import java.util.Hashtable;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {

    private static Ewoks instance = null;
    private Ewok[] source;

    private Ewoks() {
    }


    private static class SingletonHolder {
        private static Ewoks instance = new Ewoks();
    }
    public static Ewoks getInstance() {
        return Ewoks.SingletonHolder.instance;
    }

    public void createSource(int size) {
        this.source = new Ewok[size];
        for (int i = 0; i< source.length; i++){
            source[i] = new Ewok(i+1);
        }
    }
    //for methods below, we use sorted array to get for each Ewok by his serial number
    // number starts from 1 and not 0 so we need to get serial-1
    public void acquireEwok(int serial) {
        this.source[serial-1].acquire();

    }
    public void releaseEwok(int serial){
        source[serial-1].release();
    }
    public boolean isAvailable(int serial){
        return this.source[serial-1].getAvailable();
    }

    public Ewok[] getSource() {
        return source;
    }
}
