// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg.lib;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;

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

    public final static List<Media> findMedias(Context ctx,
                                               String fromPath,
                                               int maximum,
                                               boolean video,
                                               boolean mockOption) throws IOException {
        //return findFileMedias(fromPath, maximum, video);
        if (mockOption) {
            return createMockMedias(ctx, maximum);
        } else {
            return findDbMedias(ctx, fromPath, maximum);
        }
    }

    private final static List<Media> createMockMedias(Context ctx, int maximum) {
        List<Media> list = new ArrayList<>();
        for (int i = 0; i < maximum; i++) {
            String filename = String.format("/sdcard/DCIM/Camera/%d.jpg", i);
            // start from 2017/01/25
            list.add(new Media(filename, 1485385400000l + i * 10000000));
        }
        return list;
    }

    private final static List<Media> findDbMedias(Context ctx, String fromPath, int maximum) {
        final String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN
        };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String CAMERA_IMAGE_BUCKET_NAME = fromPath;
        final String CAMERA_IMAGE_BUCKET_ID =
                getBucketId(CAMERA_IMAGE_BUCKET_NAME);
        final String[] selectionArgs = {CAMERA_IMAGE_BUCKET_ID};

        final Cursor cursor = ctx.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);
        List<Media> result = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            final int dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            do {
                final String data = cursor.getString(dataIdx);
                final Long date = cursor.getLong(dateIdx);
                result.add(new Media(data, date));
            } while (cursor.moveToNext());
        }
        cursor.close();

        int max = result.size() > maximum ? maximum : result.size();
        result = result.subList(0, max);
        return result;
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
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

    public final static Operation createOperation(Media media, String destPath, boolean mockOption) {
        //return new FileOperation(media, destPath);
        if (mockOption) {
            return new MockOperation(media, destPath);
        } else {
            return new StoreOperation(media, destPath);
        }
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
