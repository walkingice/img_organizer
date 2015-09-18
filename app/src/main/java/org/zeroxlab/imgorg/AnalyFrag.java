// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import org.zeroxlab.imgorg.R;
import org.zeroxlab.imgorg.ImgOrg;
import org.zeroxlab.imgorg.lib.Organizer;
import org.zeroxlab.imgorg.lib.Organizer.Operation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AnalyFrag extends Fragment implements View.OnClickListener {

    private Resources mRes;

    private TextView mResults;
    private Button mOrganize;
    private ArrayList<Organizer.Operation> mOps;

    private File mDirFrom;
    private File mDirTo;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mRes = getResources();
        mOps = new ArrayList<Organizer.Operation>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        View root = getView();
        mResults = (TextView) root.findViewById(R.id.analy_results);
        mResults.setText("Calculating");
        mOrganize = (Button)root.findViewById(R.id.btn_organize);
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
        String keyFrom = mRes.getString(R.string.key_from_dir);
        String keyTo = mRes.getString(R.string.key_to_dir);
        String from = prefs.getString(keyFrom, ImgOrg.DEF_FROM.getPath());
        String to   = prefs.getString(keyTo, ImgOrg.DEF_TO.getPath());
        mDirFrom = new File(from);
        mDirTo   = new File(to);
    }

    private void createOptions() {
        final ProgressDialog dialog = new ProgressDialog(this.getActivity());
        dialog.setMessage("Parsing...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        AsyncTask<Object, Integer, Object> task = new AsyncTask<Object, Integer, Object> () {
            File[] medias;
            StringBuffer sb = new StringBuffer();

            @Override
            protected Object doInBackground(Object... params) {
                int count = 0;

                for (final File media: medias) {
                    Organizer.Operation op = Organizer.createOp(media, mDirTo, "");
                    if (op.isPending()) {
                        // do not append too much string
                        if (count <= 30) {
                            sb.append(op.toString() + "\n");
                        }
                        mOps.add(op);
                    }
                    count++;
                    publishProgress(new Integer(count));
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                try {
                    medias = Organizer.findMedias(mDirFrom);
                } catch (IOException e) {
                    Log.e(ImgOrg.TAG, e.toString());
                    medias = new File[0];
                }
                dialog.setMax(medias.length);
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                dialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Object result) {
                mResults.setText(sb.toString());
                dialog.cancel();
                Log.d(ImgOrg.TAG, "Done");
            }
        };

        task.execute();
    }

    private void consumeOperations() {
        if (mOps.size() == 0) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this.getActivity());

        final AsyncTask<Object, Integer, Object> task = new AsyncTask<Object, Integer, Object> () {

            @Override
            protected Object doInBackground(Object... params) {
                int count = 0;
                ArrayList<Operation> ops = new ArrayList<Operation>();
                while (!this.isCancelled() && !mOps.isEmpty()) {
                    Operation op = mOps.remove(0);
                    op.consume();
                    count++;
                    publishProgress(new Integer(count));
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                dialog.setMax(mOps.size());
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                dialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Object result) {
                dialog.cancel();
            }
        };

        dialog.setMessage("Moving...");
        dialog.setCancelable(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog){
                task.cancel(true);
            }
        });
        dialog.show();
        task.execute();
    }

}
