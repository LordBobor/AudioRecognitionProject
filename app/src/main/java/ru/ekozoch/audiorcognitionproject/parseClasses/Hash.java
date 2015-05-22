package ru.ekozoch.audiorcognitionproject.parseClasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

@ParseClassName("Hash")
public class Hash extends ParseObject {

    public long getHash(){return getLong("hash");}

    public ParseRelation<DataPoint> getDataPointsRelation(){
        return getRelation("dataPoints");
    }

    public ParseQuery<DataPoint> getDataPointsQuery(){
        return getDataPointsRelation().getQuery();
    }

}
