package edu.illinois.entm.sawbodeployer;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import edu.illinois.entm.sawbodeployer.R;

public class VideoDetailFragment extends Fragment {
    public String videoFilename;
    View rootView;
    TextView title_txt, country_txt, language_txt, location_txt, size_txt;
    //public VideoView videoView;
    //public FrameLayout wp;
    String url, fullTitle;
    boolean stopDownload = false;
    long downloadID;
    BroadcastReceiver onComplete;
    ImageView iv;
    WriteLog wl = new WriteLog();
    Button download_btn, copy_btn;
    ImageButton play_btn;
    PrepareTitle pt = new PrepareTitle();
    public HashMap<String, String> titleArray = new HashMap<String, String>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        rootView = inflater.inflate(R.layout.fragment_videodetail, container, false);

        title_txt = (TextView) rootView.findViewById(R.id.title_txt);
        country_txt = (TextView) rootView.findViewById(R.id.country_txt);
        language_txt = (TextView) rootView.findViewById(R.id.language_txt);
        size_txt = (TextView) rootView.findViewById(R.id.size_txt);

        pt.downloadTitle(getActivity());
        titleArray = pt.retrieveTitle(getActivity());

        StringTokenizer tokens = new StringTokenizer(videoFilename, "_");
        tokens.nextToken();
        String language = tokens.nextToken();
        String country = tokens.nextToken();
        String video = tokens.nextToken();
        fullTitle = titleArray.get(video);
        if (fullTitle==null) fullTitle = video;

        url = getResources().getString(R.string.video_url) + videoFilename;

        double lenghtOfFile = 0.00;
        try{
            URL Rurl = new URL(url);
            URLConnection conexion = Rurl.openConnection();
            conexion.connect();
            lenghtOfFile = (0.0 + conexion.getContentLength())/1000000.0;
        } catch(Exception e){
            e.printStackTrace();
        }

        title_txt.setText(fullTitle);
        country_txt.setText(country);
        language_txt.setText(language);

        if (lenghtOfFile > 0) {
            lenghtOfFile = Double.parseDouble(new DecimalFormat("##.##").format(lenghtOfFile));
            size_txt.setText(getResources().getString(R.string.size_str) + lenghtOfFile + " MB");
        }

        iv = (ImageView) rootView.findViewById(R.id.thumbnail);

        //Generate PNG filename
        String pngFilename = FilenameUtils.removeExtension(videoFilename);
        pngFilename = pngFilename + ".png";

