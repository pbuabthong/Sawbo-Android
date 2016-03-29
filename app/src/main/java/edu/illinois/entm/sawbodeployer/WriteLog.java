package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.NetworkOnMainThreadException;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

/**
 * Created by Pakpoomb.
 */
public class WriteLog {

    private String appID = "0011";
    private double[] getGPS(Activity act) {
        LocationManager lm = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();

        Boolean acc = getAcc(act);
        double[] gps = new double[3];
        if(!acc){
            return gps;
        }
        //Alt
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        //List<String> providers = lm.getProviders(true);
        List<String> providers = lm.getProviders(criteria, true);

        Location l = null;

        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
            gps[2] = l.getAltitude();
        }
        return gps;
    }

    private String getIP(){
        try {
            String myUri = "http://www.sawbo-illinois.org/mobile_app/ip.php";
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(myUri);
            HttpResponse response = httpClient.execute(get);

            String bodyHtml = EntityUtils.toString(response.getEntity());
            if (bodyHtml.length() > 16) bodyHtml = "";
            return bodyHtml;
        }catch (IOException e){
            return "";
        }catch (NetworkOnMainThreadException e){
            return "";
        }
    }

    public String getdID(Activity act){
        String result = "";
        String url = act.getFilesDir() + "/id.txt";
        File file = new File(url);
        if(file.exists()){
            Log.v("getdID", "exists");
            try {
                FileInputStream fis = act.openFileInput("id.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                line = bufferedReader.readLine();
                result = line;
            }catch (IOException e){
                Log.v("Error", ":(");
            }
        }else {
            Log.v("getdID", "new");

            //write ID to file
            try {
                result = BluetoothAdapter.getDefaultAdapter().getAddress();
            }catch (NullPointerException e){
                Log.v("Error", e.toString());
            }
            if(result.isEmpty()) {
                TelephonyManager tManager = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
                result = tManager.getDeviceId();
                if(result.isEmpty() || result.equals("000000000000000")){
                    Random r = new Random();
                    int i1 = r.nextInt(99999999 - 10000000) + 10000000;
                    result = Integer.toString(i1);
                }
            }
            try {
                FileOutputStream fos = act.openFileOutput("id.txt", Context.MODE_APPEND);
                fos.write(result.getBytes());
                fos.close();
            } catch (IOException e){
                Log.e("error", e.toString());
            }
        }
        Log.v("Return", result);
        return result;
    }

    public Boolean getAcc (Activity act){
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        boolean gpsOk = sharedPref.getBoolean("usegps", true);
        Log.v("location", ""+gpsOk);
        if (gpsOk) {
            return true;
        }else{
            return false;
        }
    }

    private String peer;
    public boolean writeNow(Activity act, String eventType, String content, String ipaddress, String peerID){
        peer = peerID;
        return writeNow(act, eventType, content, ipaddress);
    }

    public boolean writeNow(Activity act, String eventType, String content, String ipaddress){
        Log.v("writelog", "in");
        if(ipaddress.isEmpty()){
            ipaddress=getIP();
        }
        String uid = getdID(act);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //Get gps
        double[] gpsloc = getGPS(act);
        //eg buf type=view|device=shaohan-device|content=python2|ip=1.2.3.4|date=1408090984932#
        String peerIDLog = "";
        if (peer != null && !peer.isEmpty()){
            peerIDLog = "|peer="+peer;
        }

        String buftext = "type=" + eventType + "|appid=" + appID + "|device=" + uid + peerIDLog + "|content=" + content + "|ip=" + ipaddress
                + "|date=" + timeStamp + "|lat=" + gpsloc[0] + "|lon=" + gpsloc[1] + "|alt=" + gpsloc[2] + "#";

        /*if (eventType.equals("locale")) {
            buftext = "type=" + eventType + "|device=" + uid + peerIDLog + "|language=" + content + "|ip=" + ipaddress
                    + "|date=" + timeStamp + "|lat=" + gpsloc[0] + "|lon=" + gpsloc[1] + "|alt=" + gpsloc[2] + "#";
        } else */

        if (eventType.equals("sharesawbo")) {
            buftext = "type=" + eventType + "|appid=" + appID + "|device=" + uid + peerIDLog + "|ip=" + ipaddress
                    + "|date=" + timeStamp + "|lat=" + gpsloc[0] + "|lon=" + gpsloc[1] + "|alt=" + gpsloc[2] + "#";
        }

        Log.v("log", buftext);
        try {
            //File file = new File("log.txt");
            //FileOutputStream fos = act.openFileOutput(MYFILE, Context.MODE_PRIVATE);
            // append to file
            FileOutputStream fos = act.openFileOutput("log.txt", Context.MODE_APPEND);
            fos.write(buftext.getBytes());
            fos.close();
        }catch (IOException e){
            Log.e("error", e.toString());
            return false;
        }
        return true;
    }

    public boolean sendLog(Activity act){
        try {
            FileInputStream fis = act.openFileInput("log.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            //StringBuilder sb = new StringBuilder();
            String line, total;
            total="";
            while ((line = bufferedReader.readLine()) != null) {
                total += line;
            }
            Log.v("sendLog", total);

            try {
                //Socket logSocket = new Socket(act.getResources().getString(R.string.gsim_url), act.getResources().getInteger(R.integer.gsim_port));
                Socket logSocket = new Socket("mcdm.sawbo.illinois.edu", 1731);
                //Socket logSocket = new Socket();
                //logSocket.connect(new InetSocketAddress("gsim.cs.illinois.edu", 1731), 500);
                //if (logSocket.isConnected()) {
                    PrintWriter out = new PrintWriter(logSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(logSocket.getInputStream()));
                    out.println(total);
                    String ir = in.readLine();
                    //Log.v("server:", ir);
                    if (ir!=null) {
                        Log.v("in.readLine", "reached");
                        File file = new File(act.getFilesDir() + "/log.txt");
                        Log.v("file is", String.valueOf(file.exists()) + file.getAbsolutePath());
                        file.delete();
                        Log.v("in.readLine", "end");
                    }
                //}

            } catch (UnknownHostException e) {
                System.err.println("Don't know about host ");
                //System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to ");
                //System.exit(1);
            }
        }catch (IOException e){
            Log.v("Error", ":(");
            return false;
        }
        return true;
    }
}
