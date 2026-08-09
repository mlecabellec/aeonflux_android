/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.4 - Video Playback Activity Implementation.
 */
package com.aeonflux.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;
import com.aeonflux.app.core.media.MediaPlayerEngine;
import com.aeonflux.app.core.media.PlayerEngine;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * [TSK-20260809-002.4] Activity providing state-of-the-art video stream rendering,
 * SurfaceView rendering, timeline seeking, jump buttons (-30s, -5s, +5s, +30s),
 * and speed selector chips (80%, 100%, 110%, 120%, 150%).
 */
@AndroidEntryPoint
public class VideoPlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final Logger LOGGER = Logger.getLogger(VideoPlaybackActivity.class.getName());

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_VIDEO_TITLE = "extra_video_title";

    private PlayerEngine playerEngine;
    private String videoUrl = "";
    private String videoTitle = "";

    private SurfaceView surfaceView;
    private SeekBar seekBar;
    private TextView textCurrentTime;
    private TextView textTotalTime;
    private Button btnPlayPause;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            updateProgressUi();
            uiHandler.postDelayed(this, 250);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("[TSK-20260809-002.4] Initializing VideoPlaybackActivity.");

        try {
            setContentView(R.layout.activity_video_playback);

            Intent intent = getIntent();
            if (intent != null) {
                videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) != null ? intent.getStringExtra(EXTRA_VIDEO_URL) : "";
                videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) != null ? intent.getStringExtra(EXTRA_VIDEO_TITLE) : "Video Stream";
            }

            TextView titleView = findViewById(R.id.text_video_title);
            TextView urlView = findViewById(R.id.text_video_url);
            if (titleView != null) titleView.setText(videoTitle);
            if (urlView != null) urlView.setText(videoUrl);

            surfaceView = findViewById(R.id.surface_video);
            if (surfaceView != null) {
                surfaceView.getHolder().addCallback(this);
            }

            setupPlaybackControls();

            com.aeonflux.app.core.media.MediaNotificationHelper.requestNotificationPermission(this);

            playerEngine = new MediaPlayerEngine();
            playerEngine.setListener(new PlayerEngine.PlayerListener() {
                @Override
                public void onPrepared() {
                    LOGGER.info("[TSK-20260809-002.4] Video player prepared. Starting playback.");
                    playerEngine.play();
                    updatePlayPauseButtonLabel();
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                            VideoPlaybackActivity.this,
                            videoTitle,
                            videoUrl,
                            false,
                            true
                        );
                    }
                    uiHandler.post(progressUpdater);
                }

                @Override
                public void onCompletion() {
                    LOGGER.info("[TSK-20260809-002.4] Video playback completed.");
                    updatePlayPauseButtonLabel();
                }

                @Override
                public void onError(int what, int extra, @NonNull String message) {
                    LOGGER.warning("[TSK-20260809-002.4] Video playback error: " + message);
                    Toast.makeText(VideoPlaybackActivity.this, "Error playing video stream", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBufferingUpdate(int percent) {
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.4] Exception initializing VideoPlaybackActivity.", e);
        }
    }

    private void setupPlaybackControls() {
        try {
            seekBar = findViewById(R.id.seekbar_video_timeline);
            textCurrentTime = findViewById(R.id.text_video_current_time);
            textTotalTime = findViewById(R.id.text_video_total_time);
            btnPlayPause = findViewById(R.id.btn_video_play_pause);

            if (seekBar != null) {
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && playerEngine != null) {
                            playerEngine.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }

            if (btnPlayPause != null) {
                btnPlayPause.setOnClickListener(v -> togglePlayPause());
            }

            Button btnBack30 = findViewById(R.id.btn_video_jump_back_30);
            Button btnBack5 = findViewById(R.id.btn_video_jump_back_5);
            Button btnFwd5 = findViewById(R.id.btn_video_jump_forward_5);
            Button btnFwd30 = findViewById(R.id.btn_video_jump_forward_30);

            if (btnBack30 != null) btnBack30.setOnClickListener(v -> seekRelative(-30000));
            if (btnBack5 != null) btnBack5.setOnClickListener(v -> seekRelative(-5000));
            if (btnFwd5 != null) btnFwd5.setOnClickListener(v -> seekRelative(5000));
            if (btnFwd30 != null) btnFwd30.setOnClickListener(v -> seekRelative(30000));

            Button btnSpeed80 = findViewById(R.id.btn_video_speed_80);
            Button btnSpeed100 = findViewById(R.id.btn_video_speed_100);
            Button btnSpeed110 = findViewById(R.id.btn_video_speed_110);
            Button btnSpeed120 = findViewById(R.id.btn_video_speed_120);
            Button btnSpeed150 = findViewById(R.id.btn_video_speed_150);

            if (btnSpeed80 != null) btnSpeed80.setOnClickListener(v -> setPlaybackSpeed(0.8f));
            if (btnSpeed100 != null) btnSpeed100.setOnClickListener(v -> setPlaybackSpeed(1.0f));
            if (btnSpeed110 != null) btnSpeed110.setOnClickListener(v -> setPlaybackSpeed(1.1f));
            if (btnSpeed120 != null) btnSpeed120.setOnClickListener(v -> setPlaybackSpeed(1.2f));
            if (btnSpeed150 != null) btnSpeed150.setOnClickListener(v -> setPlaybackSpeed(1.5f));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.4] Exception setting up video controls.", e);
        }
    }

    private void togglePlayPause() {
        try {
            if (playerEngine != null) {
                if (playerEngine.isPlaying()) {
                    playerEngine.pause();
                } else {
                    playerEngine.play();
                }
                updatePlayPauseButtonLabel();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.4] Exception toggling play/pause.", e);
        }
    }

    private void seekRelative(long deltaMs) {
        try {
            if (playerEngine != null) {
                long target = playerEngine.getCurrentPosition() + deltaMs;
                playerEngine.seekTo(target);
                LOGGER.fine("[TSK-20260809-002.4] Relative seek by " + deltaMs + " ms to " + target + " ms");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.4] Exception in relative seek.", e);
        }
    }

    private void setPlaybackSpeed(float speed) {
        try {
            if (playerEngine != null) {
                playerEngine.setSpeed(speed);
                Toast.makeText(this, "Playback speed: " + (int)(speed * 100) + "%", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.4] Exception setting playback speed.", e);
        }
    }

    private void updateProgressUi() {
        try {
            if (playerEngine != null) {
                long current = playerEngine.getCurrentPosition();
                long total = playerEngine.getDuration();

                if (seekBar != null) {
                    seekBar.setMax((int) total);
                    seekBar.setProgress((int) current);
                }

                if (textCurrentTime != null) textCurrentTime.setText(formatTime(current));
                if (textTotalTime != null) textTotalTime.setText(formatTime(total));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.4] Exception updating progress UI.", e);
        }
    }

    private final android.content.BroadcastReceiver stopReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent != null && com.aeonflux.app.core.media.MediaNotificationHelper.ACTION_STOP_MEDIA.equals(intent.getAction())) {
                LOGGER.info("[TSK-20260809-002.8] BroadcastReceiver received ACTION_STOP_MEDIA. Stopping video player.");
                if (playerEngine != null) {
                    playerEngine.release();
                }
                com.aeonflux.app.core.media.MediaNotificationHelper.cancelNotification(VideoPlaybackActivity.this);
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        try {
            android.content.IntentFilter filter = new android.content.IntentFilter(com.aeonflux.app.core.media.MediaNotificationHelper.ACTION_STOP_MEDIA);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(stopReceiver, filter, RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(stopReceiver, filter);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.8] Exception registering stopReceiver", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(stopReceiver);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.8] Exception unregistering stopReceiver", e);
        }

        if (playerEngine != null && playerEngine.isPlaying()) {
            com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                this,
                videoTitle,
                videoUrl,
                false,
                true
            );
        }
    }

    private void updatePlayPauseButtonLabel() {
        if (btnPlayPause != null && playerEngine != null) {
            boolean playing = playerEngine.isPlaying();
            btnPlayPause.setText(playing ? "⏸ Pause" : "▶ Play");
            if (videoUrl != null && !videoUrl.isEmpty()) {
                com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                    this,
                    videoTitle,
                    videoUrl,
                    false,
                    playing
                );
            }
        }
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        LOGGER.fine("[TSK-20260809-002.4] Surface created.");
        try {
            if (playerEngine != null) {
                playerEngine.setSurfaceHolder(holder);
                if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                    playerEngine.prepare(this, Uri.parse(videoUrl));
                } else {
                    Toast.makeText(this, "No valid video URL provided", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.4] Exception in surfaceCreated prepare.", e);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        LOGGER.fine("[TSK-20260809-002.4] Surface destroyed.");
        if (playerEngine != null) {
            playerEngine.setSurfaceHolder(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(progressUpdater);
        com.aeonflux.app.core.media.MediaNotificationHelper.cancelNotification(this);
        if (playerEngine != null) {
            playerEngine.release();
            playerEngine = null;
        }
    }
}
