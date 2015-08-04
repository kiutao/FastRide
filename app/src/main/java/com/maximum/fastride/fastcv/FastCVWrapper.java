package com.maximum.fastride.fastcv;

/**
 * Created by Oleg Kleiman on 05-Jul-15.
 */
public class FastCVWrapper {
//
//    static {
//        System.loadLibrary("fastcvUtils");
//    }

    public FastCVWrapper(){

    }

    public native void FrameTick();
    public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native void DetectFace(long matAddrRgba, String face_cascade_name);
    public native void Blur(long matAddrGr);
}
