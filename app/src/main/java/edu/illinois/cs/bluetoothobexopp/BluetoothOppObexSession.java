package edu.illinois.cs.bluetoothobexopp;

/**
 * Created by shu17 on 9/21/14.
 */
import android.os.Handler;

/**
 * Interface for control the state of an OBEX Session.
 */
public interface BluetoothOppObexSession {

    /**
     * Message to notify when a transfer is completed For outbound share, it
     * means one file has been sent. For inbounds share, it means one file has
     * been received.
     */
    int MSG_SHARE_COMPLETE = 0;

    /**
     * Message to notify when a session is completed For outbound share, it
     * should be a consequence of session.stop() For inbounds share, it should
     * be a consequence of remote disconnect
     */
    int MSG_SESSION_COMPLETE = 1;

    /** Message to notify when a BluetoothOppObexSession get any error condition */
    int MSG_SESSION_ERROR = 2;

    /**
     * Message to notify when a BluetoothOppObexSession is interrupted when
     * waiting for remote
     */
    int MSG_SHARE_INTERRUPTED = 3;

    int MSG_CONNECT_TIMEOUT = 4;

    int SESSION_TIMEOUT = 50000;

    void start(Handler sessionHandler, int numShares);

    void stop();
    void addShare(BluetoothOppShareInfo share);
    void unblock();

}