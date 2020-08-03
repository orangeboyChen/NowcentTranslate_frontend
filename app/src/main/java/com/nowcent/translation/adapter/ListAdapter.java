package com.nowcent.translation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.wear.widget.WearableRecyclerView;

import com.nowcent.translation.R;

import java.util.List;

public class ListAdapter extends WearableRecyclerView.Adapter<ListAdapter.ListHolder> {

    List<String> list;
    Context context;

    public ListAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
    }



    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu, parent, false);
        ListHolder listHolder = new ListHolder(view);
        return listHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder holder, int position) {
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListHolder extends WearableRecyclerView.ViewHolder{

        TextView textView;

        public ListHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
