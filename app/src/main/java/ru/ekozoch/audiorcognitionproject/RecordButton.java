package ru.ekozoch.audiorcognitionproject;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Splitter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ekozoch on 16.02.15.
 */
public class RecordButton extends Button {
    boolean mStartRecording = true;
    WebSocketClient client;
    private static final String LOG_TAG = "AudioRecordTest";

    public RecordButton(Context context) {
        super(context);
        setText("Start recording");
        setOnClickListener(clicker);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText("Start recording");
        setOnClickListener(clicker);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText("Start recording");
        setOnClickListener(clicker);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setText("Start recording");
        setOnClickListener(clicker);
    }

    private void setUpWebSocketClient(){
        final Thread thread = new Thread(new AudioRecordThread());
        List<BasicNameValuePair> extraHeaders = Collections.singletonList(
                new BasicNameValuePair("Cookie", "session=abcd")
        );
        client = new WebSocketClient(URI.create("ws://enigmatic-ravine-9497.herokuapp.com/client"), new WebSocketClient.Listener() {
            private boolean recognized;
            private String message;
            @Override
            public void onConnect() {
                Log.e("SOCKET SERVER", "Connected!");
                recognized = false;
                message = "";
                Looper.prepare();
                thread.start();
            }

            @Override
            public void onMessage(String message) {
                stopRecording();

                Log.e("SOCKET SERVER", "recieved message:" + message);
                thread.interrupt();
                recognized = true;
                this.message = message;
            }

            @Override
            public void onMessage(byte[] data) {

            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.e("SOCKET SERVER", "Disconnected: " + reason);
                if(recognized)
                    if(listener!=null) {
                        recognized = false;
                        listener.onRecognized(RecordButton.this, message);
                    }
            }

            @Override
            public void onError(Exception error) {
                error.printStackTrace();
            }
        }, extraHeaders);
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        isAudioRecording = true;
        setUpWebSocketClient();
        client.connect();
    }

    private void stopRecording() {
        isAudioRecording = false;
        mStartRecording = true;
    }


    private OnRecordFinishListener listener;

    public void setOnRecordFinishListener(OnRecordFinishListener listener) {
        this.listener = listener;
    }

    OnClickListener clicker = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onRecord(mStartRecording);
            if(listener!=null) listener.onRecognitionStart(RecordButton.this);
            mStartRecording = !mStartRecording;
        }
    };




    boolean isAudioRecording;
    class AudioRecordThread implements Runnable {
        final Handler mHandler = new Handler();
        @Override
        public void run() {
            int bufferLength = 0;
            int bufferSize;
            short[] audioData;

            try {
                bufferSize = AudioRecord.getMinBufferSize(44100,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize <= 2048) {
                    bufferLength = 2048;
                } else if (bufferSize <= 4096) {
                    bufferLength = 4096;
                }

                /* set audio recorder parameters, and start recording */
                final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferLength*8);
                audioData = new short[bufferLength];
                audioRecord.startRecording();
                Log.d(LOG_TAG, "audioRecord.startRecording()");

                List<Short> shorts = new ArrayList<>();


                FeatureExtractor extractor = new FeatureExtractor();
                /* ffmpeg_audio encoding loop */
                while (isAudioRecording) {
                    audioRecord.read(audioData, 0, audioData.length);

                    for (short tmp : audioData) {
                        shorts.add(tmp);
                        int hash = extractor.nextSignal(tmp);
                        if(hash == -1) continue;
                        //Log.e("HASH OBTAINED", hash + "");
                        client.send(ByteBuffer.allocate(4).putInt(hash).array());
                    }
                }

//
//                RecognitionActivity.audio = new Short[shorts.size()];
//                RecognitionActivity.audio = shorts.toArray(RecognitionActivity.audio);
//
//                if(listener!=null) listener.onRecognized(RecordButton.this, "success");

                Runnable runnable = new Runnable() {
                    public void run() {
                        /* encoding finish, release recorder */
                        if (audioRecord != null) {
                            try {
                                audioRecord.stop();
                                audioRecord.release();
                                stopRecording();
                                client.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                mHandler.post(runnable);
            } catch (Exception e) {
                Log.e(LOG_TAG, "get audio data failed:"+e.getMessage()+e.getCause()+e.toString());
            }

        }
    }



}
