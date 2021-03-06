package dms.org.musicplayer;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MusicHelper {
    public static MediaPlayer player;
    private static Runnable runnable;
    public static int songPos;
    public static boolean shuffleMode = false;

    //RepeatMode
    public static final int REPEAT_ALL = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_NONE = 2;
    public static int repeatValue = REPEAT_ALL;

    private static boolean firstTime = true;

    public static Equalizer equalizer;

    public static void initMediaPlayer() {

    }

    @SuppressLint("DefaultLocale")
    public static void listListener(SongListAdapter adapter, MainWindow activity) {
        adapter.set_OnItemClickListener(new SongListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LinearLayout b, View v, Music obj, int position) {
                songPos = position;
                activity.playStopBtn.setImageResource(R.drawable.pausebtn);
                activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.pausebtn);
                setPlayerStuff(obj, activity);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public static void setPlayerStuff(Music obj, MainWindow activity) {
        try
        {
            if(player != null) {
                if(player.isPlaying()) {
                    player.stop();
                }
                player.release();
            }
            player = new MediaPlayer();
            player.setDataSource(obj.getMusicData());
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                if(mp.isPlaying()){
                    mp.release();
                    mp.start();
                }
                else {
                    mp.start();

                    if(firstTime) {
                        firstTime = !firstTime;
                        mp.pause();
                    }
                    activity.setPlayerStuff(obj, player.getDuration());
                    activity.maxTime.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                            TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
                    ));
                    updateProgressBar(activity);
                }
            });
            player.setOnCompletionListener(mp -> {
                if(repeatValue == REPEAT_NONE && songPos == MainWindow.getSongsNumber()) {
                    player.stop();
                }
                else {
                    Music obj1 = MainWindow.nextSong();
                    setPlayerStuff(obj1, activity);
                }
            });

        }
        catch (IOException e) { e.getStackTrace(); }

    }

    public static void seekBarListener(MainWindow activity) {
        activity.bigPlayerSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int progress, boolean fromUser) {
                if(player != null && fromUser) {
                    String format = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                            TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition())));
                    seekBar.setIndicatorFormatter(format);
                    player.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                @SuppressLint("DefaultLocale") String format = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()))

                );
                activity.currentTime.setText(format);
                activity.bigPlayerSeekBar.setIndicatorFormatter(format);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private static void updateProgressBar(MainWindow activity) {
        activity.compactPlayerProgressBar.setProgress(player.getCurrentPosition());
        activity.bigPlayerSeekBar.setProgress(player.getCurrentPosition());

        String format = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()))

        );

        activity.currentTime.setText(format);
        activity.bigPlayerSeekBar.setIndicatorFormatter(format);

        if(player.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    updateProgressBar(activity);
                }
            };
            MainWindow.handler.postDelayed(runnable, 500);

        }
    }


    public static void playBtnStuff(MainWindow activity) {
        activity.playStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()) {
                    player.pause();
                    activity.playStopBtn.setImageResource(R.drawable.playbtn);
                    activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.playbtn);
                    MainWindow.editor.putInt("current_song_progress", player.getCurrentPosition());
                    MainWindow.editor.commit();
                }
                else {
                    player.start();
                    activity.playStopBtn.setImageResource(R.drawable.pausebtn);
                    activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.pausebtn);
                    updateProgressBar(activity);
                }
            }
        });

    }
    public static void bigPlayerButtons(MainWindow activity) {
        activity.bigPlayerPlayStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()) {
                    player.pause();
                    activity.playStopBtn.setImageResource(R.drawable.playbtn);
                    activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.playbtn);
                    MainWindow.editor.putInt("current_song_progress", player.getCurrentPosition());
                    MainWindow.editor.commit();
                }
                else {
                    if(player != null) {
                        player.start();
                        activity.playStopBtn.setImageResource(R.drawable.pausebtn);
                        activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.pausebtn);
                        updateProgressBar(activity);
                    }
                }
            }
        });
        activity.bigPlayerPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.playStopBtn.setImageResource(R.drawable.pausebtn);
                activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.pausebtn);
                if(player.getCurrentPosition() < 5000) {
                    Music obj = MainWindow.previousSong();
                    setPlayerStuff(obj, activity);
                }
                else {
                    player.seekTo(0);
                }
            }
        });
        activity.bigPlayerNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Music obj = MainWindow.nextSong();
                activity.playStopBtn.setImageResource(R.drawable.pausebtn);
                activity.bigPlayerPlayStopBtn.setImageResource(R.drawable.pausebtn);
                setPlayerStuff(obj, activity);
            }
        });
        activity.bigPlayerRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleMode = !shuffleMode;

                if(shuffleMode)
                    activity.bigPlayerRandom.setAlpha(1f);

                else
                    activity.bigPlayerRandom.setAlpha(0.6f);

                MainWindow.editor.putBoolean("current_shuffle_mode", shuffleMode);
            }
        });
        activity.bigPlayerRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(repeatValue) {
                    case REPEAT_ALL:
                        repeatValue = REPEAT_ONE;
                        activity.bigPlayerRepeat.setImageResource(R.drawable.repeatonebtn);
                        break;

                    case REPEAT_ONE:
                        repeatValue = REPEAT_NONE;
                        activity.bigPlayerRepeat.setImageResource(R.drawable.repeatbtn);
                        activity.bigPlayerRepeat.setAlpha(0.6f);
                        break;

                    case REPEAT_NONE:
                        repeatValue = REPEAT_ALL;
                        activity.bigPlayerRepeat.setAlpha(1f);
                        break;
                }
                MainWindow.editor.putInt("current_repeat_mode", repeatValue);
                MainWindow.editor.commit();
            }
        });
    }
    /*public static class ProgressThread extends Thread {
        @Override
        public void run() {
            if(player != null)
                MainWindow.compactPlayerProgressBar.setProgress(player.getCurrentPosition());
        }
    }*/
}
