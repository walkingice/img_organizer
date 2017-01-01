// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public final class Organizer {

    private final static String[] EXTS = new String[]{
            "gif", "png", "bmp", "jpg", "jpeg", "mp4"
    };

    private final static String[] EXTS_NO_VID = new String[]{
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
            throw new IOException(String.format("[%s] does not exists", file.getPath()));
        }
    }

    private final static void ensureDir(File dir) throws IOException {
        ensureExists(dir);
        if (!dir.isDirectory()) {
            throw new IOException(String.format("[%s] is not a directory", dir.getPath()));
        }
    }

    public final static List<Media> findMedias(Context ctx, String fromPath, int maximum, boolean video) throws IOException {
        return findFileMedias(fromPath, maximum, video);
    }

    private final static List<Media> findFileMedias(String fromPath, int maximum, boolean video) throws IOException {
        File dir = new File(fromPath);
        ensureDir(dir);
        // FIXME: currently, always ignore mp4 video files since I do not want to get EXIF from it
        File[] found = video ? dir.listFiles(FILTER) : dir.listFiles(FILTER_NO_VID);
        List<File> list = Arrays.asList(found);
        int max = Math.min(maximum, list.size());
        list.subList(0, max);
        final List<Media> medias = new ArrayList<>();
        Observable.from(list).subscribe(new Action1<File>() {
            @Override
            public void call(File file) {
                medias.add(new Media(file.getAbsolutePath()));
            }
        });

        return medias;
    }

    public final static Operation createOperation(Media media, String destPath) {
        return new FileOperation(media, destPath);
    }

    public final static void postOperation(Context ctx, List<Operation> ops) {
        if (!(ops.get(0) instanceof FileOperation)) {
            return;
        }

        String[] toScanPaths = getStringPaths(ops);

        // scan destination path to DB
        MediaScannerConnection.scanFile(ctx, toScanPaths, null, null);
    }

    private static String[] getStringPaths(List<Operation> ops) {
        List<String> paths = new ArrayList<>(ops.size());
        for (Operation op : ops) {
            if (op.isFinished()) {
                paths.add(op.getDestination());
            }
        }
        String[] array = new String[paths.size()];
        paths.toArray(array);
        return array;
    }
}
