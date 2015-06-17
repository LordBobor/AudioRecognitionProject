package ru.ekozoch.audiorcognitionproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ekozoch.audiorcognitionproject.soundfile.CheapSoundFile;


public class RecognitionActivity extends ActionBarActivity {

    static Context context;

    public static Map<String, String> recognizedMusicMap;

    private static ProgressDialog pd;
    private ProgressBar progressBar;
    RecordButton btnRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        context = this;

        btnRecognize = (RecordButton) findViewById(R.id.btn_recognize);
        btnRecognize.setOnRecordFinishListener(listener);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        recognizedMusicMap = new HashMap<>();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnRecognize.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public OnRecordFinishListener listener = new OnRecordFinishListener() {
        @Override
        public void onRecognized(View view, String message) {
            message = message.replace("{", "").replace("}", "").replace("\"", "");
            recognizedMusicMap = Splitter.on(", ").withKeyValueSeparator(": ").split(message);
            Log.e("SERVER ANSWER PARSED", "recieved message:" + recognizedMusicMap.toString());
            Intent intent = new Intent(RecognitionActivity.this, ResultActivity.class);
            intent.putExtra("SONG_NAME", recognizedMusicMap.get("artist") + "-" + recognizedMusicMap.get("song"));
            startActivityForResult(intent, 0);
        }

        @Override
        public void onRecognitionStart(View view) {
            btnRecognize.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRecognitionError(View view, String error) {
            btnRecognize.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onRecognitionEnd(View view) {
            
        }
    };


}
