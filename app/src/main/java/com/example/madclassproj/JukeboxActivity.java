package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JukeboxActivity extends AppCompatActivity {
    private String getSong = "http://mad.mywork.gr/get_song.php?t=429822";
    private String statusPlaying = "Playing";
    private String mp3URL, statusSong, songTitle, songArtist, msgStringSong;
    private ImageView play, pause, request;
    private TextView tv_status, tv_SongTitle, tv_SongArtist, tv_SongURL;
    private SeekBar mp3SeekBar;
    final static MediaPlayer mp3player = new MediaPlayer();
    boolean runnableRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jukebox);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //prevent screen rotation and activity restart
        request = findViewById(R.id.requestBtn);
        play = findViewById(R.id.playBtn);
        pause = findViewById(R.id.pauseBtn);
        mp3SeekBar = findViewById(R.id.seekBar);
        tv_status = findViewById(R.id.musicStatus);
        tv_SongTitle = findViewById(R.id.titleText);
        tv_SongArtist = findViewById(R.id.artistText);
        tv_SongURL = findViewById(R.id.urlText);
        play.setEnabled(false);
        pause.setEnabled(false);
        mp3SeekBar.setEnabled(false);
        request.getBackground().setColorFilter(Color.parseColor("#FF8BC34A"), PorterDuff.Mode.MULTIPLY);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_status.setText("Requesting a song from CTower");
                request.getBackground().setColorFilter(null);
                request.setEnabled(false);
                pause.getBackground().setColorFilter(null);
                pause.setEnabled(false);
                play.setEnabled(false);
                play.getBackground().setColorFilter(null);
                requestSong();
            }
        });

        mp3player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp3player.stop();
                request.callOnClick();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (mp3player.isPlaying()) {
                    pause.setEnabled(false);
                    pause.getBackground().setColorFilter(null);
                    mp3player.pause();
                    tv_status.setText("Stopped");
                    play.setEnabled(true);
                    play.getBackground().setColorFilter(Color.parseColor("#FF00B8D4"), PorterDuff.Mode.MULTIPLY);
                    mplayerHandler.removeCallbacks(progressBarRunnable);
                    runnableRunning = false;
                //}
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_status.setText(statusPlaying);
                mp3player.start();
                pause.setEnabled(true);
                pause.getBackground().setColorFilter(Color.parseColor("#FFD50000"), PorterDuff.Mode.MULTIPLY);
                play.setEnabled(false);
                play.getBackground().setColorFilter(null);
                if (!runnableRunning)
                    progressBarRunnable.run();
            }
        });
    }

    private void requestSong() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document songInfo = Jsoup.connect(getSong).get();
                    Elements statusGetSong = songInfo.getElementsByTag("status");
                    statusSong = statusGetSong.text();
                    Elements getSongTitle = songInfo.getElementsByTag("title");
                    songTitle = getSongTitle.text();
                    Elements getSongArtist = songInfo.getElementsByTag("artist");
                    songArtist = getSongArtist.text();
                    Elements getSongURL = songInfo.getElementsByTag("url");
                    mp3URL = getSongURL.text();
                } catch (Exception e) {
                    msgStringSong = "Error : " + e.getMessage() + "\n";
                    Toast.makeText(JukeboxActivity.this, msgStringSong, Toast.LENGTH_SHORT).show();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mp3SeekBar.setEnabled(false);
                        if (statusSong.equals("2-OK")) {
                            pause.setEnabled(true);
                            pause.getBackground().setColorFilter(Color.parseColor("#FFD50000"), PorterDuff.Mode.MULTIPLY);
                            request.setEnabled(true);
                            request.getBackground().setColorFilter(Color.parseColor("#FF8BC34A"), PorterDuff.Mode.MULTIPLY);
                            tv_SongArtist.setText(songArtist);
                            tv_SongTitle.setText(songTitle);
                            tv_SongURL.setText(mp3URL);
                            tv_status.setText(statusPlaying);
                            if (mp3player.isPlaying()){
                                mp3player.stop();
                            }
                            try {
                                    mp3player.reset();
                                    mp3player.setDataSource(mp3URL);
                                    mp3player.prepareAsync();
                                    mp3player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp3player.start();
                                        //mp3SeekBar.setProgress(0);
                                        progressBarRunnable.run();
                                        runnableRunning = true;
                                        }
                                    });
                                } catch (Exception e) {
                                e.printStackTrace();
                                }
                            }
                        else {
                            Toast.makeText(JukeboxActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            Intent toLoginActivity = new Intent(JukeboxActivity.this, LoginActivity.class);
                            startActivity(toLoginActivity);
                        }
                    }
                });
            }
        }).start();
    }

    private Handler mplayerHandler = new Handler();
    private Runnable progressBarRunnable = new Runnable() {

        @Override
        public void run() {
            if(mp3player.isPlaying()) {
                mp3SeekBar.setEnabled(true);
                int songDuration = mp3player.getDuration();
                mp3SeekBar.setMax(songDuration);
                int songCurrentPosition = mp3player.getCurrentPosition();
                mp3SeekBar.setProgress(songCurrentPosition);
                mp3SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            mp3player.seekTo(progress);
                        }
                    }
                });
            }
            mplayerHandler.postDelayed(this, 1000);
            //System.out.println("still counting even after inactivity?");
        }
    };

    private void stopPlayer() {
        if (mp3player.isPlaying()) {
            mp3player.pause();
            mplayerHandler.removeCallbacks(progressBarRunnable);
            runnableRunning = false;
            tv_status.setText("Stopped");
            pause.getBackground().setColorFilter(null);
            pause.setEnabled(false);
            play.setEnabled(true);
            play.getBackground().setColorFilter(Color.parseColor("#FF00B8D4"), PorterDuff.Mode.MULTIPLY);
        }
    }
//stop playing when paused, stopped, or destroyed
    @Override
    protected void onPause() {
        super.onPause();
        stopPlayer();
    }
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}