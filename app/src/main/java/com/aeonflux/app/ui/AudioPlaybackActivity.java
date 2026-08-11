/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.3 - Audio Playback Activity Implementation.
 */
package com.aeonflux.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aeonflux.app.R;
import com.aeonflux.app.core.media.MediaPlayerEngine;
import com.aeonflux.app.core.media.PlayerEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * [TSK-20260809-002.3] Activity providing state-of-the-art audio stream playback,
 * timeline seeking, jump buttons (-30s, -5s, +5s, +30s), speed selector (80%, 100%, 110%, 120%, 150%),
 * and a dedicated Karaoke Speech-to-Text RecyclerView container.
 */
@AndroidEntryPoint
public class AudioPlaybackActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(AudioPlaybackActivity.class.getName());

    public static final String EXTRA_AUDIO_URL = "extra_audio_url";
    public static final String EXTRA_AUDIO_TITLE = "extra_audio_title";

    public static class AudioTranscriptLine {
        public final long startMs;
        public final long endMs;
        public final String text;

        public AudioTranscriptLine(long startMs, long endMs, @NonNull String text) {
            this.startMs = startMs;
            this.endMs = endMs;
            this.text = Objects.requireNonNull(text, "text must not be null");
        }
    }

    private PlayerEngine playerEngine;
    private String audioUrl = "";
    private String audioTitle = "";

    private SeekBar seekBar;
    private TextView textCurrentTime;
    private TextView textTotalTime;
    private Button btnPlayPause;
    private RecyclerView recyclerTranscript;
    private TranscriptAdapter transcriptAdapter;

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
        LOGGER.info("[TSK-20260809-002.3] Initializing AudioPlaybackActivity.");

        try {
            setContentView(R.layout.activity_audio_playback);
            setupPlaybackControls();
            setupKaraokeTranscriptView();

            Intent intent = getIntent();
            handleAudioIntent(intent);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.3] Exception initializing AudioPlaybackActivity.", e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        LOGGER.info("[ACTIVE-STREAM-ORCHESTRATOR] Received new audio intent. Re-orchestrating active audio stream.");
        handleAudioIntent(intent);
    }

    private void handleAudioIntent(@Nullable Intent intent) {
        if (intent != null) {
            this.audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL) != null ? intent.getStringExtra(EXTRA_AUDIO_URL) : "";
            this.audioTitle = intent.getStringExtra(EXTRA_AUDIO_TITLE) != null ? intent.getStringExtra(EXTRA_AUDIO_TITLE) : "Audio Stream";
        }

        com.aeonflux.app.core.media.MediaNotificationHelper.requestNotificationPermission(this);

        TextView titleView = findViewById(R.id.text_audio_title);
        TextView urlView = findViewById(R.id.text_audio_url);
        if (titleView != null) titleView.setText(audioTitle);
        if (urlView != null) urlView.setText(audioUrl);

        // NOTE: Do NOT call updateTranscriptDisplay() here — duration is not yet resolved.
        // Transcription is started inside onPrepared() once MediaPlayer has the real duration.
        startActiveAudioStream(this.audioUrl, this.audioTitle);
    }

    private void startActiveAudioStream(@NonNull String url, @NonNull String title) {
        try {
            if (playerEngine != null) {
                if (playerEngine.isPlaying()) {
                    playerEngine.pause();
                }
                playerEngine.release();
                playerEngine = null;
            }

            playerEngine = new MediaPlayerEngine();
            playerEngine.setListener(new PlayerEngine.PlayerListener() {
                @Override
                public void onPrepared() {
                    LOGGER.info("[ACTIVE-STREAM-ORCHESTRATOR] Active player prepared for: " + title + " (" + url + ")");
                    playerEngine.play();
                    updatePlayPauseButtonLabel();
                    // Duration is now resolved — safe to start Whisper STT chain.
                    updateTranscriptDisplay();
                    if (url != null && !url.isEmpty()) {
                        com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                            AudioPlaybackActivity.this,
                            title,
                            url,
                            true,
                            true
                        );
                    }
                    uiHandler.post(progressUpdater);
                }

                @Override
                public void onCompletion() {
                    LOGGER.info("[TSK-20260809-002.3] Playback completed for active stream.");
                    updatePlayPauseButtonLabel();
                }

                @Override
                public void onError(int what, int extra, @NonNull String message) {
                    LOGGER.warning("[TSK-20260809-002.3] Playback error: " + message);
                    Toast.makeText(AudioPlaybackActivity.this, "Error playing audio stream", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBufferingUpdate(int percent) {
                }
            });

            if (url != null && !url.trim().isEmpty()) {
                playerEngine.prepare(this, Uri.parse(url));
            } else {
                Toast.makeText(this, "No valid audio URL provided", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[ACTIVE-STREAM-ORCHESTRATOR] Exception starting active audio stream.", e);
        }
    }

    private void setupPlaybackControls() {
        try {
            seekBar = findViewById(R.id.seekbar_audio_timeline);
            textCurrentTime = findViewById(R.id.text_current_time);
            textTotalTime = findViewById(R.id.text_total_time);
            btnPlayPause = findViewById(R.id.btn_audio_play_pause);

            if (seekBar != null) {
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && playerEngine != null) {
                            fromUserSeeking = true;
                            playerEngine.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (fromUserSeeking && playerEngine != null && whisperEngine != null && whisperEngine.isEnabled()) {
                            fromUserSeeking = false;
                            long currentPos = playerEngine.getCurrentPosition();
                            whisperEngine.cancelCurrentTranscription();
                            updateTranscriptDisplay(currentPos);
                        }
                    }
                });
            }

            if (btnPlayPause != null) {
                btnPlayPause.setOnClickListener(v -> togglePlayPause());
            }

            Button btnBack30 = findViewById(R.id.btn_jump_back_30);
            Button btnBack5 = findViewById(R.id.btn_jump_back_5);
            Button btnFwd5 = findViewById(R.id.btn_jump_forward_5);
            Button btnFwd30 = findViewById(R.id.btn_jump_forward_30);

            if (btnBack30 != null) btnBack30.setOnClickListener(v -> seekRelative(-30000));
            if (btnBack5 != null) btnBack5.setOnClickListener(v -> seekRelative(-5000));
            if (btnFwd5 != null) btnFwd5.setOnClickListener(v -> seekRelative(5000));
            if (btnFwd30 != null) btnFwd30.setOnClickListener(v -> seekRelative(30000));

            Button btnSpeed80 = findViewById(R.id.btn_speed_80);
            Button btnSpeed100 = findViewById(R.id.btn_speed_100);
            Button btnSpeed110 = findViewById(R.id.btn_speed_110);
            Button btnSpeed120 = findViewById(R.id.btn_speed_120);
            Button btnSpeed150 = findViewById(R.id.btn_speed_150);

            if (btnSpeed80 != null) btnSpeed80.setOnClickListener(v -> setPlaybackSpeed(0.8f));
            if (btnSpeed100 != null) btnSpeed100.setOnClickListener(v -> setPlaybackSpeed(1.0f));
            if (btnSpeed110 != null) btnSpeed110.setOnClickListener(v -> setPlaybackSpeed(1.1f));
            if (btnSpeed120 != null) btnSpeed120.setOnClickListener(v -> setPlaybackSpeed(1.2f));
            if (btnSpeed150 != null) btnSpeed150.setOnClickListener(v -> setPlaybackSpeed(1.5f));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-002.3] Exception setting up playback controls.", e);
        }
    }

    private final com.aeonflux.app.core.media.whisper.WhisperTranscriptEngine whisperEngine = new com.aeonflux.app.core.media.whisper.WhisperTranscriptEngine();
    private Button btnToggleTranscription;

    private void setupKaraokeTranscriptView() {
        try {
            whisperEngine.initEngine(this);
            whisperEngine.setEnabled(true);
            btnToggleTranscription = findViewById(R.id.btn_toggle_transcription);
            recyclerTranscript = findViewById(R.id.recycler_audio_transcript);

            if (recyclerTranscript != null) {
                recyclerTranscript.setLayoutManager(new LinearLayoutManager(this));
                transcriptAdapter = new TranscriptAdapter(new ArrayList<>());
                recyclerTranscript.setAdapter(transcriptAdapter);
            }

            if (btnToggleTranscription != null) {
                btnToggleTranscription.setOnClickListener(v -> {
                    boolean newState = !whisperEngine.isEnabled();
                    whisperEngine.setEnabled(newState);

                    updateTranscriptionButtonUi();
                    updateTranscriptDisplay();
                    Toast.makeText(this, newState ? "OpenAI Whisper Base STT Enabled" : "OpenAI Whisper Base STT Disabled", Toast.LENGTH_SHORT).show();
                });
                updateTranscriptionButtonUi();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-003.3] Exception setting up OpenAI Whisper transcript view.", e);
        }
    }

    private void updateTranscriptionButtonUi() {
        if (btnToggleTranscription != null) {
            boolean enabled = whisperEngine.isEnabled();
            btnToggleTranscription.setText(enabled ? "🎤 Disable Whisper STT" : "🎤 Enable OpenAI Whisper STT");
            btnToggleTranscription.setBackgroundTintList(android.content.res.ColorStateList.valueOf(enabled ? android.graphics.Color.parseColor("#8B5CF6") : android.graphics.Color.parseColor("#64748B")));
        }
    }

    private final java.util.concurrent.ExecutorService bgExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    private boolean fromUserSeeking = false;

    private void updateTranscriptDisplay() {
        updateTranscriptDisplay(0L);
    }

    private void updateTranscriptDisplay(long startOffsetMs) {
        if (recyclerTranscript == null || transcriptAdapter == null) return;

        if (!whisperEngine.isEnabled()) {
            List<AudioTranscriptLine> disabledLines = new ArrayList<>();
            disabledLines.add(new AudioTranscriptLine(0, 86400000L, "Transcription is currently disabled. Tap 'Enable OpenAI Whisper STT' above to activate on-device speech-to-text."));
            transcriptAdapter.setLines(disabledLines);
            return;
        }

        long duration = playerEngine != null ? playerEngine.getDuration() : 300000L;
        String currentUrl = audioUrl;
        String currentTitle = audioTitle;

        List<AudioTranscriptLine> loadingLines = new ArrayList<>();
        loadingLines.add(new AudioTranscriptLine(0, duration > 0 ? duration : 300000L, "Transcription vocale OpenAI Whisper C++ en cours... Veuillez patienter pendant l'inférence."));
        transcriptAdapter.setLines(loadingLines);

        bgExecutor.execute(() -> {
            try {
                LOGGER.info("[BG-STT-EXEC] Running Whisper STT extraction on background thread from offset " + startOffsetMs + "ms...");
                whisperEngine.generateTranscriptForAudio(currentUrl, currentTitle, duration, startOffsetMs, (lines, decodedCount, totalCount) -> {
                    runOnUiThread(() -> {
                        if (transcriptAdapter != null && !lines.isEmpty()) {
                            transcriptAdapter.setLines(lines);
                        }
                    });
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[BG-STT-EXEC] Exception during background STT extraction.", e);
            }
        });
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
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.3] Exception toggling play/pause.", e);
        }
    }

    private void seekRelative(long deltaMs) {
        try {
            if (playerEngine != null) {
                long target = playerEngine.getCurrentPosition() + deltaMs;
                playerEngine.seekTo(target);
                if (whisperEngine != null && whisperEngine.isEnabled()) {
                    whisperEngine.cancelCurrentTranscription();
                    updateTranscriptDisplay(target);
                }
                LOGGER.fine("[TSK-20260809-002.3] Relative seek by " + deltaMs + " ms to " + target + " ms");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.3] Exception in relative seek.", e);
        }
    }

    private void setPlaybackSpeed(float speed) {
        try {
            if (playerEngine != null) {
                playerEngine.setSpeed(speed);
                Toast.makeText(this, "Playback speed: " + (int)(speed * 100) + "%", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-002.3] Exception setting playback speed.", e);
        }
    }

    private long lastLogTimestamp = 0L;

    private void updateProgressUi() {
        try {
            if (playerEngine != null) {
                long current = playerEngine.getCurrentPosition();
                long total = playerEngine.getDuration();
                boolean playing = playerEngine.isPlaying();

                if (seekBar != null) {
                    seekBar.setMax((int) total);
                    seekBar.setProgress((int) current);
                }

                if (textCurrentTime != null) textCurrentTime.setText(formatTime(current));
                if (textTotalTime != null) textTotalTime.setText(formatTime(total));

                if (transcriptAdapter != null) {
                    int activeIndex = transcriptAdapter.updateActivePosition(current);
                    if (activeIndex >= 0 && recyclerTranscript != null) {
                        recyclerTranscript.smoothScrollToPosition(activeIndex);
                    }
                }

                long now = System.currentTimeMillis();
                if (now - lastLogTimestamp >= 1000L) {
                    lastLogTimestamp = now;
                    String activeText = transcriptAdapter != null ? transcriptAdapter.getActiveLineText() : "N/A";
                    int activeIdx = transcriptAdapter != null ? transcriptAdapter.getActiveMasterIndex() : -1;
                    AudioTranscriptLine activeLine = transcriptAdapter != null ? transcriptAdapter.getActiveLine() : null;
                    long chunkStart = activeLine != null ? activeLine.startMs : -1L;
                    long chunkEnd = activeLine != null ? activeLine.endMs : -1L;

                    String realtimeDiag = String.format(
                        Locale.US,
                        "[REALTIME-WHISPER-DIAG] SoundPos: %d/%dms (%02d:%02d/%02d:%02d) | IsPlaying: %b | STT_Enabled: %b | ActiveChunkIdx: #%03d [%dms -> %dms] | TrackTitle: '%s' | StreamURL: '%s' | OutgoingSpeechText: '%s'",
                        current, total, (current / 60000L), ((current % 60000L) / 1000L), (total / 60000L), ((total % 60000L) / 1000L),
                        playing, whisperEngine.isEnabled(), activeIdx, chunkStart, chunkEnd,
                        (audioTitle != null ? audioTitle : "N/A"), (audioUrl != null ? audioUrl : "N/A"), activeText
                    );
                    android.util.Log.d("AeonFlux_STT_RealtimeDiag", realtimeDiag);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.3] Exception updating progress UI.", e);
        }
    }

    private boolean wasSttEnabledBeforeBackground = false;

    private final android.content.BroadcastReceiver stopReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent != null && com.aeonflux.app.core.media.MediaNotificationHelper.ACTION_STOP_MEDIA.equals(intent.getAction())) {
                LOGGER.info("[TSK-20260809-002.8] BroadcastReceiver received ACTION_STOP_MEDIA. Stopping audio player.");
                if (playerEngine != null) {
                    playerEngine.release();
                }
                com.aeonflux.app.core.media.MediaNotificationHelper.cancelNotification(AudioPlaybackActivity.this);
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        try {
            android.content.IntentFilter filter = new android.content.IntentFilter(com.aeonflux.app.core.media.MediaNotificationHelper.ACTION_STOP_MEDIA);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(stopReceiver, filter, RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(stopReceiver, filter);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[TSK-20260809-002.8] Exception registering stopReceiver", e);
        }

        if (wasSttEnabledBeforeBackground) {
            wasSttEnabledBeforeBackground = false;
            whisperEngine.setEnabled(true);
            updateTranscriptionButtonUi();
            updateTranscriptDisplay();
            LOGGER.info("[TSK-20260809-004.3] Automatically restored Whisper STT transcription on returning to foreground.");
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

        if (whisperEngine != null && whisperEngine.isEnabled()) {
            wasSttEnabledBeforeBackground = true;
            whisperEngine.setEnabled(false);
            updateTranscriptionButtonUi();
            updateTranscriptDisplay();
            LOGGER.info("[TSK-20260809-004.3] Automatically paused Whisper STT transcription on entering background.");
        }

        if (playerEngine != null && playerEngine.isPlaying()) {
            com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                this,
                audioTitle,
                audioUrl,
                true,
                true
            );
        }
    }

    private void updatePlayPauseButtonLabel() {
        if (btnPlayPause != null && playerEngine != null) {
            boolean playing = playerEngine.isPlaying();
            btnPlayPause.setText(playing ? "⏸ Pause" : "▶ Play");
            if (audioUrl != null && !audioUrl.isEmpty()) {
                com.aeonflux.app.core.media.MediaNotificationHelper.showMediaNotification(
                    this,
                    audioTitle,
                    audioUrl,
                    true,
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
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(progressUpdater);
        com.aeonflux.app.core.media.MediaNotificationHelper.cancelNotification(this);
        if (playerEngine != null) {
            playerEngine.release();
            playerEngine = null;
        }
        try {
            bgExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }

    private static class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {
        private static final int MAX_BUFFER_SIZE = 50;
        private final List<AudioTranscriptLine> masterLines = new ArrayList<>();
        private final List<AudioTranscriptLine> visibleLines = new ArrayList<>();
        private long activePositionMs = 0L;

        public TranscriptAdapter(@NonNull List<AudioTranscriptLine> initialLines) {
            setLines(initialLines);
        }

        public void setLines(@NonNull List<AudioTranscriptLine> newLines) {
            this.masterLines.clear();
            this.masterLines.addAll(newLines);
            updateSlidingWindow(0L);
        }

        public int updateActivePosition(long positionMs) {
            this.activePositionMs = positionMs;
            return updateSlidingWindow(positionMs);
        }

        private int updateSlidingWindow(long positionMs) {
            if (masterLines.isEmpty()) {
                visibleLines.clear();
                notifyDataSetChanged();
                return -1;
            }

            int activeMasterIdx = -1;
            for (int i = 0; i < masterLines.size(); i++) {
                AudioTranscriptLine line = masterLines.get(i);
                if (positionMs >= line.startMs && positionMs <= line.endMs) {
                    activeMasterIdx = i;
                    break;
                }
            }
            if (activeMasterIdx == -1) {
                activeMasterIdx = 0;
            }

            int startWindow = Math.max(0, activeMasterIdx - 10);
            int endWindow = Math.min(masterLines.size(), startWindow + MAX_BUFFER_SIZE);
            if (endWindow - startWindow < MAX_BUFFER_SIZE && startWindow > 0) {
                startWindow = Math.max(0, endWindow - MAX_BUFFER_SIZE);
            }

            visibleLines.clear();
            visibleLines.addAll(masterLines.subList(startWindow, endWindow));
            notifyDataSetChanged();

            int visibleActiveIdx = activeMasterIdx - startWindow;
            return (visibleActiveIdx >= 0 && visibleActiveIdx < visibleLines.size()) ? visibleActiveIdx : -1;
        }

        @NonNull
        public String getActiveLineText() {
            AudioTranscriptLine activeLine = getActiveLine();
            return activeLine != null ? activeLine.text : (visibleLines.isEmpty() ? "No lines" : visibleLines.get(0).text);
        }

        public int getActiveMasterIndex() {
            for (int i = 0; i < masterLines.size(); i++) {
                AudioTranscriptLine line = masterLines.get(i);
                if (activePositionMs >= line.startMs && activePositionMs <= line.endMs) {
                    return i;
                }
            }
            return masterLines.isEmpty() ? -1 : 0;
        }

        @Nullable
        public AudioTranscriptLine getActiveLine() {
            for (AudioTranscriptLine line : masterLines) {
                if (activePositionMs >= line.startMs && activePositionMs <= line.endMs) {
                    return line;
                }
            }
            return masterLines.isEmpty() ? null : masterLines.get(0);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transcript_line, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AudioTranscriptLine line = visibleLines.get(position);
            long seconds = (line.startMs / 1000) % 60;
            long minutes = (line.startMs / (1000 * 60)) % 60;
            holder.textTimestamp.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
            holder.textContent.setText(line.text);

            boolean isActive = activePositionMs >= line.startMs && activePositionMs <= line.endMs;
            holder.container.setBackgroundColor(isActive ? Color.parseColor("#E0F2FE") : Color.TRANSPARENT);
            holder.textContent.setTextColor(isActive ? Color.parseColor("#0284C7") : Color.BLACK);
        }

        @Override
        public int getItemCount() {
            return visibleLines.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View container;
            final TextView textTimestamp;
            final TextView textContent;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.container_transcript_line);
                textTimestamp = itemView.findViewById(R.id.text_timestamp);
                textContent = itemView.findViewById(R.id.text_line_content);
            }
        }
    }
}
