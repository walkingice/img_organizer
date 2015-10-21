package org.zeroxlab.imgorg;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.zeroxlab.imgorg.lib.AndroidExif;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExifTest extends InstrumentationTestCase {

    Context mContext;
    File withExif = null;
    File n5 = null;
    File f1 = null;
    File f2 = null;
    File f3 = null;
    File f4 = null;
    File f5 = null;
    File f6 = null;
    List<File> mOpened = new ArrayList<>();

    @Override
    protected void setUp() {
        mContext = getInstrumentation().getContext();
        withExif = copyFromAssetToTmp(mContext, "with_exif.jpg", "with_exif", "jpg");
        n5 = copyFromAssetToTmp(mContext, "DEF_Camera-N5.jpg", "n5_def_camera", "jpg");
        f1 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF.jpg", "fv1", "jpg");
        f2 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF_only.jpg", "fv2", "jpg");
        f3 = copyFromAssetToTmp(mContext, "FV5-N5-Test_EXIF_XMP.jpg", "fv3", "jpg");
        f4 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode1.jpg", "fv4", "jpg");
        f5 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode2.jpg", "fv5", "jpg");
        f6 = copyFromAssetToTmp(mContext, "FV5-N5-Test_MetaMode3.jpg", "fv6", "jpg");
        mOpened.add(withExif);
        mOpened.add(n5);
        mOpened.add(f1);
        mOpened.add(f2);
        mOpened.add(f3);
        mOpened.add(f4);
        mOpened.add(f5);
        mOpened.add(f6);
    }

    @Override
    protected void tearDown() {
        for (File opened : mOpened) {
            if (opened != null) {
                opened.delete();
            }
        }
        mOpened.clear();
    }

    public void testCorrectExif() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        try {
            String date = AndroidExif.getDate(withExif, format);
            assertEquals("2015-10", date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testCameraFV5Pics() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        try {
            // only these two mode could find EXIF data via Android API
            assertEquals("2015-11", AndroidExif.getDate(f4, format));
            assertEquals("2015-11", AndroidExif.getDate(f6, format));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* We want File object for testing. Copy data content from assets to tmp file */
    private File copyFromAssetToTmp(Context ctx, String assetPath, String tmpName, String suffix) {
        Resources res = ctx.getResources();
        AssetManager asset = res.getAssets();
        File tmp = null;
        try {
            File dir = ctx.getCacheDir();
            InputStream in = asset.open(assetPath);
            tmp = File.createTempFile(tmpName, suffix, dir);
            FileOutputStream out = new FileOutputStream(tmp);
            copy(in, out);
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        int size = 0x1000;
        byte[] buffer = new byte[size];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
}
