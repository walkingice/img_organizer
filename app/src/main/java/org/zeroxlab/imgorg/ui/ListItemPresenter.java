package org.zeroxlab.imgorg.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zeroxlab.imgorg.R;
import org.zeroxlab.imgorg.lib.Operation;

import java.io.File;

public class ListItemPresenter implements SelectorAdapter.Presenter<Operation> {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.view_list_item, parent, false);
        return new InnerViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Operation item) {
        InnerViewHolder holder = (InnerViewHolder) viewHolder;
        File src = new File(item.getSource());
        File dst = new File(item.getDestination());
        holder.iText1.setText(src.getName());
        holder.iText2.setText(item.getDestination());
    }

    @Override
    public void onUnbindViewHolder(RecyclerView.ViewHolder viewHolder) {
    }

    class InnerViewHolder extends RecyclerView.ViewHolder {
        TextView iText1;
        TextView iText2;

        public InnerViewHolder(View view) {
            super(view);
            iText1 = (TextView) view.findViewById(android.R.id.text1);
            iText2 = (TextView) view.findViewById(android.R.id.text2);
        }
    }
}
