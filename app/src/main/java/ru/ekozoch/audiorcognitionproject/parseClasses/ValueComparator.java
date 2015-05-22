package ru.ekozoch.audiorcognitionproject.parseClasses;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<Music>{

    Map<Music, Integer> base;
    public ValueComparator(Map<Music, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(Music a, Music b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
