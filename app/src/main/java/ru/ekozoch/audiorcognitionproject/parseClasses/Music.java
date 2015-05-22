package ru.ekozoch.audiorcognitionproject.parseClasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;


@ParseClassName("Music")
public class Music extends ParseObject implements Comparable<Music> {

    public String getSongName(){return getString("name");}

    public String getSongArtist(){return getString("artist");}

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (!(other instanceof Music)) return false;
        Music otherMusic = (Music) other;
        if (this.getObjectId().equals(otherMusic.getObjectId())) return true;
        else return false;
    }

    @Override
    public int compareTo(Music another) {
        return another.getObjectId().trim().compareTo(this.getObjectId().trim());
    }

}