        String path = getActivity().getFilesDir() + "/";
        File imgFile = new  File(path + pngFilename);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            iv.setImageBitmap(myBitmap);
        }

        download_btn = (Button) rootView.findViewById(R.id.download_btn);
        File f = new File(getActivity().getFilesDir() + "/" + videoFilename);
        if(f.exists()) {
            download_btn.setText(getResources().getString(R.string.avoffline_str));
            download_btn.setEnabled(false);
        }

        copy_btn = (Button) rootView.findViewById(R.id.copy_btn);
        copy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(getResources().getString(R.string.video_url) + videoFilename);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", getResources().getString(R.string.video_url) + videoFilename);
                    clipboard.setPrimaryClip(clip);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getActivity().getResources().getString(R.string.url_copy_dialog))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        /*videoView = (VideoView)rootView.findViewById(R.id.videoView);
        wp = (FrameLayout) rootView.findViewById(R.id.wrapper);

        MediaController mediaController = new MediaController(rootView.getContext());
        mediaController.setVisibility(View.VISIBLE);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(url);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                videoView.setVisibility(View.INVISIBLE);
                wp.setVisibility(View.INVISIBLE);
            }
        });


        */
        getThumb();
        play_btn = (ImageButton) rootView.findViewById(R.id.play_btn);
        play_btn.bringToFront();

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wl.writeNow(getActivity(), "view", videoFilename, "");
                VideoPlaybackFragment fragment = null;
                fragment = new VideoPlaybackFragment();
                url = getResources().getString(R.string.video_url) + videoFilename;
                fragment.videoPath = url;
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();

                /*wp.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                wp.bringToFront();
                videoView.bringToFront();
                videoView.start();*/
            }
        });

        download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WriteLog wl = new WriteLog();
                if (stopDownload) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getActivity().getResources().getString(R.string.stopdownload_str))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                    getActivity().unregisterReceiver(onComplete);
                                    manager.remove(downloadID);
                                    download_btn.setText("Download");
                                    stopDownload = false;
                                    download_btn.invalidate();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                } else {
                    download_btn.setText("Stop download");
                    stopDownload = true;
                    //download_btn.setEnabled(false);
                    download_btn.invalidate();

                    if (isDownloadManagerAvailable(getActivity())) {
                        try {
                            String urlvideo = URLEncoder.encode(videoFilename, "UTF-8").replace("+", "%20");
                            Log.v("encode url", urlvideo);

                            URL url = new URL(getResources().getString(R.string.video_url) + urlvideo); //you can write here any link
                            //String url = "url you want to download";
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
                            request.setDescription("SAWBO video file");
                            request.setTitle(fullTitle);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            }
                            final File chkf = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoFilename);
                            if (chkf.exists()) {
                                chkf.delete();
                            }
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoFilename);

                            DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            downloadID = manager.enqueue(request);

                            onComplete = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {

                                    //Toast.makeText(getActivity().getBaseContext(), "Download finished", Toast.LENGTH_LONG).show();

                                    //Generate PNG filename
                                    String pngFilename = FilenameUtils.removeExtension(videoFilename);
                                    pngFilename = pngFilename + ".png";

                                    String path = context.getFilesDir() + "/";

                                    //Copy from tmp to internal storage
                                    File src = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoFilename);
                                    File dst = new File(path + videoFilename);
                                    Log.d("filepath", src.toString());
                                    try {
                                        copy(src, dst);
                                    } catch (IOException e) {

                                    }

                                    try {
                                        FileOutputStream out;
                                        File land = new File(path + pngFilename);
                                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path + videoFilename, MediaStore.Video.Thumbnails.MINI_KIND);//filePath is your video file path.
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        byte[] byteArray = stream.toByteArray();

                                        out = new FileOutputStream(land.getPath());
                                        out.write(byteArray);
                                        out.close();

                                        if (land.exists()) {
                                            Bitmap myBitmap = BitmapFactory.decodeFile(land.getAbsolutePath());
                                            iv.setImageBitmap(myBitmap);
                                            play_btn.bringToFront();
                                        }
                                    } catch (FileNotFoundException e) {

                                    } catch (IOException e) {

                                    }
                                    stopDownload = false;
                                    download_btn.setText(context.getResources().getString(R.string.avoffline_str));
                                    download_btn.setEnabled(false);
                                    download_btn.invalidate();
                                    if (chkf.exists()) {
                                        chkf.delete();
                                    }
                                }
                            };

                            getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                        } catch (UnsupportedEncodingException e) {

                        } catch (MalformedURLException e) {

                        }
                    }
                    //DownloadFromURL();
                    //new DownloadVideo().execute(new String[]{});
                    wl.writeNow(getActivity(), "download", videoFilename, "");
                    wl.sendLog(getActivity());

                }
            }
        });


        Log.v("test", videoFilename);
        return rootView;
    }

    public void getThumb() {
        String urlvideo = videoFilename;
        try{
            urlvideo = URLEncoder.encode(videoFilename, "utf-8");
        } catch (UnsupportedEncodingException e) {

        }
        String path = getResources().getString(R.string.video_url) + urlvideo;
        Log.v("path", path);
    }

    /*private int progressposition = 0;
    private int progresslength = 0;
    private String showtxt="Download in progress";


    private void showNotificationProgress() {
        final int id = 123;

        if(isAdded()){
            showtxt = getResources().getString(R.string.downloadprog_str);
        }

        final NotificationManager mNotifyManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity());
        mBuilder.setContentTitle(getResources().getString(R.string.downloadsawbo_str))
                .setContentText(showtxt)
                .setSmallIcon(R.drawable.ic_share_holo_dark);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d("download", "download" + progressposition);
                        while (progressposition < progresslength) {
                            mBuilder.setProgress((int)progresslength, progressposition, false);
                            /*getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    download_btn.setText("Downloading " + (progressposition/progresslength*100) + "%");
                                }
                            });
                            mNotifyManager.notify(id, mBuilder.build());
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                Log.v("progress", "sleep failure");
                            }
                        }
                        mBuilder.setContentText(getResources().getString(R.string.succdown_str)).setProgress(0,0,false);
                        mNotifyManager.notify(id, mBuilder.build());
                    }
                }
        ).start();
    }*/

    /*
    private class DownloadVideo extends AsyncTask<String, Void, String> {

        ByteArrayBuffer baf;

        @Override
        protected String doInBackground(String... videoURL) {
            String response = "";
            try {
                String urlvideo = URLEncoder.encode(videoFilename, "UTF-8").replace("+", "%20");
                Log.v("encode url", urlvideo);

                URL url = new URL(getResources().getString(R.string.video_url) + urlvideo); //you can write here any link

                long startTime = System.currentTimeMillis();
                Log.d("download", "download begining");
                Log.d("download", "download url:" + url);
                Log.d("download", "downloaded file name:" + videoFilename);

                URLConnection ucon = url.openConnection();
                ucon.connect();
                int file_size = ucon.getContentLength();
                Log.v("length", ""+file_size);
                //File file = new File(videoFilename);
                //String fileNameWithOutExt = FilenameUtils.removeExtension(videoFilename);
                //String tmpdownload = fileNameWithOutExt + ".download";


                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                progresslength = file_size;
                progressposition = 0;
                showNotificationProgress();
                //TODO: Memory
                baf = new ByteArrayBuffer(50);
                int total = 0;
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                    total += current;
                    progressposition = total;
                    //Log.v("current: ", ""+current);
                }


            } catch (IOException e) {
                Log.d("Error", "Error: " + e);
            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.v("onPost", "inOnpost:"+videoFilename);
                FileOutputStream fos = getActivity().openFileOutput(videoFilename, Context.MODE_PRIVATE);
                fos.write(baf.toByteArray());
                fos.close();

                //Generate PNG filename
                String pngFilename = FilenameUtils.removeExtension(videoFilename);
                pngFilename = pngFilename + ".png";

                String path = getActivity().getFilesDir() + "/";

                FileOutputStream out;
                File land=new File(path +pngFilename);
                Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(path + videoFilename, MediaStore.Video.Thumbnails.MINI_KIND);//filePath is your video file path.
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                out=new  FileOutputStream(land.getPath());
                out.write(byteArray);
                out.close();

                if(land.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(land.getAbsolutePath());
                    iv.setImageBitmap(myBitmap);
                    play_btn.bringToFront();
                }

                download_btn.setText(getResources().getString(R.string.avoffline_str));
                download_btn.setEnabled(false);
                download_btn.invalidate();
            } catch (IOException e) {

            }
        }
    }
*/
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

    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }


