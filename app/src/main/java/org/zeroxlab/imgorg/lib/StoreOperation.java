// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StoreOperation implements Operation {

    private final static SimpleDateFormat sSDF = new SimpleDateFormat("yyyy-MM");

    private String mSource;
    private String mDestination;
    private Date mDate;
    private boolean mFinished;

    StoreOperation(Media source, String dest) {
        mSource = source.data;
        mDate = source.date;
        Date d = mDate == null ? new Date() : mDate;
        String dstDate = sSDF.format(d);
        File dst = new File(dest + "/" + dstDate);
        mDestination = dst.getAbsolutePath();
    }

    @Override
    public void consume(Context ctx) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, mDestination);
        int rows = ctx.getContentResolver().update(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values,
                MediaStore.MediaColumns.DATA + "='" + mSource + "'",
                null
        );
        if (rows == 1) {
            File from = new File(mSource);
            File to = new File(mDestination);
            File parent = to.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            from.renameTo(to);
        }
        mFinished = true;
    }

    @Override
    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public String getSource() {
        return mSource;
    }

    @Override
    public String getDestination() {
        return mDestination;
    }
}
