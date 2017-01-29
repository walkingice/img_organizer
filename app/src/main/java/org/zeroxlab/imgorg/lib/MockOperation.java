package org.zeroxlab.imgorg.lib;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MockOperation implements Operation {

    private final static SimpleDateFormat sSDF = new SimpleDateFormat("yyyy-MM");

    private Media mMedia;
    private String mDest;
    private boolean mFinished = false;

    MockOperation(Media src, String dest) {
        mMedia = src;
        Date d = src.date == null ? new Date() : src.date;
        String dstDate = sSDF.format(d);
        mDest = dest + "/" + dstDate;
    }

    @Override
    public void consume(Context ctx) {
        mFinished = true;
        Log.d("Mock", "consumed operation");
    }

    @Override
    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public String getSource() {
        return mMedia.data;
    }

    @Override
    public String getDestination() {
        return mDest;
    }
}
