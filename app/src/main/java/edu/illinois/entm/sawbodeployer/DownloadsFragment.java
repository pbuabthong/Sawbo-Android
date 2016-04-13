package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.illinois.cs.bluetoothobexopp.BluetoothOppFileSender;
import edu.illinois.entm.sawbodeployer.adapter.BrowseListArrayAdapter;
import edu.illinois.entm.sawbodeployer.btxfr.ClientThread;
import edu.illinois.entm.sawbodeployer.btxfr.MessageType;
import edu.illinois.entm.sawbodeployer.btxfr.ProgressData;
import edu.illinois.entm.sawbodeployer.btxfr.ServerThread;
import edu.illinois.entm.sawbodeployer.R;

public class DownloadsFragment extends Fragment {
	
	public DownloadsFragment(){}

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DISCOVERABLE_BT = 2;
    private final static int REQUEST_CONNECT_DEVICE_SECURE = 3;

    View rootView;
    WriteLog wl = new WriteLog();
    PrepareTitle pt = new PrepareTitle();
    public HashMap<String, String> titleArray = new HashMap<String, String>();
    public String dialogVideo, videoFilename, url;

    File OBEXfile;
    //private static final UUID MY_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private boolean viaOBEX = false;

    boolean firstTime=true;

    ServerThread st;
    ClientThread ct;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        rootView = inflater.inflate(R.layout.fragment_downloads, container, false);
        prepareTable();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return rootView;
    }

    public void prepareTable() {
        final ListView listview = (ListView) rootView.findViewById(R.id.download_list);
        File dirFiles = getActivity().getFilesDir();
        final ArrayList<String> videoFile = new ArrayList<String>();
        for (String strFile : dirFiles.list())
        {
            String extension = strFile.substring(strFile.lastIndexOf(".")+1);
            if(extension.equals("3gp") || extension.equals("mp4")){
                videoFile.add(strFile);
            }
            Log.v("filename", strFile);
        }

        pt.downloadTitle(getActivity());
        titleArray = pt.retrieveTitle(getActivity());
        if (titleArray.size()==0) {
            Toast.makeText(getActivity(), "Please connect to the internet to download the \"full title\" for the videos", Toast.LENGTH_SHORT).show();
        }
        String[] values = new String[videoFile.size()];
        Log.v("values_length", String.valueOf(videoFile.size()));

        TextView novid_txt = (TextView) rootView.findViewById(R.id.novid);
        if (videoFile.size()==0) {
            novid_txt.setVisibility(View.VISIBLE);
        } else {
            novid_txt.setVisibility(View.GONE);
        }

        String fullTitle;
        int count = 0;
        for (String fileName : videoFile) {
            String addlight = "";
            if(fileName.contains("_Light")) {
                addlight = " | Light";
            }
            StringTokenizer tokens = new StringTokenizer(fileName, "_");
            tokens.nextToken();
            String language = tokens.nextToken();
            String country = tokens.nextToken();
            String video = tokens.nextToken();
            fullTitle = titleArray.get(video);
            if (fullTitle==null) fullTitle = video;
            values[count] = fullTitle + "^^" + language
                    + " | " + country + addlight;
            count++;
        }

        if(values.length>0) {
            final BrowseListArrayAdapter adapter = new BrowseListArrayAdapter(getActivity(), values);
            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    wl.writeNow(getActivity(), "view", videoFile.get(position), "");
                    String url = getActivity().getFilesDir() + "/" + videoFile.get(position);

                    VideoPlaybackFragment fragment = null;
                    fragment = new VideoPlaybackFragment();
                    fragment.videoPath = url;
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).addToBackStack(null).commit();

                }
            });
            listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {
                    dialogVideo = videoFile.get(pos);
                    final int position = pos;
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_downloads);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();

                    Button sharesawbo_btn = (Button) dialog.findViewById(R.id.sharesawbo_btn);
                    final Button sharenormal_btn = (Button) dialog.findViewById(R.id.sharenormal_btn);
                    Button delete_btn = (Button) dialog.findViewById(R.id.delete_btn);

                    sharesawbo_btn.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    shareSAWBOAtPos(position, dialogVideo);
                                }
                            }
                    );

                    sharenormal_btn.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    shareNormalAtPos(position, dialogVideo);
                                }
                            }
                    );

                    delete_btn.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Are you sure you want to delete this video?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                    deleteVideoAtPos(position, dialogVideo);
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface d, int id) {
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                    );


                    return true;
                }
            });
        }
    }

    private void deleteVideoAtPos(int pos, String filename) {
        //Generate PNG filename
        String pngFilename = FilenameUtils.removeExtension(filename);
        pngFilename = pngFilename + ".png";
        wl.writeNow(getActivity(), "delete", filename, "");
        File file = new File(getActivity().getFilesDir() + "/" + filename);
        Log.v("file location", file.getAbsolutePath());
        file.delete();
        File imagefile = new File(getActivity().getFilesDir() + "/" + pngFilename);
        if (imagefile.exists()) {
            imagefile.delete();
        }
        prepareTable();
    }

    private void shareNormalAtPos(int pos, String filename) {
        // OBEX
        OBEXfile = new File(getActivity().getFilesDir() + "/" + filename);
        Log.d("filepath sharing", getActivity().getFilesDir() + "/" + filename);
        videoFilename = filename;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.v("Bluetooth", "not available");
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            } else {
                viaOBEX = true;
                senderBTReady();
            }
        }
    }

    private void shareSAWBOAtPos(int pos, String filename) {
        url = getActivity().getFilesDir() + "/" + filename;
        videoFilename = filename;
        //connectBluetooth(url);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmreceiver_str))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            Log.v("Bluetooth", "not available");
                        } else {
                            if (!mBluetoothAdapter.isEnabled()) {
                                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
                            } else {
                                viaOBEX = false;
                                senderBTReady();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


    }

    ProgressDialog progressDialog;
    ProgressData progressData = new ProgressData();

    Handler clientHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MessageType.READY_FOR_DATA: {
                    firstTime = true;
                    Toast.makeText(getActivity(), "Ready to send", Toast.LENGTH_SHORT).show();
                    try {
                        byte[] dataToSend = org.apache.commons.io.FileUtils.readFileToByteArray(new File(url));
                        byte[] header = new byte[(videoFilename.getBytes().length + 4)];
                        byte[] lheader = ByteBuffer.allocate(4).putInt(videoFilename.getBytes().length).array();
                        byte[] fheader = videoFilename.getBytes();
                        System.arraycopy(lheader, 0, header, 0, 4);
                        System.arraycopy(fheader, 0, header, 4, fheader.length);

                        byte[] lengthfilename = Arrays.copyOfRange(header, 0, 4);
                        int l = ByteBuffer.wrap(lengthfilename).getInt();
                        byte[] filename = Arrays.copyOfRange(header, 4, l+4);


                        Log.v("header", "Length: " + l + ", Filename: " + new String(filename, "UTF-8"));

                        byte[] datawheader = new byte[dataToSend.length + header.length];
                        System.arraycopy(header, 0, datawheader, 0, header.length);
                        System.arraycopy(dataToSend, 0, datawheader, header.length, dataToSend.length);
                        Message msgForSendingData = new Message();
                        msgForSendingData.obj = datawheader;
                        ct.incomingHandler.sendMessage(msgForSendingData);
                    } catch (IOException e) {
                        Log.d("SENDER", "Failed to open file " + url);
                        Toast.makeText(getActivity(), "Failed to open file " + url, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case MessageType.COULD_NOT_CONNECT: {
                    progressDialog.dismiss();
                    firstTime = true;
                    Toast.makeText(getActivity(), "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getActivity().getResources().getString(R.string.cannotcon_str))
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;
                }

                case MessageType.SENDING_DATA: {
                    if (progressDialog != null && firstTime) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        firstTime = false;
                    }
                    /*progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getResources().getString(R.string.sending_str));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getActivity().getResources().getString(R.string.stopsending_str))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            ct.cancel();
                                            if(progressDialog!=null) {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if(progressDialog!=null) {
                                                progressDialog.show();
                                            }
                                            return;
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                    progressDialog.setCanceledOnTouchOutside(true);
                    if(progressDialog!=null) {
                        progressDialog.show();
                    }*/
                    try {
                        progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) progressData.remainingSize / progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setMessage(getResources().getString(R.string.sending_str));
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(getActivity().getResources().getString(R.string.stopsending_str))
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    ct.cancel();
                                                    if (progressDialog != null) {
                                                        progressDialog.dismiss();
                                                        progressDialog = null;
                                                    }
                                                    firstTime = true;
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    if(progressDialog!=null) {
                                                        progressDialog.show();
                                                    }
                                                    return;
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });
                            progressDialog.setCanceledOnTouchOutside(true);
                            if(progressDialog!=null) {
                                progressDialog.show();
                            }

                        }
                        if(progressDialog!=null) {
                            progressDialog.setProgress((int) Math.floor(pctRemaining));
                        }

                    }catch(Exception e){
                        Log.v("Receiving exception: ", e.getMessage());
                    }
                    break;
                }

                case MessageType.DATA_SENT_OK: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    firstTime = true;
                    ct.cancel();
                    Toast.makeText(getActivity(), getResources().getString(R.string.sent_str), Toast.LENGTH_SHORT).show();
                    break;
                }

                case MessageType.EXCEPTION: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    firstTime = true;
                    ct.cancel();
                    Toast.makeText(getActivity(), "Sending Failed. Sharing was most likely canceled.", Toast.LENGTH_LONG).show();
                    break;
                }

                case MessageType.DIGEST_DID_NOT_MATCH: {

                    if(progressDialog!=null){
                        progressDialog.dismiss();
                        progressDialog=null;
                    }
                    firstTime = true;
                    ct.cancel();
                    Toast.makeText(getActivity(), getResources().getString(R.string.sendfail_str), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };

    public void senderBTReady() {
        Intent senderIntent = new Intent(getActivity(), DeviceListActivity.class);
        if(!viaOBEX) {
            senderIntent.putExtra("enableSearch", false);
        }
        startActivityForResult(senderIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }

    private void connectDevice(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        if(viaOBEX){
            wl.writeNow(getActivity(), "sharecontentviabluetooth", videoFilename, "", address);
            BluetoothOppFileSender sender = new BluetoothOppFileSender(getActivity(), OBEXfile, address);
            sender.send();
        }else {
            Log.v("remote", address.toString());
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (ct != null) {
                ct.cancel();
            }
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.connecting_str));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getActivity().getResources().getString(R.string.stopsending_str))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ct.cancel();
                                    if(progressDialog!=null) {
                                        progressDialog.dismiss();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(progressDialog!=null) {
                                        progressDialog.show();
                                    }
                                    return;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            progressDialog.setCanceledOnTouchOutside(true);
            if(progressDialog!=null) {
                progressDialog.show();
            }
            ct = new ClientThread(device, clientHandler);
            ct.start();
            /*wl.writeNow(getActivity(), "sharecontentviaapp", videoFilename, "", address);
            //(new SenderThread(device)).start();
            if (ct != null) {
                ct.cancel();
            }
            ct = new ClientThread(device, clientHandler);
            ct.start();*/
        }
    }

    /*
    private void pairDevice(BluetoothDevice device) {
        try {
            mDevice = device;
            Toast.makeText(getActivity(), "pairing", Toast.LENGTH_SHORT).show();
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            Boolean bool = (Boolean) method.invoke(device);
            Log.v("invoke", ""+bool);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getActivity().registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(getActivity(), "Paired", Toast.LENGTH_SHORT).show();
                    if (ct != null) {
                        ct.cancel();
                    }
                    ct = new ClientThread(mDevice, clientHandler);
                    ct.start();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(getActivity(), "Unpaired", Toast.LENGTH_SHORT).show();
                }

                //mBluetoothAdapter.notifyDataSetChanged();
            }
        }
    };
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    senderBTReady();
                }
                break;
            case REQUEST_DISCOVERABLE_BT:
                if (resultCode != Activity.RESULT_CANCELED) {
                    //receiverBTReader();
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                Log.v("Bluetooth", "request_connect_secure");
                if (resultCode == Activity.RESULT_OK) {
                    Log.v("Bluetooth", "ready for connection");
                    connectDevice(data);
                }
                break;

        }
    }
}
