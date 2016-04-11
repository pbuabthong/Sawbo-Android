package edu.illinois.entm.sawbodeployer.btxfr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

//import com.simonguest.btxfr.Constants;
//import com.simonguest.btxfr.MessageType;
//import com.simonguest.btxfr.Utils;

public class ClientThread extends Thread {
    private final String TAG = "android-btxfr";
    private BluetoothSocket socket;
    private final Handler handler;
    public Handler incomingHandler;
    private BluetoothDevice bDevice;

    public ClientThread(BluetoothDevice device, Handler handler) {
        BluetoothSocket tempSocket = null;
        this.handler = handler;

        try {
            tempSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID_STRING));
            /*BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            Method m = mAdapter.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] { UUID.class });
            tempSocket = (BluetoothSocket) m.invoke(mAdapter, new Object[] { UUID.fromString(Constants.UUID_STRING) });*/
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        this.socket = tempSocket;
    }

    public void run() {
        try {
            Log.v(TAG, "Opening client socket");
            socket.connect();
            Log.v(TAG, "Connection established");

        } catch (IOException ioe) {
            handler.sendEmptyMessage(MessageType.COULD_NOT_CONNECT);
            Log.e(TAG, ioe.toString());
            try {
                socket.close();
            } catch (IOException ce) {
                Log.e(TAG, "Socket close exception: " + ce.toString());
            }
        }

        Looper.prepare();

        incomingHandler = new Handler(){
            @Override
            public void handleMessage(Message message)
            {
                if (message.obj != null)
                {
                    Log.v(TAG, "Handle received data to send");
                    byte[] payload = (byte[])message.obj;

                    try {
                        handler.sendEmptyMessage(MessageType.SENDING_DATA);
                        OutputStream outputStream = socket.getOutputStream();

                        // Send the header control first
                        outputStream.write(Constants.HEADER_MSB);
                        outputStream.write(Constants.HEADER_LSB);

                        // write size
                        outputStream.write(Utils.intToByteArray(payload.length));

                        // write digest
                        byte[] digest = Utils.getDigest(payload);
                        outputStream.write(digest);

                        // now write the data
                        outputStream.write(payload);
                        outputStream.flush();

                        Log.v(TAG, "Data sent.  Waiting for return digest as confirmation");
                        InputStream inputStream = socket.getInputStream();
                        byte[] incomingDigest = new byte[16];
                        int incomingIndex = 0;

                        try {
                            while (true) {
                                byte[] header = new byte[1];
                                inputStream.read(header, 0, 1);
                                incomingDigest[incomingIndex++] = header[0];
                                if (incomingIndex == 16) {
                                    if (MessageDigest.isEqual(digest, incomingDigest)) {
                                        Log.v(TAG, "Digest matched OK.  Data was received OK.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DATA_SENT_OK);
                                    } else {
                                        Log.e(TAG, "Digest did not match.  Might want to resend.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DIGEST_DID_NOT_MATCH);
                                    }

                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }

                        Log.v(TAG, "Closing the client socket.");
                        socket.close();

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            }
        };

        if (socket.isConnected()) {
            handler.sendEmptyMessage(MessageType.READY_FOR_DATA);
            Looper.loop();
        }
    }

    public void cancel() {
        try {
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG + "Cancel exception: ", e.toString());
        }
    }
}