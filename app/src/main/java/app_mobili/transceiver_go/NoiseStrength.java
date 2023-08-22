package app_mobili.transceiver_go;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NoiseStrength extends Sensor {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private boolean isRecording = false;
    private final int bufferSize;
    private static final int DURATION_MS = 10; // 1 second duration for recording

    private int noise = -1;

    public NoiseStrength(Context context) {
        this.context = context;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    public int getNoiseLevel() {
        // Check for the RECORD_AUDIO permission at runtime
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the RECORD_AUDIO permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, measure the noise level
            startRecording();
            return noise;
        }
        return -1;
    }

    @SuppressLint("MissingPermission")
    private void startRecording() {
        if (!isRecording) {
            isRecording = true;
            short[] audioBuffer = new short[bufferSize];
            // This is checked before calling the function, no need to add it again
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
            audioRecord.startRecording();

            // Sleep for the duration of the recording (1 second in this case)
            try {
                Thread.sleep(DURATION_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Read the recorded audio data
            audioRecord.read(audioBuffer, 0, bufferSize);
            audioRecord.stop();
            audioRecord.release();
            isRecording = false;

            // Calculate the noise level based on the audio buffer
            calculateNoiseLevel(audioBuffer);
        }
    }

    private void calculateNoiseLevel(short[] audioBuffer) {
        // Calculate the RMS (Root Mean Square) of the audio buffer
        long sumSquared = 0;
        for (short value : audioBuffer) {
            sumSquared += value * value;
        }

        double rms = Math.sqrt(sumSquared / (double) audioBuffer.length);

        // Convert RMS to decibels (dB)
        double dB = 20 * Math.log10(rms / Short.MAX_VALUE);

        // Normalize the dB value to a range of 0 to 100 (or any other scale you prefer)
        noise = (int) Math.min(Math.max((dB + 100) / 2, 0), 100);
    }
}
