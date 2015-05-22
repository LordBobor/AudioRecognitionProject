package ru.ekozoch.audiorcognitionproject;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ru.ekozoch.audiorcognitionproject.parseClasses.Music;


public class ResultActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        List<Music> listMusic = new ArrayList<Music>(RecognitionActivity.sortedMusicMap.keySet());
        List<Integer> listValues = new ArrayList<Integer>(RecognitionActivity.sortedMusicMap.values());
        Log.e("ResultMap", RecognitionActivity.sortedMusicMap.toString());
        Log.e("MusicList", listMusic.toString());
        Log.e("ValuesList", listValues.toString());

        String[] data = new String[listMusic.size()];
        for(int i = 0; i<listMusic.size(); ++i){
            data[i] = listMusic.get(i).getSongArtist() + " - " + listMusic.get(i).getSongName()
                    + "\n" + listValues.get(i) + " matches";
        }

        ListView my_listview = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        my_listview.setAdapter(adapter);

//        resultText.setText("Composition\n" + listMusic.get(0).getSongArtist() + " - " + listMusic.get(0).getSongName()
//                + "\nwas found\nwith " + listValues.get(0) + " matches");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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
}
