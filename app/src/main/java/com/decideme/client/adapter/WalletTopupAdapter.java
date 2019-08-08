package com.decideme.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.decideme.client.R;


/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class WalletTopupAdapter extends RecyclerView.Adapter<WalletTopupAdapter.ViewHolder> {

    String[] amount;
    Context context;
    private static LayoutInflater inflater = null;

    public WalletTopupAdapter(Context context, String[] amount) {
        this.context = context;
        this.amount = amount;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_wallet_topup_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_amount.setText("P " + amount[position]);
    }

    @Override
    public int getItemCount() {
        return amount.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_amount;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_amount = (TextView) itemView.findViewById(R.id.tv_topup);
        }
    }
}
