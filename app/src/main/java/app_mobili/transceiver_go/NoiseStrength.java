package app_mobili.transceiver_go;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

public class NoiseStrength extends Sensor {
    private static final int SAMPLE_RATE = 44100;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private final int bufferSize;
    private int silenceAmplitude = 0;
    private int clapAmplitude = 100;
    private int maxAmplitude = 0;
    private final int millis = 3000; // milliseconds of recording per measurement

    public interface RecordingListener {
        default void onRecordingFinished(int noise){
        }
    }

    private RecordingListener recordingListener;

    public void setRecordingListener(RecordingListener listener) {
        this.recordingListener = listener;
    }


    public NoiseStrength(Context context) {
        this.context = context;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    }

    public void startRecording() {
        if (isRecording) {
            return;
        }
        // Check for the RECORD_AUDIO permission at runtime
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the RECORD_AUDIO permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // Handle initialization error
            return;
        }

        isRecording = true;
        maxAmplitude = 0;

        audioRecord.startRecording();

        Handler handler = new Handler();
        handler.postDelayed(this::stopRecording, millis); // Record for x seconds
    }

    private void stopRecording() {
        if (!isRecording) {
            return;
        }

        calculateAverageAmplitude();

        calibrateTo100Scale();


        isRecording = false;

        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;


        if (this.recordingListener != null) {
            this.recordingListener.onRecordingFinished(this.SensorValue);
        }
    }

    private void calculateAverageAmplitude() {
        if (audioRecord != null) {
            short[] buffer = new short[bufferSize];
            int samplesRead = audioRecord.read(buffer, 0, bufferSize);
            if (samplesRead > 0) {
                int sumAmplitude = 0;
                for (int i = 0; i < samplesRead; i++) {
                    int amplitude = Math.abs(buffer[i]);
                    sumAmplitude += amplitude;
                }
                this.SensorValue = sumAmplitude / samplesRead;
            }
        }
    }

    private void calibrateTo100Scale() {
        double scale = ((double) SensorValue - silenceAmplitude) / clapAmplitude;
        scale *= 100;
        if (scale < 0) {
            scale = 0;
        }
        SensorValue = (int) scale;
    }
    private void calculateClapAmplitude() {
        if (audioRecord != null) {
            short[] buffer = new short[bufferSize];
            int bytesRead = audioRecord.read(buffer, 0, bufferSize);
            if (bytesRead > 0) {
                for (int i = 0; i < bytesRead; i++) {
                    int amplitude = Math.abs(buffer[i]);
                    if (amplitude > maxAmplitude) {
                        maxAmplitude = amplitude;
                    }
                }
            }
        }
        // adjusted with silence difference
        clapAmplitude = maxAmplitude - silenceAmplitude;
    }

    private void setClapFromRecording() {
        if (!isRecording) {
            return;
        }

        calculateClapAmplitude();

        isRecording = false;

        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;


    }

    private void setSilenceFromRecording() {
        if (!isRecording) {
            return;
        }

        isRecording = false;

        calculateAverageAmplitude();

        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;


        silenceAmplitude = SensorValue;
    }

    // starts a recording that upon ending calculates and stores the silenceAmplitude
    public void calibrateSilence() {

        if (isRecording) {
            return;
        }

        // Check for the RECORD_AUDIO permission at runtime
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the RECORD_AUDIO permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // Handle initialization error
            return;
        }

        isRecording = true;
        maxAmplitude = 0;

        audioRecord.startRecording();

        Handler handler = new Handler();
        handler.postDelayed(this::setSilenceFromRecording, millis); // Record for 3 seconds
    }

    public int getSilenceAmplitude() {
        return silenceAmplitude;
    }

    public int getClapAmplitude() {
        return clapAmplitude;
    }

    public int getNoiseScale() {
        return SensorValue;
    }

    public void calibrateClap() {
        if (isRecording) {
            return;
        }

        // Check for the RECORD_AUDIO permission at runtime
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the RECORD_AUDIO permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // Handle initialization error
            return;
        }

        isRecording = true;
        maxAmplitude = 0;

        audioRecord.startRecording();

        Handler handler = new Handler();
        handler.postDelayed(this::setClapFromRecording, millis); // Record for 3 seconds
    }
}
