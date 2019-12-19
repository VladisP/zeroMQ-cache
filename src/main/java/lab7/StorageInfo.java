package lab7;

import org.zeromq.ZFrame;

public class StorageInfo {
    //    private String id;
    private ZFrame address;
    private int start;
    private int end;
    private long heartbeatTime;

    public StorageInfo(/*String id, */ZFrame address, int start, int end, long heartbeatTime) {
        //this.id = id;
        this.address = address;
        this.start = start;
        this.end = end;
        this.heartbeatTime = heartbeatTime;
    }

    public void setHeartbeatTime(long heartbeatTime) {
        this.heartbeatTime = heartbeatTime;
    }

    public boolean isDead() {
        
    }
}
