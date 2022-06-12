package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminationBrodcast implements Broadcast{
    private long terminationTime;

    public TerminationBrodcast(long terminationTime){
        this.terminationTime = terminationTime;
    }

    public long getTerminationTime() {
        return terminationTime;
    }
}
