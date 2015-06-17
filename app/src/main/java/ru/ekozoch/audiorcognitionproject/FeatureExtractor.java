package ru.ekozoch.audiorcognitionproject;

import org.apache.commons.math3.complex.Complex;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by phoenix on 5/30/15.
 */
public class FeatureExtractor {

    STFT transformer;

    public FeatureExtractor() {
        transformer = new STFT(4096, 1024);
    }

    public int nextSignal(short next) {
        Complex[] result = transformer.nextSignal(next / (float)(1 << 16));
        if(result == null) return -1;
        int a = maxElement(result, 60, 100) / 2;
        int b = maxElement(result, 100, 140) / 2;
        int c = maxElement(result, 140, 200) / 2;
        int d = maxElement(result, 200, 320) / 2;
        return a | ((b | ((c | (d << 8)) << 8)) << 8);
    }

    int maxElement(Complex[] array, int begin, int end) {
        int res = 0;
        for(int i = 1; begin + i < end; ++i){
            if(array[begin + i].abs() > array[begin + res].abs()){
                res = i;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        DataInputStream is = null;
        try {
            is = new DataInputStream(new FileInputStream(args[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FeatureExtractor extractor = new FeatureExtractor();
        try {
            while (true) {
                short next = is.readShort();
                int hash = extractor.nextSignal(next);
                if(hash == -1) continue;
                System.out.println(hash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.flush();
        System.out.close();
    }

}
