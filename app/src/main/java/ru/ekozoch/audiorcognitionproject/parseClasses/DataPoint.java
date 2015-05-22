package ru.ekozoch.audiorcognitionproject.parseClasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("DataPoint")
public class DataPoint extends ParseObject {

    public int getLine(){
        return getInt("line");
    }

    public Music getMusic(){
        return (Music) getParseObject("musicId");
    }

}
