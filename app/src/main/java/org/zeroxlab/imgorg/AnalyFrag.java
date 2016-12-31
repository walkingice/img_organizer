// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.zeroxlab.imgorg.lib.Organizer;
import org.zeroxlab.imgorg.lib.Organizer.Operation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AnalyFrag extends Fragment implements View.OnClickListener {

    private Resources mRes;

    private ListView mResults;
    private Button mOrganize;
    private List<Map<String, Object>> mPending;
    private List<Map<String, Object>> mRemoved;
    private BaseAdapter mAdapter;

    private int mMax = Integer.parseInt(ImgOrg.DEF_MAX);
    private boolean mHandleVideo = ImgOrg.DEF_HANDLE_VIDEO;
    private File mDirFrom;
    private File mDirTo;

    private final static String KEY_OPERATION = "get_organizer_operation";
    private final static String KEY_PATH_FROM = "get_path_from";
    private final static String KEY_PATH_TO = "get_path_to";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mRes = getResources();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPending = new LinkedList<>();
        mRemoved = new ArrayList<>();
        View root = getView();
        mResults = (ListView) root.findViewById(R.id.analy_results);
        mResults.setAdapter(mAdapter);
        mOrganize = (Button) root.findViewById(R.id.btn_organize);
        mOrganize.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        readPreferences();
        createOptions();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analy, container, false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_organize) {
            consumeOperations();
        }
    }

    private void readPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String keyMax = mRes.getString(R.string.key_maximum);
        String keyHandleVideo = mRes.getString(R.string.key_handle_video);
        String keyFrom = mRes.getString(R.string.key_from_dir);
        String keyTo = mRes.getString(R.string.key_to_dir);

        mMax = Integer.parseInt(prefs.getString(keyMax, mMax + ""));
        mHandleVideo = prefs.getBoolean(keyHandleVideo, ImgOrg.DEF_HANDLE_VIDEO);
        String from = prefs.getString(keyFrom, ImgOrg.DEF_FROM.getPath());
        String to = prefs.getString(keyTo, ImgOrg.DEF_TO.getPath());
        mDirFrom = new File(from);
        mDirTo = new File(to);
    }

    private void createOptions() {
        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Parsing...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<Object, Observable<File>>() {
                    @Override
                    public Observable<File> call(Object o) {
                        try {
                            File[] files = Organizer.findMedias(mDirFrom, mMax, mHandleVideo);
                            dialog.setMax(files.length);
                            return Observable.from(files);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .flatMap(new Func1<File, Observable<Map<String, Object>>>() {
                    @Override
                    public Observable<Map<String, Object>> call(File file) {
                        Organizer.Operation op = Organizer.createOp(file, mDirTo, "");
                        Map<String, Object> map = new HashMap<>();
                        map.put(KEY_OPERATION, op);
                        map.put(KEY_PATH_FROM, op.getPathFrom());
                        map.put(KEY_PATH_TO, op.getPathTo());
                        return Observable.just(map);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        mAdapter = new SimpleAdapter(getActivity(),
                                mPending,
                                android.R.layout.simple_list_item_2,
                                new String[]{KEY_PATH_FROM, KEY_PATH_TO},
                                new int[]{android.R.id.text1, android.R.id.text2});
                        mResults.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        dialog.cancel();
                        Log.d(ImgOrg.TAG, "Done");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dialog.cancel();
                    }

                    @Override
                    public void onNext(Map<String, Object> map) {
                        mPending.add(map);
                        dialog.setProgress(mPending.size());
                    }
                });
    }

    private void consumeOperations() {
        if (mPending.size() == 0) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Moving...");
        dialog.setCancelable(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        Subscription subscription = Observable.just(mPending.size())
                .observeOn(Schedulers.newThread())
                .concatMap(new Func1<Integer, Observable<Map<String, Object>>>() {
                    @Override
                    public Observable<Map<String, Object>> call(Integer size) {
                        dialog.setMax(size);
                        return Observable.from(mPending);
                    }
                })
                .concatMap(new Func1<Map<String, Object>, Observable<Map<String, Object>>>() {
                    @Override
                    public Observable<Map<String, Object>> call(Map<String, Object> map) {
                        Operation op = (Operation) map.get(KEY_OPERATION);
                        op.consume();
                        return Observable.just(map)
                                .delay(300, TimeUnit.MILLISECONDS); // just for testing
                    }
                })
                //.delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        dialog.cancel();
                        onUpdateOperations();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dialog.cancel();
                    }

                    @Override
                    public void onNext(Map<String, Object> map) {
                        mRemoved.add(map);
                        dialog.setProgress(mRemoved.size());
                    }
                });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                subscription.unsubscribe();
            }
        });
        dialog.show();
    }

    private void onUpdateOperations() {
        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        String[] toScanPaths = getStringPaths(mRemoved, KEY_PATH_TO);

        // scan destination path to DB
        MediaScannerConnection.scanFile(this.getActivity(), toScanPaths, null, null);

        /* FIXME: the below code which be comment out, it breaks my sdcard storage and
         * my system format my sdcar
         */
        // remove source path from DB
        //String[] toRemovePaths = getStringPaths(mRemoved, KEY_PATH_FROM);
        //try {
        //    ContentResolver resolver = this.getActivity().getContentResolver();
        //    ArrayList<ContentProviderOperation> options = new ArrayList<>(toRemovePaths.length);
        //    for (int i = 0; i < toRemovePaths.length; i++) {
        //        ContentProviderOperation op =
        //                ContentProviderOperation.newDelete(
        //                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //                        .withSelection(MediaStore.Images.Media.DATA + "=?",
        //                                new String[]{toRemovePaths[i]})
        //                        .build();
        //        options.add(op);
        //    }
        //    resolver.applyBatch(MediaStore.AUTHORITY, options);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        // update ListView
        for (Iterator<Map<String, Object>> i = mRemoved.iterator(); i.hasNext(); ) {
            mPending.remove(i.next());
        }
        mRemoved.clear();
        mAdapter.notifyDataSetChanged();
        dialog.cancel();
    }

    private String[] getStringPaths(List<Map<String, Object>> maps, String key) {
        List<String> paths = new ArrayList<>(maps.size());
        for (Iterator<Map<String, Object>> it = maps.iterator(); it.hasNext(); ) {
            Map<String, Object> map = it.next();
            Operation op = (Operation) map.get(KEY_OPERATION);
            if (op.isMoved()) {
                paths.add((String) map.get(key));
            }
        }
        String[] array = new String[paths.size()];
        paths.toArray(array);
        return array;
    }
}
