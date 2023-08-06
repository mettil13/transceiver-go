package app_mobili.transceiver_go;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SoundActivity extends AppCompatActivity {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private AudioRecord audioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        }
        else {
            // textview = give permit :>
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start capturing audio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            audioRecord.startRecording();
            // Start a background thread to read and process the audio data
            Thread audioThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    readAndPrintSoundLevels();
                }
            });
            audioThread.start();
        } else {
            TextView SoundView = findViewById(R.id.PressureView);
            SoundView.setText("no permission :(");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop capturing audio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED){
            audioRecord.stop();
            audioRecord.release();
        }
    }

    private void readAndPrintSoundLevels() {
        TextView SoundView = findViewById(R.id.PressureView);
        short[] audioBuffer = new short[BUFFER_SIZE];

        while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            // Read audio data from the buffer
            int numSamples = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);

            // Calculate the sound level
            double sumSquared = 0;
            for (int i = 0; i < numSamples; i++) {
                double sample = audioBuffer[i] / 32768.0;
                sumSquared += sample * sample;
            }
            double rms = Math.sqrt(sumSquared / numSamples);

            // Print the sound level information
            final double soundLevel = 20 * Math.log10(rms);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SoundView.setText("Sound Level: " + soundLevel + " dB");
                }
            });
        }
    }
}