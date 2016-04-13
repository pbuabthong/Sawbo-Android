package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.bluetoothobexopp.BluetoothOppFileSender;
import edu.illinois.entm.sawbodeployer.btxfr.ClientThread;
import edu.illinois.entm.sawbodeployer.btxfr.MessageType;
import edu.illinois.entm.sawbodeployer.btxfr.ProgressData;
import edu.illinois.entm.sawbodeployer.btxfr.ServerThread;
import edu.illinois.entm.sawbodeployer.R;

public class ShareFragment extends Fragment {

    public ShareFragment(){}
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DISCOVERABLE_BT = 2;
    private final static int REQUEST_CONNECT_DEVICE_SECURE = 3;

    //private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //private static final UUID MY_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");
    protected static BluetoothAdapter mBluetoothAdapter;
    private boolean viaOBEX = false;
    //private BroadcastReceiver mReceiver;

    View rootView;
    WriteLog wl = new WriteLog();
    String url, videoFilename, appFilepath;
    File OBEXfile;

    ProgressDialog progressDialog, waitingDialog;
    ProgressData progressData = new ProgressData();

    ServerThread st;
    ClientThread ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        rootView = inflater.inflate(R.layout.fragment_share, container, false);

        Button sharesawbo_txt_btn = (Button) rootView.findViewById(R.id.sharesawboapp_txt_btn);
        Button sharenormal_txt_btn = (Button) rootView.findViewById(R.id.sharenormal_txt_btn);
        Button send_txt_btn = (Button) rootView.findViewById(R.id.send_txt_btn);

        sharenormal_txt_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getActivity().getResources().getString(R.string.receive_long_str))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
        );

        sharesawbo_txt_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getActivity().getResources().getString(R.string.sharesawbo_long_str))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
        );

        send_txt_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getActivity().getResources().getString(R.string.sharenormal_long_str))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
        );

        ImageButton connect_btn = (ImageButton) rootView.findViewById(R.id.connect_btn);
        ImageButton sharesawboapp_btn = (ImageButton) rootView.findViewById(R.id.sharesawboapp_btn);
        connect_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //connectBluetooth();
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            Log.v("Bluetooth", "not available");
                        } else {
                            Intent enableBtDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            startActivityForResult(enableBtDiscoverableIntent, REQUEST_DISCOVERABLE_BT);
                        }

                    }
                }
        );

        sharesawboapp_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        final List pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
                        String filepath;
                        for (Object object : pkgAppsList) {
                            ResolveInfo info = (ResolveInfo) object;
                            File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
                            filepath = file.getAbsolutePath();
                            if (filepath.contains(getResources().getString(R.string.app_package))) {
                                appFilepath = filepath;
                                Log.v("test", appFilepath);
                            }
                            // Copy the .apk file to wherever
                        }

                        /*PackageManager pm = getActivity().getPackageManager();
                        List<PackageInfo> pkginfo_list = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
                        List<ApplicationInfo> appinfo_list = pm.getInstalledApplications(0);
                        for (int x = 0; x < pkginfo_list.size(); x++) {
                            PackageInfo pkginfo = pkginfo_list.get(x);
                            String app_filename = appinfo_list.get(x).publicSourceDir;
                            if (app_filename.contains(getResources().getString(R.string.app_package))) {
                                appFilepath = app_filename;
                            }
                        }*/
                        // OBEX
                        File src = new File(appFilepath);
                        File dst = new File(getActivity().getFilesDir() + "/" + "sawbo.apk");
                        if (!dst.exists()) {
                            try {
                                copy(src, dst);
                            } catch (IOException e) {
                                Log.d("Error", e.toString());
                            }
                        }
                        OBEXfile = new File(getActivity().getFilesDir() + "/" + "sawbo.apk");
                        Log.d("filepath sharing", appFilepath);
                        if (OBEXfile.exists()) {
                            Toast.makeText(getActivity(), ".apk found!", Toast.LENGTH_SHORT).show();
                        }
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
                }
        );

        //prepareTable();
        return rootView;
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public void senderBTReady() {
        Intent senderIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(senderIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    senderBTReady();
                }
                break;
            case REQUEST_DISCOVERABLE_BT:
                Log.v("result", ""+resultCode);
                if (resultCode != Activity.RESULT_CANCELED) {
                    waitingDialog = new ProgressDialog(getActivity());
                    String displaytxt = getResources().getString(R.string.waiting_str) + wl.getdID(getActivity());
                    waitingDialog.setMessage(displaytxt);
                    waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    waitingDialog.setCanceledOnTouchOutside(true);
                    waitingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getActivity().getResources().getString(R.string.stopreceving_str))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            st.cancelRun();
                                            if(waitingDialog!=null) {
                                                waitingDialog.dismiss();
                                                waitingDialog = null;
                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if(waitingDialog!=null) {
                                                waitingDialog.show();
                                            }
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                    if(waitingDialog!=null) {
                        waitingDialog.show();
                    }
                    receiverBTReader();

                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.v("Bluetooth", "ready for connection");
                    connectDevice(data);
                }
                break;

        }
    }

    private void connectDevice(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        wl.writeNow(getActivity(), "sharesawbo", "sawbo.apk", "", address);
        if(viaOBEX){
            BluetoothOppFileSender sender = new BluetoothOppFileSender(getActivity(), OBEXfile, address);
            sender.send();
        }
    }


    Handler serverHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {
                        if(waitingDialog!=null){
                            waitingDialog.dismiss();
                            waitingDialog = null;
                        }
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        byte[] lengthfilename = Arrays.copyOfRange((byte[]) message.obj, 0, 4);
                        int l = ByteBuffer.wrap(lengthfilename).getInt();
                        byte[] filename = Arrays.copyOfRange((byte[]) message.obj, 4, l + 4);
                        String fileString = "test_test_test_test_test_test.3gp";
                        byte[] realByte = Arrays.copyOfRange((byte[]) message.obj, l + 4, ((byte[]) message.obj).length);

                        try {
                            fileString = new String(filename, "UTF-8");
                            Log.v("header", "Length: " + l + ", Filename: " + fileString);
                        } catch (UnsupportedEncodingException e) {

                        }

                        try {
                            FileOutputStream fos = getActivity().openFileOutput(fileString, Context.MODE_PRIVATE);
                            fos.write(realByte, 0, realByte.length);
                            fos.close();

                            //Generate PNG filename
                            String pngFilename = FilenameUtils.removeExtension(fileString);
                            pngFilename = pngFilename + ".png";

                            String path = getActivity().getFilesDir() + "/";

                            FileOutputStream out;
                            File land = new File(path + pngFilename);
                            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path + fileString, MediaStore.Video.Thumbnails.MINI_KIND);//filePath is your video file path.
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            out = new FileOutputStream(land.getPath());
                            out.write(byteArray);
                            out.close();

                            //BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
//                        bos.write(realByte, 0, realByte.length);
                        } catch (Exception e) {

                        }