//    try {
//        String urlvideo = URLEncoder.encode(videoFilename, "UTF-8").replace("+", "%20");
//        Log.v("encode url", urlvideo);
//
//        URL url = new URL("http://www-u.life.illinois.edu/mobile_app/sources/" + urlvideo); //you can write here any link
//        File file = new File(videoFilename);
//
//        long startTime = System.currentTimeMillis();
//        Log.d("download", "download begining");
//        Log.d("download", "download url:" + url);
//        Log.d("download", "downloaded file name:" + videoFilename);
//                        /* Open a connection to that URL. */
//        URLConnection ucon = url.openConnection();
//
//                        /*
//                         * Define InputStreams to read from the URLConnection.
//                         */
//        InputStream is = ucon.getInputStream();
//        BufferedInputStream bis = new BufferedInputStream(is);
//
//                        /*
//                         * Read bytes to the Buffer until there is nothing more to read(-1).
//                         */
//        //TODO: Memory
//        ByteArrayBuffer baf = new ByteArrayBuffer(50);
//        int current = 0;
//        while ((current = bis.read()) != -1) {
//            baf.append((byte) current);
//        }
//
//                        /* Convert the Bytes read to a String. */
//
//        FileOutputStream fos = getActivity().openFileOutput(videoFilename, Context.MODE_PRIVATE);
//        fos.write(baf.toByteArray());
//        fos.close();
//
//            /*FileOutputStream fos = new FileOutputStream(videoFilename);
//            fos.write(baf.toByteArray());
//            fos.close();*/
//        Log.d("Download", "download ready in"
//                + ((System.currentTimeMillis() - startTime) / 1000)
//                + " sec");
//
//    } catch (IOException e) {
//        Log.d("Error", "Error: " + e);
//    }

}
