// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public final class Organizer {

    private final static String[] EXTS = new String[] {
        "gif", "png", "bmp", "jpg", "jpeg", "mp4"
    };

    private final static String[] EXTS_NO_VID = new String[] {
        "gif", "png", "bmp", "jpg", "jpeg"
    };

    private final static FilenameFilter FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTS) {
                if (name.toLowerCase().endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    private final static FilenameFilter FILTER_NO_VID = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTS_NO_VID) {
                if (name.toLowerCase().endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    private final static void ensureExists(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException(
                    String.format("[%s] does not exists",
                        file.getPath()));
        }
    }

    private final static void ensureDir(File dir) throws IOException {
        ensureExists(dir);
        if (!dir.isDirectory()) {
            throw new IOException(
                    String.format("[%s] is not a directory",
                        dir.getPath()));
        }
    }

    public final static File[] findMedias(File dir, int maximum, boolean video) throws IOException {
        ensureDir(dir);
        // FIXME: currently, always ignore mp4 video files since I do not want to get EXIF from it
        File[] found = video ? dir.listFiles(FILTER) : dir.listFiles(FILTER_NO_VID);
        if (found != null && found.length > maximum) {
            List<File> trim = Arrays.asList(found).subList(0, maximum);
            found = new File[trim.size()];
            found = trim.toArray(found);
        }
        return found;
    }

    public final static Operation createOp (File file, File toDir, String prefix) {
        Operation op = new Operation(file, toDir);
        return op;
    }

    //public final static void invoke(Operation op, boolean moveFilm) {
    //}

    public static class Operation {
        private final static SimpleDateFormat sSDF = new SimpleDateFormat("yyyy-MM");
        private boolean mIsImg;
        private boolean mIsFilm;
        private boolean mIsDestExists;
        private boolean mIsDup; // dest == from ? we use 'copy' but not 'move', so it might happens

        private boolean mPending;
        private boolean mMoved = false;

        private File mFrom;
        private File mTo;
        private String mContainerPath;

        private String mPathFrom = "";
        private String mPathTo = "";

        public Operation (File fromFile, File destDir) {
                mFrom = fromFile;
                mPathFrom = mFrom.getAbsolutePath();
                mContainerPath = destDir.getAbsolutePath();
                mIsImg = true;
                probe();
        }

        // true, if this file should be moved
        public boolean isPending() {
            return mPending;
        }

        public boolean isMoved() {
            return mMoved;
        }

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

        public String getPathFrom() {
            return mPathFrom;
        }

        public String getPathTo() {
            return mPathTo;
        }
    }
}
