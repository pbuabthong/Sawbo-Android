package edu.illinois.cs.bluetoothobexopp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class BluetoothOppFileSender {
    private Context mContext;
    private File fileToSend;
    private String remoteDeviceAddress;
    private BluetoothOppTransfer mTransfer;

    public BluetoothOppFileSender(Context context, File file, String address) {
        mContext = context;
        fileToSend = file;
        remoteDeviceAddress = address;
    }

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mTransfer.stop();
        }
    };

    public void send() {
        BluetoothOppShareInfo info = new BluetoothOppShareInfo(
                0,
                Uri.fromFile(fileToSend),
                "",
                fileToSend.getName(),
                "video/*",
                BluetoothShare.DIRECTION_OUTBOUND,
                remoteDeviceAddress,
                1,
                1,
                BluetoothShare.STATUS_PENDING,
                (int)fileToSend.length(),
                0,
                0,
                false
        );


        BluetoothOppBatch mBatch = new BluetoothOppBatch(mContext, info);
        mBatch.mDirection = BluetoothShare.DIRECTION_OUTBOUND;
        mTransfer = new BluetoothOppTransfer(mContext, null, mBatch);
        mTransfer.start();
    }

}
