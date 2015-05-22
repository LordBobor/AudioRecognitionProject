package ru.ekozoch.audiorcognitionproject.parseQuery;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class GetHashQuery {
    public static void executeInBackGround(long hash, final FunctionCallback callback){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("music_hash", hash);

        ParseCloud.callFunctionInBackground("matchHashes", params, new FunctionCallback<HashMap>() {
            public void done(HashMap result, ParseException e) {
                callback.done(result, e);
            }
        });
    }
}
