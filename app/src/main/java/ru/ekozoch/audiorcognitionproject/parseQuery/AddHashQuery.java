package ru.ekozoch.audiorcognitionproject.parseQuery;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

/**
 * Created by ekozoch on 25.03.15.
 */
public class AddHashQuery {
    public static void executeInBackGround(long hash, int hash_n, final FunctionCallback callback){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("music_hash", hash);
        params.put("line", hash_n);

        ParseCloud.callFunctionInBackground("addHashes", params, new FunctionCallback<Integer>() {
            public void done(Integer result, ParseException e) {
                callback.done(result, e);
            }
        });
    }
}
