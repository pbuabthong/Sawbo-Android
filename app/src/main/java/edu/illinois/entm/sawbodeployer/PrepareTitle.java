package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.illinois.entm.sawbodeployer.R;

public class PrepareTitle {
    public boolean downloadTitle(Activity act)
    {
        String titlepath = act.getFilesDir() + "/title.txt";
        File file = new File(titlepath);
        try {
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            URL url = new URL(act.getResources().getString(R.string.title_url));

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            Log.v("test", "before connect");

            //and connect!
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();
            Log.v("test", "after connect");

            //File file = new File("title.txt");
            Log.v("test", "after connect2");

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = act.openFileOutput("title.txt", Context.MODE_PRIVATE);
            Log.v("test", "after connect3");

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
            Log.v("test", "after connect4");

            Log.v("result", inputStream.toString());
            //this is the total size of the file
            int totalSize = urlConnection.getContentLength();
            //variable to store total downloaded bytes
            int downloadedSize = 0;
            Log.v("test", "after connect5");

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                downloadedSize += bufferLength;
                //this is where you would do something to report the prgress, like this maybe
                //updateProgress(downloadedSize, totalSize);
                Log.i("download progress", String.valueOf(downloadedSize));
            }
            Log.v("test", "after connect6");
            //close the output stream when done
            fileOutput.close();

            //Prepare title array


            return true;
//catch some possible errors...
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public HashMap<String, String> retrieveTitle(Activity act) {
        HashMap<String, String> finalTitle = new HashMap<String, String>();
        try {
            FileInputStream fis = act.openFileInput("title.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            //StringBuilder sb = new StringBuilder();
            String line, ke, val;
            while ((line = bufferedReader.readLine()) != null && line.contains("///")) {
                if (line.contains("<")) {
                    Log.e("Break", "contain <");
                    break;
                }
                Log.v("Break", "not contain <");
                StringTokenizer tokens = new StringTokenizer(line, "///");
                ke = tokens.nextToken();
                val = tokens.hasMoreTokens() ? tokens.nextToken():"";
                finalTitle.put(ke, val);
            }
        }catch (IOException e){
            Log.v("Error", ":(");
        }
        return finalTitle;
    }

    public HashMap<String, String> retrieveTopic(Activity act) {
        HashMap<String, String> finalTitle = new HashMap<String, String>();
        try {
            FileInputStream fis = act.openFileInput("title.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            //StringBuilder sb = new StringBuilder();
            String line, ke, val, topic;
            while ((line = bufferedReader.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(line, "///");
                ke = tokens.nextToken();
                val = tokens.hasMoreTokens() ? tokens.nextToken():"";
                topic = tokens.hasMoreTokens() ? tokens.nextToken():"";
                finalTitle.put(ke, topic);
                Log.v("topic", "ke" + "::" + "topic");
            }
        }catch (IOException e){
            Log.v("Error", ":(");
        }
        return finalTitle;
    }
}
