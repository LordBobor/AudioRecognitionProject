package ru.ekozoch.audiorcognitionproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ekozoch.audiorcognitionproject.parseClasses.DataPoint;
import ru.ekozoch.audiorcognitionproject.parseClasses.Music;
import ru.ekozoch.audiorcognitionproject.parseClasses.ValueComparator;
import ru.ekozoch.audiorcognitionproject.parseQuery.AddHashQuery;
import ru.ekozoch.audiorcognitionproject.parseQuery.GetHashQuery;
import ru.ekozoch.audiorcognitionproject.soundfile.CheapSoundFile;


public class RecognitionActivity extends ActionBarActivity {

    private static String mFileName = null;
    static Context context;
    private TextView resultText;
    CheapSoundFile file;

    Map<Music, Integer> musicMap;
    static TreeMap<Music,Integer> sortedMusicMap;
    static HashMap<Music, List<Integer>> timing;

    List<Long> hashes = new ArrayList<>();
    static Short[] audio =null;

    private static ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        context = this;

        RecordButton btnRecognize = (RecordButton) findViewById(R.id.btn_recognize);
        TextView btnRecognizeFile = (TextView) findViewById(R.id.btn_recognize_file);
        resultText = (TextView) findViewById(R.id.resultText);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        String title = "audiorecordtest.txt";
        mFileName += ("/" + title);

        musicMap = new HashMap<>();

        btnRecognize.setFileName(mFileName);
        btnRecognize.setOnRecordFinishListener(listener);

        btnRecognizeFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RecognitionActivity.this, "START", Toast.LENGTH_SHORT).show();

                //TODO select composition name
                collectMusicData("/storage/emulated/0/Music/Sonata Arctica - I Want Out.mp3");

            }
        });