//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 2;
//                    Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
//                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                    imageView.setImageBitmap(image);
                        Toast.makeText(getActivity(), getResources().getString(R.string.received_str), Toast.LENGTH_SHORT).show();
                        st.cancel();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                            progressDialog=null;
                        }
                        if (waitingDialog!=null){
                            waitingDialog.dismiss();
                            waitingDialog=null;
                        }
                        Toast.makeText(getActivity(), getResources().getString(R.string.sendfail_str), Toast.LENGTH_SHORT).show();
                        st.cancel();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        try {
                            progressData = (ProgressData) message.obj;
                            double pctRemaining = 100 - (((double) progressData.remainingSize / progressData.totalSize) * 100);
                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage(getResources().getString(R.string.receiving_str));
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressDialog.setProgress(0);
                                progressDialog.setMax(100);
                                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(getActivity().getResources().getString(R.string.stopreceving_str))
                                                .setCancelable(false)
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        st.cancel();
                                                        if (progressDialog != null) {
                                                            progressDialog.dismiss();
                                                            progressDialog = null;
                                                        }
                                                        if (waitingDialog != null) {
                                                            waitingDialog.dismiss();
                                                            waitingDialog = null;
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

                            }
                            if(progressDialog!=null) {
                                progressDialog.setProgress((int) Math.floor(pctRemaining));
                            }

                        }catch(Exception e){
                            Log.v("Receiving exception: ", e.getMessage());
                        }
                        break;

                    }

                    case MessageType.INVALID_HEADER: {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                            progressDialog=null;
                        }
                        if (waitingDialog!=null){
                            waitingDialog.dismiss();
                            waitingDialog=null;
                        }
                        Toast.makeText(getActivity(), getResources().getString(R.string.wrongheader_str), Toast.LENGTH_SHORT).show();
                        st.cancel();
                        break;
                    }
                    case MessageType.EXCEPTION: {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                            progressDialog=null;
                        }
                        if (waitingDialog!=null){
                            waitingDialog.dismiss();
                            waitingDialog=null;
                        }
                        Toast.makeText(getActivity(), "Receiving failed. Sharing was most likely canceled.", Toast.LENGTH_SHORT).show();
                        st.cancel();
                        break;
                    }
                }
        }
    };

    public void receiverBTReader() {
        Log.v("Bluetooth", "Bluetooth is ready for receiver.");
        if (st != null) {
            st.cancel();
        }
        st = new ServerThread(mBluetoothAdapter, serverHandler);
        st.start();

    }
}


