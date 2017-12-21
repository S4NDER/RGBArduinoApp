package com.jansen.sander.carrgbapp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Sander on 20/12/2017.
 */

public class CustomColorDataAdapter extends RecyclerView.Adapter<CustomColorDataAdapter.CustomColorViewHolder> {

    protected List<CustomColor> customColors;

    public class CustomColorViewHolder extends RecyclerView.ViewHolder{
        private TextView title, color, colorbar;

        public CustomColorViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.rgb_color_title);
            color = (TextView) itemView.findViewById(R.id.custom_red);
            colorbar =(TextView) itemView.findViewById(R.id.color_bar);
        }
    }

    public CustomColorDataAdapter(List<CustomColor> customColors){
        this.customColors = customColors;
    }

    @Override
    public CustomColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_row, parent, false);
        return new CustomColorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomColorViewHolder holder, int position) {
        CustomColor customColor = customColors.get(position);
        holder.title.setText("RGB Color:");
        holder.color.setText("rgb("+customColor.getRed()+", " +customColor.getGreen()+", "+ customColor.getBlue()+")");
        holder.colorbar.setBackgroundColor(Color.rgb(customColor.getRed(), customColor.getGreen(), customColor.getBlue()));

    }

    @Override
    public int getItemCount() {
        return customColors.size();
    }

    public int getCid(int position){
        CustomColor customColor = customColors.get(position);
        return customColor.getCid();
    }
}
