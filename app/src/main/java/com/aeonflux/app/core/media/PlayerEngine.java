/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.2 - Strategy Pattern Player Engine Interface.
 */
package com.aeonflux.app.core.media;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * [TSK-20260809-002.2] Strategy Pattern abstraction for media playback engines.
 * Encapsulates audio/video stream playback, timeline seeking, variable speed control,
 * and surface attachment.
 */
public interface PlayerEngine {

    interface PlayerListener {
        void onPrepared();
        void onCompletion();
        void onError(int what, int extra, @NonNull String message);
        void onBufferingUpdate(int percent);
    }

    void prepare(@NonNull Context context, @NonNull Uri mediaUri) throws Exception;

    void setSurfaceHolder(@Nullable SurfaceHolder surfaceHolder);

    void setSurface(@Nullable Surface surface);

    void play();

    void pause();

    void seekTo(long positionMs);

    void setSpeed(float speedMultiplier);

    long getCurrentPosition();

    long getDuration();

    boolean isPlaying();

    void release();

    void setListener(@Nullable PlayerListener listener);
}
