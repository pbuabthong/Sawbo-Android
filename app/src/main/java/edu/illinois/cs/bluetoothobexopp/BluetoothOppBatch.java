package edu.illinois.cs.bluetoothobexopp;

/**
 * Created by shu17 on 9/21/14.
 */


import java.io.File;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

/**
 * This class stores information about a batch of OPP shares that should be
 * transferred in one session.
 */
/*There are a few cases: 1. create a batch for a single file to send
 * 2. create a batch for multiple files to send
 * 3. add additional file(s) to existing batch to send
 * 4. create a batch for receive single file
 * 5. add additional file to existing batch to receive (this only happens as the server
 * session notify more files to receive)
 * 6. Cancel sending a single file
 * 7. Cancel sending a file from multiple files (implies cancel the transfer, rest of
 * the unsent files are also canceled)
 * 8. Cancel receiving a single file
 * 9. Cancel receiving a file (implies cancel the transfer, no additional files will be received)
 */

public class BluetoothOppBatch {
    private static final String TAG = "BtOppBatch";
    private static final boolean V = Constants.VERBOSE;

    public int mId;
    public int mStatus;

    public  long mTimestamp;
    public  int mDirection;
    public  BluetoothDevice mDestination;

    private BluetoothOppBatchListener mListener;

    private final ArrayList<BluetoothOppShareInfo> mShares;
    private final Context mContext;

    /**
     * An interface for notifying when BluetoothOppTransferBatch is changed
     */
    public interface BluetoothOppBatchListener {
        /**
         * Called to notify when a share is added into the batch
         * @param id , BluetoothOppShareInfo.id
         */
        public void onShareAdded(int id);

        /**
         * Called to notify when a share is deleted from the batch
         * @param id , BluetoothOppShareInfo.id
         */
        public void onShareDeleted(int id);

        /**
         * Called to notify when the batch is canceled
         */
        public void onBatchCanceled();
    }

    /**
     * A batch is always created with at least one ShareInfo
     * @param context, Context
     * @param info, BluetoothOppShareInfo
     */
    public BluetoothOppBatch(Context context, BluetoothOppShareInfo info) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mShares = new ArrayList();
        mTimestamp = info.mTimestamp;
        mDirection = info.mDirection;
        mDestination = adapter.getRemoteDevice(info.mDestination);
        mStatus = Constants.BATCH_STATUS_PENDING;
        mShares.add(info);

        if (V) Log.v(TAG, "New Batch created for info " + info.mId);
    }



    public BluetoothOppBatch(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mShares = new ArrayList();
        mStatus = Constants.BATCH_STATUS_PENDING;

    }

    /**
     * Add one share into the batch.
     */
    /* There are 2 cases: Service scans the databases and it's multiple send
     * Service receives database update and know additional file should be received
     */

    public void addShare(BluetoothOppShareInfo info) {
        mShares.add(info);
        if (mListener != null) {
            mListener.onShareAdded(info.mId);
        }
    }

    /**
     * Delete one share from the batch. Not used now.
     */
    /*It should only be called under requirement that cancel one single share, but not to
     * cancel the whole batch. Currently we assume "cancel" is to cancel whole batch.
     */
    public void deleteShare(BluetoothOppShareInfo info) {
        if (info.mStatus == BluetoothShare.STATUS_RUNNING) {
            info.mStatus = BluetoothShare.STATUS_CANCELED;
            if (info.mDirection == BluetoothShare.DIRECTION_INBOUND && info.mFilename != null) {
                new File(info.mFilename).delete();
            }
        }

        if (mListener != null) {
            mListener.onShareDeleted(info.mId);
        }
    }

    /**
     * Cancel the whole batch.
     */
    /* 1) If the batch is running, stop the transfer
     * 2) Go through mShares list and mark all incomplete share as CANCELED status
     * 3) update ContentProvider for these canceled transfer
     */
    public void cancelBatch() {
        if (V) Log.v(TAG, "batch " + this.mId + " is canceled");

        if (mListener != null) {
            mListener.onBatchCanceled();
        }
        //TODO investigate if below code is redundant
        for (int i = mShares.size() - 1; i >= 0; i--) {
            BluetoothOppShareInfo info = mShares.get(i);

            if (info.mStatus < 200) {
                if (info.mDirection == BluetoothShare.DIRECTION_INBOUND && info.mFilename != null) {
                    new File(info.mFilename).delete();
                }
                if (V) Log.v(TAG, "Cancel batch for info " + info.mId);

                Constants.updateShareStatus(mContext, info.mId, BluetoothShare.STATUS_CANCELED);
            }
        }
        mShares.clear();
    }

    /** check if a specific share is in this batch */
    public boolean hasShare(BluetoothOppShareInfo info) {
        return mShares.contains(info);
    }

    /** if this batch is empty */
    public boolean isEmpty() {
        return (mShares.size() == 0);
    }

    public int getNumShares() {
        return mShares.size();
    }

    /**
     * Get the running status of the batch
     * @return
     */

    /** register a listener for the batch change */
    public void registerListern(BluetoothOppBatchListener listener) {
        mListener = listener;
    }

    /**
     * Get the first pending ShareInfo of the batch
     * @return BluetoothOppShareInfo, for the first pending share, or null if
     *         none exists
     */
    public BluetoothOppShareInfo getPendingShare() {
        for (int i = 0; i < mShares.size(); i++) {
            BluetoothOppShareInfo share = mShares.get(i);
            if (share.mStatus == BluetoothShare.STATUS_PENDING) {
                return share;
            }
        }
        return null;
    }
}