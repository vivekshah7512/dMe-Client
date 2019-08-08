package com.decideme.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;

import java.util.ArrayList;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class WorkerItemAdapter extends RecyclerView.Adapter<WorkerItemAdapter.ViewHolder> {

    private String[] worker_id,
            worker_name,
            worker_image,
            worker_price,
            worker_lat,
            worker_long;
    Context context;
    private static LayoutInflater inflater = null;
    boolean[] checkBoxState;
    public static ArrayList<String> CheckId;
    public MarkerClick markerClick;

    public WorkerItemAdapter(Context context, String[] worker_id,
                             String[] worker_name,
                             String[] worker_image,
                             String[] worker_price,
                             String[] worker_lat,
                             String[] worker_long) {
        this.context = context;
        this.worker_id = worker_id;
        this.worker_name = worker_name;
        this.worker_image = worker_image;
        this.worker_price = worker_price;
        this.worker_lat = worker_lat;
        this.worker_long = worker_long;
        checkBoxState = new boolean[worker_id.length];
        CheckId = new ArrayList<>();
        markerClick = (MarkerClick) context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public interface MarkerClick {
        void markerClick(int pos);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_worker_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv_name.setText(worker_name[position]);
        /*Glide.with(context).load(worker_image[position])
                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)                            .error(R.mipmap.user))
                .thumbnail(0.5f)
                .into(holder.img);*/

        holder.img_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClick.markerClick(position);
            }
        });

        holder.checkBox.setChecked(checkBoxState[position]);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    checkBoxState[position] = true;
                    if (CheckId.size() > 3) {
                        Toast.makeText(context, "Maximum 3 recruits you can select", Toast.LENGTH_SHORT).show();
                    } else {
                        CheckId.add(worker_id[position]);
                    }
                } else {
                    checkBoxState[position] = false;
                    CheckId.remove(worker_id[position]);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return worker_id.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        ImageView img, img_location;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_worker_name);
            img = (ImageView) itemView.findViewById(R.id.img_worker);
            img_location = (ImageView) itemView.findViewById(R.id.img_location);
            checkBox = (CheckBox) itemView.findViewById(R.id.cb_worker);
        }
    }
}
