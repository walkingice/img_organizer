// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.zeroxlab.imgorg.lib.Media;
import org.zeroxlab.imgorg.lib.Operation;
import org.zeroxlab.imgorg.lib.Organizer;
import org.zeroxlab.imgorg.ui.ListItemPresenter;
import org.zeroxlab.imgorg.ui.SelectorAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private RecyclerView mResults;
    private TextView mFromDir;
    private Button mOrganize;
    private SelectorAdapter<Operation> mAdapter;

    private int mMax = Integer.parseInt(ImgOrg.DEF_MAX);
    private boolean mMockOption = false;
    private boolean mHandleVideo = ImgOrg.DEF_HANDLE_VIDEO;
    private String mFromPath;
    private String mToPath;

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
        View root = getView();

        mResults = (RecyclerView) root.findViewById(R.id.analy_results);
        mFromDir = (TextView) root.findViewById(R.id.analy_from_directory);
        mAdapter = new SelectorAdapter<>(new SelectorAdapter.PresenterSelector() {
            SelectorAdapter.Presenter presenter = new ListItemPresenter();

            @Override
            public SelectorAdapter.Presenter getPresenter(SelectorAdapter.Type type) {
                return presenter;
            }
        });

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
        String keyMockOption = mRes.getString(R.string.key_use_mock_operation);

        mMax = Integer.parseInt(prefs.getString(keyMax, mMax + ""));
        mMockOption = prefs.getBoolean(keyMockOption, false);
        mHandleVideo = prefs.getBoolean(keyHandleVideo, ImgOrg.DEF_HANDLE_VIDEO);
        mFromPath = prefs.getString(keyFrom, ImgOrg.DEF_FROM.getPath());
        mToPath = prefs.getString(keyTo, ImgOrg.DEF_TO.getPath());

        mFromDir.setText(mFromPath);
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
                            List<Media> medias = Organizer.findMedias(getActivity(),
                                    mFromPath,
                                    mMax,
                                    mHandleVideo,
                                    mMockOption);
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
                        return Observable.just(Organizer.createOperation(media, mToPath, mMockOption));
                    }
                })
                //.take(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Operation>() {
                    @Override
                    public void onCompleted() {
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
                        mAdapter.addItem(op, SelectorAdapter.Type.A);
                        dialog.setProgress(mAdapter.getItemCount());
                    }
                });
    }

    private void consumeOperations() {
        if (mAdapter.getItemCount() == 0) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Moving...");
        dialog.setCancelable(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        final Context ctx = getActivity();

        final int total = mAdapter.getItemCount();

        Subscription subscription = Observable.just(total)
                .observeOn(Schedulers.newThread())
                .concatMap(new Func1<Integer, Observable<Operation>>() {
                    @Override
                    public Observable<Operation> call(Integer size) {
                        dialog.setMax(size);
                        List<Operation> list = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            list.add(mAdapter.getItem(i));
                        }
                        return Observable.from(list);
                    }
                })
                .concatMap(new Func1<Operation, Observable<Operation>>() {
                    @Override
                    public Observable<Operation> call(Operation op) {
                        op.consume(ctx);
                        return Observable.just(op)
                                .delay(100, TimeUnit.MILLISECONDS); // just for testing
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Operation>() {
                    @Override
                    public void onCompleted() {
                        mAdapter.notifyDataSetChanged();
                        dialog.cancel();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dialog.cancel();
                    }

                    @Override
                    public void onNext(Operation op) {
                        mAdapter.remove(op);
                        dialog.setProgress(total - mAdapter.getItemCount());
                    }
                });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                subscription.unsubscribe();
                mAdapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }
}
