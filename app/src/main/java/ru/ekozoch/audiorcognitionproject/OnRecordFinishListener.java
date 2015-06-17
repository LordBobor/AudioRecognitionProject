package ru.ekozoch.audiorcognitionproject;

import android.view.View;


public interface OnRecordFinishListener {
    void onRecognized(View view, String result);
    void onRecognitionStart(View view);
    void onRecognitionError(View view, String error);
    void onRecognitionEnd(View view);
}
