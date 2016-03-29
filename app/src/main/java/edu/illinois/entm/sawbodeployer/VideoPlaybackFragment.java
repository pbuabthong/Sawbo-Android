package edu.illinois.entm.sawbodeployer;


import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import edu.illinois.entm.sawbodeployer.R;

/**
 * Created by Pakpoomb on 9/6/14.
 */
public class VideoPlaybackFragment extends Fragment {
    View rootView;
    String videoPath;
    VideoView videoView;
    FrameLayout wp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        rootView = inflater.inflate(R.layout.fragment_videoplayback, container, false);
        videoView = (VideoView) rootView.findViewById(R.id.videoPlaybackView);
        wp = (FrameLayout) rootView.findViewById(R.id.wrapper_playback);
        //String url = "http://www-u.life.illinois.edu/mobile_app/sources/" + videoFilename;
        final MediaController mediaController = new MediaController(rootView.getContext());
        mediaController.setVisibility(View.VISIBLE);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vmp) {
                FragmentManager fragmentManager = getFragmentManager();
                BrowseFragment bf = new BrowseFragment();
                fragmentManager.popBackStackImmediate();
                videoView.setVisibility(View.INVISIBLE);
                mediaController.hide();
                wp.setVisibility(View.INVISIBLE);
            }
        });
        videoView.start();

        return rootView;
    }
}