//        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
//        metaRetriver.setDataSource(mFileName);

        //saveHashesRecursive();

    }

    private void collectMusicData(final String filename){
        Looper.prepare();
        final Handler mHandler = new Handler();
        new Thread() {
            public void run() {
                try {
                    file = CheapSoundFile.create(filename, new CheapSoundFile.ProgressListener() {
                        @Override
                        public boolean reportProgress(double fractionComplete) {
                            return true;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            afterSavingRingtone();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Looper.prepare();
                mHandler.post(runnable);
            }
        }.start();
    }


    private void afterSavingRingtone() throws IOException {
        final File outFile = new File(mFileName);
        int numFrames = file.getNumFrames();
        file.WriteFile(outFile, 0, numFrames);

        MusicRecognizer recognizer = new MusicRecognizer();
        recognizer.indexFile(mFileName);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public OnRecordFinishListener listener = new OnRecordFinishListener() {
        @Override
        public void callback(View view, String result) {
            MusicRecognizer recognizer = new MusicRecognizer();
            try {
                recognizer.indexFile(mFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    };


    private class MusicRecognizer{
        private int LOWER_LIMIT = 40;
        private int UPPER_LIMIT = 300;
        private int CHUNK_SIZE = 4096;


        String fw = "";

        public int[] RANGE = new int[] {80, 120, 180, UPPER_LIMIT+1};

        //тут лежит всякая работа с файлами, которая не важна для выполнения алгоритма.
        //После чего вызывается метод indexAudioFile()
        public void indexFile(final String filePath) throws IOException {
            fw = "";
//            final ByteArrayOutputStream out = new ByteArrayOutputStream();
//            final BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(filePath)));
            hideProgress();
            showProgress("Auidio converting to Hashes");
            indexAudioFile();

//            final Handler handler = new Handler();
//            new Thread() {
//                public void run() {
////                    int read;
////                    byte[] buff = new byte[CHUNK_SIZE];
////                    byte[] bytes = null;
////                    try {
////                        while ((read = in.read(buff)) > 0){
////                            out.write(buff, 0, read);
////                            out.flush();
////                            bytes = out.toByteArray();
////                            //audio = out.toByteArray();
////                        }
////
////                    } catch (IOException | NullPointerException e) {
////                        e.printStackTrace();
////                    }
//
//                    //readShortFromFile(filePath);
//
////                    final byte[] finalBytes = bytes;
//                    Runnable runnable = new Runnable() {
//                        public void run() {
//
////                            short[] shorts = new short[finalBytes.length/2];
////                            Log.e("lol", finalBytes.length + "");
////                            for (int i = 0; i < finalBytes.length/2; i++) {
////                                shorts[i] = (short)((finalBytes[2*i] << 8) | finalBytes[2*i + 1]);
////                            }
////                            audio = ArrayUtils.addAll(audio, shorts);
//
//                            hideProgress();
//                            indexAudioFile();
//
//                            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            mFileName += "/audioTest.txt";
//
//                            try {
//                                org.apache.commons.io.FileUtils.writeStringToFile(new File(mFileName), fw);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            Toast.makeText(RecognitionActivity.this, "DONE", Toast.LENGTH_SHORT).show();
//                        }
//                    };
//                    handler.post(runnable);
//                }
//            }.start();

        }

        private void readShortFromFile(String strFilePath) {
            List<Short> shorts = new ArrayList<>();
            try {
                FileInputStream fin = new FileInputStream(strFilePath);
                DataInputStream dis = new DataInputStream(fin);

                while (dis.available() > 0) {
                    // read two bytes from data input, return short
                    short k = dis.readShort();
                    shorts.add(k);
                }


                dis.close();
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!shorts.isEmpty()) {
                audio = new Short[shorts.size()];
                audio = shorts.toArray(audio);
            }
        }

        private void indexAudioFile(){
            final int totalSize = audio.length;

            final int amountPossible = totalSize / CHUNK_SIZE;

            //When turning into frequency domain we'll need complex numbers:
            Complex[][] results = new Complex[amountPossible][];

            for (int times = 0; times < amountPossible; times++) {
                Log.e("FFT on data chunk", times + "/"+ amountPossible);
                Complex[] complex = new Complex[CHUNK_SIZE];
                for (int i = 0; i < CHUNK_SIZE; i++) {
                    //Put the time domain data into a complex number with imaginary part as 0:
                    complex[i] = new Complex(audio[(times * CHUNK_SIZE) + i], 0);
                }
                //Perform FFT analysis on the chunk:
                FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
                results[times] = transformer.transform(complex, TransformType.FORWARD);
            }

            hashes = new ArrayList<>();
            for(int i = 0; i<amountPossible; i++) processLine(results[i]);

            try {
                org.apache.commons.io.FileUtils.write(new File(mFileName), fw);
            } catch (IOException e) {
                e.printStackTrace();
            }

            hideProgress();
            //TODO Chose one of the following
            match(amountPossible);
            //saveHashesRecursive();

        }

        public void processLine(Complex[] results){
            int AMOUNT_OF_POINTS = RANGE.length;
            int[] recordPoints = new int[AMOUNT_OF_POINTS];
            double[] highscores = new double[AMOUNT_OF_POINTS];


            //For every line of data:
            for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT-1; freq++) {
                //Get the magnitude:
                double mag = Math.log(results[freq].abs() + 1);
                //Find out which range we are in:
                int index = getIndex(freq);
                //Save the highest magnitude and corresponding frequency:
                if ( mag > highscores[index] ) {
                    highscores[index] = mag;
                    recordPoints[index] = freq;
                }
            }

            //Write the points to a file:
            String line = "";
            for (int i = 1; i < AMOUNT_OF_POINTS; i++) {
                line+=(recordPoints[i] + "\t");
            }
            long hash = hash(line);
            hashes.add(hash);
            fw+=(line + "\n");
        }

        //Find out in which range
        public int getIndex(int freq) {
            int i = 0;
            while(RANGE[i] < freq ) i++;
            return i;
        }

        private static final int FUZ_FACTOR = 3;
        private long hash(String line) {
            String[] p = line.split("\t");
            long p1 = Long.parseLong(p[0]);
            long p2 = Long.parseLong(p[1]);
            long p3 = Long.parseLong(p[2]);
            return  ((p3-(p3%FUZ_FACTOR)) * 1000000 + (p2-(p2%FUZ_FACTOR)) * 1000 + (p1-(p1%FUZ_FACTOR)));
        }

    }

    protected static void showProgress(String message) {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    protected void hideProgress() {
        if(pd!=null)
            pd.dismiss();
    }


    int total = 0;
    int matches = 0;
    private void match(final int amountPossible){
        showProgress("Matching Song");
        total = 0;
        matches = 0;
        musicMap.clear();

        timing = new HashMap<Music, List<Integer>>();
        for(Long hash : hashes) {
            final Long fHash = hash;
            GetHashQuery.executeInBackGround( hash, new FunctionCallback<HashMap>() {
                public void done(HashMap result, ParseException e) {
                    ++total;
                    if (e == null) {
                        matches++;

                        ArrayList<DataPoint> dataPoints = (ArrayList<DataPoint>) result.get("dataPoint");
                        ArrayList<Music> musics = (ArrayList<Music>) result.get("music");

                        for(int i = 0; i< musics.size(); ++i){
                            Music music = musics.get(i);
                            DataPoint point = dataPoints.get(i);

                            if(!timing.containsKey(music)) timing.put(music, new ArrayList<Integer>());
                            List<Integer> song_timings = timing.get(music);

                            if (musicMap.containsKey(music))
                                musicMap.put(music, (musicMap.get(music) + 1));
                            else musicMap.put(music, 1);
                            Log.e("HASH MATCHING", music.getSongArtist() + " - " + music.getSongName() + " " + point.getLine());
                            song_timings.add( point.getLine() - hashes.indexOf(fHash));
                            timing.put(music, song_timings);
                        }

                    } else {
                        Log.e("HASH MATCHING", e.getMessage() + " " + total + "/" + amountPossible);
                    }
                    if(total == amountPossible) {
                        hideProgress();
                        if (musicMap.size() > 0){
                            ValueComparator bvc = new ValueComparator(musicMap);
                            sortedMusicMap = new TreeMap<Music, Integer>(bvc);
                            sortedMusicMap.putAll(musicMap);

                            startActivity(new Intent(RecognitionActivity.this, ResultActivity.class));
                        }
                        else {
                            resultText.setText("No matches :(");
                        }
                    }
                }
            });
        }
    }

    private void saveHashesRecursive(){
        hash_n=0;
        showProgress("Saving hashes");
        saveHash();
    }

    int hash_n;
    private void saveHash(){

        if (hash_n == hashes.size()){
            hideProgress();
            return;
        }

        AddHashQuery.executeInBackGround(hashes.get(hash_n), hash_n, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                if (e == null) {
                    Log.e("HASH SAVED", result + " ");
                    hash_n++;
                    pd.setMessage("Saving hashes " + hash_n +"/"+hashes.size());
                    saveHash();
                } else {
                    Toast.makeText(RecognitionActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                    hideProgress();
                    e.printStackTrace();
                }
            }
        });
    }


}
