// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import org.zeroxlab.imgorg.lib.Media;
import org.zeroxlab.imgorg.lib.Operation;
import org.zeroxlab.imgorg.lib.Organizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<Operation> mPending;
    private List<Operation> mRemoved;
    private BaseAdapter mAdapter;

    private int mMax = Integer.parseInt(ImgOrg.DEF_MAX);
    private boolean mHandleVideo = ImgOrg.DEF_HANDLE_VIDEO;
    private String mFromPath;
    private String mToPath;

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
        mFromPath = prefs.getString(keyFrom, ImgOrg.DEF_FROM.getPath());
        mToPath = prefs.getString(keyTo, ImgOrg.DEF_TO.getPath());
    }

    private void createOptions() {
        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Parsing...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<Object, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(Object o) {
                        try {
                            List<Media> medias = Organizer.findMedias(getActivity(), mFromPath, mMax, mHandleVideo);
                            dialog.setMax(medias.size());
                            return Observable.from(medias);
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .flatMap(new Func1<Media, Observable<Operation>>() {
                    @Override
                    public Observable<Operation> call(Media media) {
                        return Observable.just(Organizer.createOperation(media, mToPath));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Operation>() {
                    @Override
                    public void onCompleted() {
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (Operation op : mPending) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(KEY_PATH_FROM, op.getSource());
                            map.put(KEY_PATH_TO, op.getDestination());
                            list.add(map);
                        }

                        mAdapter = new SimpleAdapter(getActivity(),
                                list,
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
                    public void onNext(Operation op) {
                        mPending.add(op);
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
                .concatMap(new Func1<Integer, Observable<Operation>>() {
                    @Override
                    public Observable<Operation> call(Integer size) {
                        dialog.setMax(size);
                        return Observable.from(mPending);
                    }
                })
                .concatMap(new Func1<Operation, Observable<Operation>>() {
                    @Override
                    public Observable<Operation> call(Operation op) {
                        op.consume();
                        return Observable.just(op)
                                .delay(300, TimeUnit.MILLISECONDS); // just for testing
                    }
                })
                //.delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Operation>() {
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
                    public void onNext(Operation op) {
                        mRemoved.add(op);
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

        Organizer.postOperation(getActivity(), mRemoved);

        // update ListView
        for (Operation op : mRemoved) {
            mPending.remove(op);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Operation op : mPending) {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_PATH_FROM, op.getSource());
            map.put(KEY_PATH_TO, op.getDestination());
            list.add(map);
        }

        mRemoved.clear();
        mAdapter = new SimpleAdapter(getActivity(),
                list,
                android.R.layout.simple_list_item_2,
                new String[]{KEY_PATH_FROM, KEY_PATH_TO},
                new int[]{android.R.id.text1, android.R.id.text2});
        mResults.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        dialog.cancel();
    }
}
