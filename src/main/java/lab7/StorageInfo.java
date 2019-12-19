package lab7;

import org.zeromq.ZFrame;

public class StorageInfo {
    private 
    private ZFrame address;
    private int start;
    private int end;
    private long heartbeatTime;

    public StorageInfo(ZFrame address, int start, int end, long heartbeatTime) {
        this.address = address;
        this.start = start;
        this.end = end;
        this.heartbeatTime = heartbeatTime;
    }
}
