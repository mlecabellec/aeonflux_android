/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.2 - Android MediaPlayer Engine Implementation.
 */
package com.aeonflux.app.core.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260809-002.2] Android MediaPlayer concrete implementation of PlayerEngine.
 * Enforces parameter defenses, variable speed control, surface binding, and exception protection.
 */
public class MediaPlayerEngine implements PlayerEngine {

    private static final Logger LOGGER = Logger.getLogger(MediaPlayerEngine.class.getName());

    private MediaPlayer mediaPlayer;
    private PlayerListener listener;
    private float currentSpeed = 1.0f;
    private boolean isPrepared = false;

    public MediaPlayerEngine() {
        LOGGER.fine("[TSK-20260809-002.2] Initializing MediaPlayerEngine instance.");
    }

    @Override
    public void prepare(@NonNull Context context, @NonNull Uri mediaUri) throws Exception {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(mediaUri, "mediaUri must not be null");

        LOGGER.info("[TSK-20260809-002.2] Preparing MediaPlayer for URI: " + mediaUri);
        release();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );
            mediaPlayer.setVolume(1.0f, 1.0f);

            mediaPlayer.setDataSource(context, mediaUri);

            mediaPlayer.setOnPreparedListener(mp -> {
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer prepared successfully.");
                isPrepared = true;
                try {
                    mp.setVolume(1.0f, 1.0f);
                } catch (Exception ignored) {}
                applySpeedInternal(currentSpeed);
                if (listener != null) {
                    listener.onPrepared();
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer playback completed.");
                if (listener != null) {
                    listener.onCompletion();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LOGGER.warning("[TSK-20260809-002.2] MediaPlayer error: what=" + what + ", extra=" + extra);
                isPrepared = false;
                if (listener != null) {
                    listener.onError(what, extra, "MediaPlayer playback error (what=" + what + ", extra=" + extra + ")");
                }
                return true;
            });

            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                if (listener != null) {
                    listener.onBufferingUpdate(percent);
                }
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.2] Exception in MediaPlayer prepare.", e);
            release();
            throw e;
        }
    }

    @Override
    public void setSurfaceHolder(@Nullable SurfaceHolder surfaceHolder) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setDisplay(surfaceHolder);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception setting SurfaceHolder.", e);
        }
    }

    @Override
    public void setSurface(@Nullable Surface surface) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setSurface(surface);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception setting Surface.", e);
        }
    }

    @Override
    public void play() {
        try {
            if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                applySpeedInternal(currentSpeed);
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer started playback.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception starting MediaPlayer.", e);
        }
    }

    @Override
    public void pause() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer paused playback.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception pausing MediaPlayer.", e);
        }
    }

    @Override
    public void seekTo(long positionMs) {
        try {
            if (mediaPlayer != null && isPrepared) {
                long clampedPos = Math.max(0L, Math.min(positionMs, getDuration()));
                mediaPlayer.seekTo((int) clampedPos);
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer seeked to: " + clampedPos + " ms");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception seeking MediaPlayer.", e);
        }
    }

    @Override
    public void setSpeed(float speedMultiplier) {
        if (speedMultiplier < 0.2f || speedMultiplier > 4.0f) {
            LOGGER.warning("[TSK-20260809-002.2] Invalid speed multiplier passed: " + speedMultiplier);
            return;
        }
        this.currentSpeed = speedMultiplier;
        applySpeedInternal(speedMultiplier);
    }

    private void applySpeedInternal(float speed) {
        try {
            if (mediaPlayer != null && isPrepared && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PlaybackParams params = mediaPlayer.getPlaybackParams();
                if (params == null) {
                    params = new PlaybackParams();
                }
                params.setSpeed(speed);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setPlaybackParams(params);
                } else {
                    mediaPlayer.setPlaybackParams(params);
                    mediaPlayer.pause();
                }
                LOGGER.fine("[TSK-20260809-002.2] Applied playback speed: " + speed);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.2] Exception setting PlaybackParams speed.", e);
        }
    }

    @Override
    public long getCurrentPosition() {
        try {
            if (mediaPlayer != null && isPrepared) {
                return mediaPlayer.getCurrentPosition();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.2] Exception getting current position.", e);
        }
        return 0L;
    }

    @Override
    public long getDuration() {
        try {
            if (mediaPlayer != null && isPrepared) {
                return mediaPlayer.getDuration();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.2] Exception getting duration.", e);
        }
        return 0L;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void release() {
        try {
            if (mediaPlayer != null) {
                isPrepared = false;
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                LOGGER.fine("[TSK-20260809-002.2] MediaPlayer released cleanly.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.2] Exception releasing MediaPlayer.", e);
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    @Override
    public void setListener(@Nullable PlayerListener listener) {
        this.listener = listener;
    }
}
