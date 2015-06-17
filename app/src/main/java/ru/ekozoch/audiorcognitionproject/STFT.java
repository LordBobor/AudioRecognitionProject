package ru.ekozoch.audiorcognitionproject;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * Created by phoenix on 5/30/15.
 */

public class STFT {
    int window;
    int stride;
    float[] toProcess;
    int toProcessLast;
    boolean processing;
    Complex[] in;
    float[] result;
    float[] windowingFunction;

    public STFT(int window, int stride) {
        this.window = window;
        this.stride = stride;
        assert (window & (window - 1)) == 0;
        assert window % stride == 0;
        toProcess = new float[window];
        toProcessLast = 0;
        processing = false;
        in = new Complex[window];
        result = new float[window];
        windowingFunction = new float[window];
        for(int i = 0; i < window; ++i){
            windowingFunction[i] = 0.5f * (float)(1. - Math.cos(2. * Math.PI * i / (window - 1)));
        }
    }

    public Complex[] nextSignal(float next) {
        toProcess[toProcessLast++] = next;
        if(toProcessLast == window){
            toProcessLast = 0;
            processing = true;
        }
        if(processing && (toProcessLast & (stride - 1)) == 0){
            for(int i = 0; i < window; ++i){
                in[i] = new Complex(toProcess[(toProcessLast + i) & (window - 1)] * windowingFunction[i], 0);
            }
            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
            return transformer.transform(in, TransformType.FORWARD);
            //return FFT.fft(in);
        }
        return null;
    }

}
