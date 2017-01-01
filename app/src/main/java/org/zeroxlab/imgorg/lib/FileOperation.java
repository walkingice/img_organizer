// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FileOperation implements Operation {
    private final static SimpleDateFormat sSDF = new SimpleDateFormat("yyyy-MM");

    private boolean mPending;
    private boolean mMoved = false;

    private File mFrom;
    private File mTo;
    private String mContainerPath;

    private String mPathFrom = "";
    private String mPathTo = "";

    public FileOperation(Media media, String destDir) {
        this(new File(media.data), new File(destDir));
    }

    public FileOperation(File fromFile, File destDir) {
        mFrom = fromFile;
        mPathFrom = mFrom.getAbsolutePath();
        mContainerPath = destDir.getAbsolutePath();
        probe();
    }

    // true, if this file should be moved
    public boolean isPending() {
        return mPending;
    }

    public boolean isFinished() {
        return mMoved;
    }

    @Override
    public void consume() {
        if (!this.isPending()) {
            return;
        }

        File parent = mTo.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (!parent.isDirectory()) {
            return;
        }

        mFrom.renameTo(mTo);
        mMoved = true;
    }

    private boolean probe() {
        // to see its type and destination file existence.
        // if it is img or film, find its create date and create dest dir
        // use EXIF to create dest filename
        try {
            String time = AndroidExif.getDate(mFrom, sSDF);
            mTo = new File(String.format("%s/%s/%s", mContainerPath, time, mFrom.getName()));
            mPathTo = mTo.getAbsolutePath();
            mPending = !mTo.exists();
        } catch (IOException e) {
            mPending = false;
        } catch (ParseException e) {
            mPending = false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("from: " + mFrom.getPath());
        if (mTo != null) {
            sb.append(" to: " + mTo.getPath());
        }
        return sb.toString();
    }

    public String getSource() {
        return mPathFrom;
    }

    public String getDestination() {
        return mPathTo;
    }
}
