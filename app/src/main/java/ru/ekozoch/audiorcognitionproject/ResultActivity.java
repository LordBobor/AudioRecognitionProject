package ru.ekozoch.audiorcognitionproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.ekozoch.audiorcognitionproject.parseClasses.Music;


public class ResultActivity extends ActionBarActivity {

    FloatingActionButton fab;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvArtist = (TextView) findViewById(R.id.tvArtist);
        TextView tvSong = (TextView) findViewById(R.id.tvSong);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        RestAdapter lastFMRestAdapter = new RestAdapter.Builder()
                .setEndpoint("http://ws.audioscrobbler.com")
                .build();

        LastFMService lastFMService = lastFMRestAdapter.create(LastFMService.class);

        final String artist = RecognitionActivity.recognizedMusicMap.get("artist");
        final String song = RecognitionActivity.recognizedMusicMap.get("song");

        tvArtist.setText(artist);
        tvSong.setText(song);

        lastFMService.listRepos(song, artist, new Callback<Response>() {
            public void success(Response response, retrofit.client.Response arg1) {

                try {
                    JSONObject obj = parseResponseToJSON(response);
                    Log.e("LAST FM", "recieved message:" + obj.toString());
                    JSONObject metaData = obj.getJSONObject("results").getJSONObject("trackmatches").getJSONObject("track");
                    Log.e("LAST FM", "song image is:" + metaData.getJSONArray("image").getJSONObject(3).getString("#text"));
                    new DownloadImageTask((ImageView) findViewById(R.id.ivAlbum))
                            .execute(metaData.getJSONArray("image").getJSONObject(3).getString("#text"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            public void failure(RetrofitError arg0) {
            }
        });

        lastFMService.getArtist(artist, new Callback<Response>() {
            public void success(Response response, retrofit.client.Response arg1) {

                try {
                    JSONObject obj = parseResponseToJSON(response);
                    Log.e("LAST FM", "recieved message:" + obj.toString());
                    JSONObject metaData = obj.getJSONObject("results").getJSONObject("artistmatches").getJSONObject("artist");
                    Log.e("LAST FM", "artist image is:" + metaData.getJSONArray("image").getJSONObject(3).getString("#text"));
                    new DownloadImageTask((ImageView) findViewById(R.id.ivArtist))
                            .execute(metaData.getJSONArray("image").getJSONObject(3).getString("#text"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            public void failure(RetrofitError arg0) {
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    fab.setShowProgressBackground(true);
                    fab.setIndeterminate(true);
                    fab.setProgress(1, true);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_18dp));

                    final TrackURLFinder finder = new TrackURLFinder(artist + " - " + song);
                    final Handler mHandler = new Handler();
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = finder.addUrlToLovedTrack();
                                try {

                                    playAudio(url);
                                    Runnable runnable = new Runnable() {
                                        public void run() {
                                            fab.setShowProgressBackground(false);
                                            fab.setIndeterminate(false);
                                            fab.setProgress(0, false);
                                        }
                                    };
                                    mHandler.post(runnable);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (SAXException | IOException | ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                } else {
                    isPlaying=false;
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_18dp));
                    fab.setShowProgressBackground(false);
                    fab.setIndeterminate(false);
                    killMediaPlayer();
                }
            }
        });





    }


    private MediaPlayer mediaPlayer;

    private void playAudio(String url) throws Exception
    {
        killMediaPlayer();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }
    private void killMediaPlayer() {
        if(mediaPlayer!=null) {
            try {
                mediaPlayer.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject parseResponseToJSON(Response response) throws JSONException {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(
                    response.getBody().in()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(sb.toString().replace("\\",""));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
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
