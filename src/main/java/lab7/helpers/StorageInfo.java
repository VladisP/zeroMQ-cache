package lab7.helpers;

import lab7.app.Storage;
import org.zeromq.ZFrame;

public class StorageInfo {
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

    public ZFrame getAddress() {
        return address;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setHeartbeatTime(long heartbeatTime) {
        this.heartbeatTime = heartbeatTime;
    }

    public boolean isDead() {
        return heartbeatTime + 2 * Storage.HEARTBEAT_TIMEOUT < System.currentTimeMillis();
    }
}
