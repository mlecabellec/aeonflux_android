/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.8 - Persistent Media Playback Notification Manager.
 */
package com.aeonflux.app.core.media;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.aeonflux.app.ui.AudioPlaybackActivity;
import com.aeonflux.app.ui.VideoPlaybackActivity;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260809-002.8] Helper managing persistent media playback notifications.
 * Displays ongoing playback status, stop action, and tap-to-reopen Intent returning
 * to AudioPlaybackActivity or VideoPlaybackActivity with full playback context.
 */
public class MediaNotificationHelper {

    private static final Logger LOGGER = Logger.getLogger(MediaNotificationHelper.class.getName());

    public static final String CHANNEL_ID = "media_playback_channel";
    public static final String CHANNEL_NAME = "Media Playback";
    public static final int NOTIFICATION_ID = 20268;

    public static final String ACTION_STOP_MEDIA = "com.aeonflux.app.ACTION_STOP_MEDIA";
    public static final String ACTION_TOGGLE_MEDIA = "com.aeonflux.app.ACTION_TOGGLE_MEDIA";

    /**
     * [TSK-20260809-002.8] Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+).
     */
    public static void requestNotificationPermission(@NonNull Activity activity) {
        Objects.requireNonNull(activity, "activity must not be null");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                LOGGER.info("[TSK-20260809-002.8] Requesting POST_NOTIFICATIONS runtime permission.");
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * [TSK-20260809-002.8] Build android.app.Notification object for media playback.
     */
    @NonNull
    public static Notification buildMediaNotification(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String mediaUrl,
            boolean isAudio,
            boolean isPlaying
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(mediaUrl, "mediaUrl must not be null");

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Ongoing media playback controls");
            nm.createNotificationChannel(channel);
        }

        Intent openIntent;
        if (isAudio) {
            openIntent = new Intent(context, AudioPlaybackActivity.class);
            openIntent.putExtra(AudioPlaybackActivity.EXTRA_AUDIO_URL, mediaUrl);
            openIntent.putExtra(AudioPlaybackActivity.EXTRA_AUDIO_TITLE, title);
        } else {
            openIntent = new Intent(context, VideoPlaybackActivity.class);
            openIntent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO_URL, mediaUrl);
            openIntent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO_TITLE, title);
        }
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, openIntent, flags);

        Intent stopIntent = new Intent(ACTION_STOP_MEDIA);
        stopIntent.setPackage(context.getPackageName());
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 1, stopIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(isPlaying ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause)
            .setContentTitle(title.isEmpty() ? "Media Playback" : title)
            .setContentText(isPlaying ? (isAudio ? "🎵 Playing audio stream..." : "🎬 Playing video stream...") : "⏸ Playback paused")
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Media", stopPendingIntent);

        return builder.build();
    }

    /**
     * [TSK-20260809-002.8] Display or update ongoing media playback notification.
     */
    public static void showMediaNotification(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String mediaUrl,
            boolean isAudio,
            boolean isPlaying
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(mediaUrl, "mediaUrl must not be null");

        try {
            LOGGER.info("[TSK-20260809-002.8] Displaying media notification for title: " + title + ", isAudio: " + isAudio + ", isPlaying: " + isPlaying);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            Notification notification = buildMediaNotification(context, title, mediaUrl, isAudio, isPlaying);
            nm.notify(NOTIFICATION_ID, notification);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.8] Exception creating media notification.", e);
        }
    }

    /**
     * [TSK-20260809-002.8] Cancel persistent media playback notification.
     */
    public static void cancelNotification(@NonNull Context context) {
        Objects.requireNonNull(context, "context must not be null");
        try {
            LOGGER.info("[TSK-20260809-002.8] Canceling media playback notification.");
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIFICATION_ID);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.8] Exception canceling notification.", e);
        }
    }
}
