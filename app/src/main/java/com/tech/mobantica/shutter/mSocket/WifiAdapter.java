package com.tech.mobantica.shutter.mSocket;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tech.mobantica.shutter.R;

import java.util.List;

/**
 * Created by Comp2 on 1/22/2018.
 */

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.MyViewHolder> {

    private final Context context;
    private final List<WifiPojo> list;

    public WifiAdapter(Context _context, List<WifiPojo> _list) {

        this.context=_context;
        this.list=_list;

//        context = _context;
//        list = _list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WifiPojo pos = list.get(position);
        holder.tvSsidname.setText(pos.ssidName);

        if (pos.flagIsconnected.equals("1"))
        {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Connected");

        }
        else if(pos.flagIsconnected.equals("0"))
        {
            holder.tvStatus.setVisibility(View.INVISIBLE);
        }
        else {

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSsidname;
        public TextView tvStatus;

        public MyViewHolder(View view) {
            super(view);
            tvSsidname = (TextView) view.findViewById(R.id.ssidName);
            tvStatus = (TextView) view.findViewById(R.id.status);
        }
    }

}
