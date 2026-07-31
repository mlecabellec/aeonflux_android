package com.aeonflux.app.core.media;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlaybackService extends MediaLibraryService {
    private MediaLibrarySession mediaSession;
    private ExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        
        MediaLibrarySession.Callback callback = new MediaLibrarySession.Callback() {
            // Implement callbacks for media library navigation if needed
        };
        
        mediaSession = new MediaLibrarySession.Builder(this, player, callback)
            .build();
    }

    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            player.release();
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
}
