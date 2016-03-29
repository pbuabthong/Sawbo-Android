package edu.illinois.cs.bluetoothobexopp;

/**
 * Created by shu17 on 9/21/14.
 */


import android.net.Uri;

/**
 * This class stores information about a single OBEX share, e.g. one object
 * send/receive to a destination address.
 */
public class BluetoothOppShareInfo {

    public int mId;

    public Uri mUri;

    public String mHint;

    public String mFilename;

    public String mMimetype;

    public int mDirection;

    public String mDestination;

    public int mVisibility;

    public int mConfirm;

    public int mStatus;

    public int mTotalBytes;

    public int mCurrentBytes;

    public long mTimestamp;

    public boolean mMediaScanned;

    public BluetoothOppShareInfo(int id, Uri uri, String hint, String filename, String mimetype,
                                 int direction, String destination, int visibility, int confirm, int status,
                                 int totalBytes, int currentBytes, int timestamp, boolean mediaScanned) {
        mId = id;
        mUri = uri;
        mHint = hint;
        mFilename = filename;
        mMimetype = mimetype;
        mDirection = direction;
        mDestination = destination;
        mVisibility = visibility;
        mConfirm = confirm;
        mStatus = status;
        mTotalBytes = totalBytes;
        mCurrentBytes = currentBytes;
        mTimestamp = timestamp;
        mMediaScanned = mediaScanned;
    }

    public boolean isReadyToStart() {
        /*
         * For outbound 1. status is pending.
         * For inbound share 1. status is pending
         */
        if (mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
            if (mStatus == BluetoothShare.STATUS_PENDING && mUri != null) {
                return true;
            }
        } else if (mDirection == BluetoothShare.DIRECTION_INBOUND) {
            if (mStatus == BluetoothShare.STATUS_PENDING) {
                //&& mConfirm != BluetoothShare.USER_CONFIRMATION_PENDING) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCompletionNotification() {
        if (!BluetoothShare.isStatusCompleted(mStatus)) {
            return false;
        }
        if (mVisibility == BluetoothShare.VISIBILITY_VISIBLE) {
            return true;
        }
        return false;
    }

    /**
     * Check if a ShareInfo is invalid because of previous error
     */
    public boolean isObsolete() {
        if (BluetoothShare.STATUS_RUNNING == mStatus) {
            return true;
        }
        return false;
    }

}