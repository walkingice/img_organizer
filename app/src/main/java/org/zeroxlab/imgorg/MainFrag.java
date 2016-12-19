// vim: et sw=4 sts=4 tabstop=4
package org.zeroxlab.imgorg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainFrag extends Fragment implements View.OnClickListener {

    private Context mCtx;
    private Resources mRes;

    private TextView mFromDir;
    private TextView mToDir;

    private View mBtnAnaly;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mCtx = getActivity();
        mRes = getResources();
        Log.d(ImgOrg.TAG, "MainFrag onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        View root = getView();
        mFromDir = (TextView) root.findViewById(R.id.main_from_path);
        mToDir = (TextView) root.findViewById(R.id.main_to_path);
        mBtnAnaly = root.findViewById(R.id.main_btn_analy);
        mBtnAnaly.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_btn_analy) {
            Intent i = new Intent(mCtx, AnalyActivity.class);
            startActivity(i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void initPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String keyMax = mRes.getString(R.string.key_maximum);
        String keyVideo = mRes.getString(R.string.key_handle_video);
        String keyFrom = mRes.getString(R.string.key_from_dir);
        String keyTo = mRes.getString(R.string.key_to_dir);
        int max = Integer.parseInt(prefs.getString(keyMax, ImgOrg.DEF_MAX));
        boolean handleVideo = prefs.getBoolean(keyVideo, ImgOrg.DEF_HANDLE_VIDEO);
        String from = prefs.getString(keyFrom, ImgOrg.DEF_FROM.getPath());
        String to = prefs.getString(keyTo, ImgOrg.DEF_TO.getPath());

        mFromDir.setText(from);
        mToDir.setText(to);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(keyVideo, handleVideo);
        editor.putString(keyMax, "" + max);
        editor.putString(keyFrom, from);
        editor.putString(keyTo, to);
        editor.commit();
    }
}
