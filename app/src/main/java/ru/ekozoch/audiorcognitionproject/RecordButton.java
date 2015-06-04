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

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekozoch on 16.02.15.
 */
public class RecordButton extends Button {
    boolean mStartRecording = true;

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

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

    public void setFileName(String mFileName) {
        RecordButton.mFileName = mFileName;
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

        Thread thread = new Thread(new AudioRecordThread());
        thread.start();
//        mRecorder = new MediaRecorder();
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
//        mRecorder.setOutputFile(mFileName);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//
//        try {
//            mRecorder.prepare();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "prepare() failed");
//        }
//
//        mRecorder.start();
    }

    private void stopRecording() {
        isAudioRecording = false;
//        try{
//            mRecorder.stop();
//        }catch(RuntimeException stopException){
//            //handle cleanup here
//        }
//
//        mRecorder.release();
//        mRecorder = null;
//        if(listener!=null) listener.callback(this, "success");
    }


    private OnRecordFinishListener listener;

    public void setOnRecordFinishListener(OnRecordFinishListener listener) {
        this.listener = listener;
    }

    OnClickListener clicker = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onRecord(mStartRecording);
            if (mStartRecording) {
                setText("Stop recording");
            } else {
                RecognitionActivity.showProgress("Some preprocessing stuff");
                setText("Start recording");
            }
            mStartRecording = !mStartRecording;
        }
    };




    boolean isAudioRecording;
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            int bufferLength = 0;
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            Looper.prepare();
            final Handler mHandler = new Handler();

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
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferLength);
                audioData = new short[bufferLength];
                audioRecord.startRecording();
                Log.d(LOG_TAG, "audioRecord.startRecording()");

                List<Short> shorts = new ArrayList<>();

                /* ffmpeg_audio encoding loop */
                while (isAudioRecording) {
                    audioRecord.read(audioData, 0, audioData.length);
//                    int index;
//                    int iterations = audioData.length;

//                    ByteBuffer bb = ByteBuffer.allocate(audioData.length * 2);
//
//                    for(index = 0; index != iterations; ++index)
//                    {
//                        bb.putShort(audioData[index]);
//                    }
                    for (short tmp : audioData)
                    shorts.add(tmp);
                    //result = ArrayUtils.addAll(result, audioData);
                }

                RecognitionActivity.audio = new Short[shorts.size()];
                RecognitionActivity.audio = shorts.toArray(RecognitionActivity.audio);

                if(listener!=null) listener.callback(RecordButton.this, "success");

//                DataOutputStream dos = null;
//                try {
//                    dos = new DataOutputStream(new FileOutputStream(mFileName));
//                    for (short aFinalResult : finalResult) {
//                        dos.writeShort(aFinalResult);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        dos.close();
//                    } catch (IOException | NullPointerException e) {
//                        e.printStackTrace();
//                    }
//                }

                Runnable runnable = new Runnable() {
                    public void run() {
                        /* encoding finish, release recorder */
                        if (audioRecord != null) {
                            try {
                                audioRecord.stop();
                                audioRecord.release();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
//                        try {
//                            org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(mFileName), finalResult);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        if(listener!=null) listener.callback(RecordButton.this, "success");
                    }
                };
                Looper.prepare();
                mHandler.post(runnable);
            } catch (Exception e) {
                Log.e(LOG_TAG, "get audio data failed:"+e.getMessage()+e.getCause()+e.toString());
            }

        }
    }

}
